/*
 * Copyright (c) 2011 Brian J. Tarricone <brian@tarricone.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.spurint.android.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;

import android.os.Build;
import android.os.Handler;

public class Network
{
    //private static final String TAG = "Network";

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE;
    private static final ExecutorService pool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                                                                       MAX_POOL_SIZE,
                                                                       30, TimeUnit.SECONDS,
                                                                       new LinkedBlockingQueue<Runnable>());

    public interface RequestListener
    {
        void onRequestHeadersReceived(Future<HttpResponse> requestToken, int statusCode, String statusCodeMessage, Map<String,String> headers);
        void onRequestReceivedBodyData(Future<HttpResponse> requestToken, byte[] data, int dataLength, long totalBytesRead, long totalExpectedLength);
        void onRequestFinished(Future<HttpResponse> requestToken);
        void onRequestError(Future<HttpResponse> requestToken, Exception e);
    }

    private static class NetworkTask implements Callable<HttpResponse>,
                                                Runnable
    {
        private final HttpClient httpClient;
        private final RequestListener listener;
        private final HttpUriRequest request;
        private final Handler handler;

        private Thread taskThread;
        private HttpResponse finalResponse;
        private Exception finalError;
        private Future<HttpResponse> token;

        NetworkTask(HttpClient httpClient,
                    HttpUriRequest request,
                    RequestListener listener,
                    Handler handler)
        {
            this.httpClient = httpClient;
            this.request = request;
            this.listener = listener;
            this.handler = handler;
        }

        public void setToken(Future<HttpResponse> token)
        {
            this.token = token;
        }

        @Override
        public HttpResponse call() throws Exception
        {
            taskThread = Thread.currentThread();
            HttpResponse resp = null;
            Exception err = null;

            if (taskThread.isInterrupted())
                return null;

            try {
                //Log.d(TAG, "starting http request");
                resp = httpClient.execute(request);
            } catch (Exception e) {
                err = e;
            }

            if (taskThread.isInterrupted())
                return null;

            finalResponse = resp;
            finalError = err;

            // this is so dirty
            while (token == null)
                Thread.sleep(50);

            if (resp != null && finalError != null && listener != null) {
                Map<String,String> headers = new HashMap<String,String>(resp.getAllHeaders().length);
                for (Header h : resp.getAllHeaders())
                    headers.put(h.getName(), h.getValue());
                listener.onRequestHeadersReceived(token, resp.getStatusLine().getStatusCode(),
                                                  resp.getStatusLine().getReasonPhrase(), headers);
            }

            if (resp != null && finalError == null && resp.getEntity() != null) {
                final long contentLength = resp.getEntity().getContentLength();

                if (contentLength != 0) {
                    InputStream is = null;

                    try {
                        is = resp.getEntity().getContent();
                        byte[] buf = new byte[RESPONSE_ENTITY_BUFFER_SIZE];

                        long totalBytesRead = 0;
                        int bin;
                        while ((bin = is.read(buf)) != -1) {
                            if (taskThread.isInterrupted()) {
                                resp = null;
                                break;
                            }

                            if (bin > 0) {
                                totalBytesRead += bin;

                                if (handler != null && listener != null) {
                                    final long fTotalBytesRead = totalBytesRead;
                                    final byte[] fBuf = new byte[bin];
                                    System.arraycopy(buf, 0, fBuf, 0, bin);

                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            listener.onRequestReceivedBodyData(token, fBuf, fBuf.length, fTotalBytesRead, contentLength);
                                        }
                                    });
                                } else if (listener != null) {
                                    listener.onRequestReceivedBodyData(token, buf, bin, totalBytesRead, contentLength);
                                }
                            }
                        }
                    } catch (Exception e) {
                        finalError = e;
                    } finally {
                        if (is != null)
                            try { is.close(); } catch (IOException e) { }
                    }
                }
            }

            if (listener != null && !taskThread.isInterrupted()) {
                if (handler != null)
                    handler.post(this);
                else
                    run();
            }

            return resp;
        }

        // this is only for handling the response, not for running the task
        @Override
        public void run() {
            if (!taskThread.isInterrupted()) {
                if (finalError != null) {
                    listener.onRequestError(token, finalError);
                } else if (finalResponse != null) {
                    //Log.d(TAG, "got http response code " + finalResponse.getStatusLine().getStatusCode());
                    //HttpEntity entity = finalResponse.getEntity();
                    //Log.d(TAG, "body is type " + entity.getContentType() + ", length " + entity.getContentLength());
                    listener.onRequestFinished(token);
                }
            }
        }
    }

    private static final int CONN_TIMEOUT = 30000;
    private static final int SO_TIMEOUT = 45000;
    private static final int RESPONSE_ENTITY_BUFFER_SIZE = 512 * 1024;  /* 512 kB */

    private static final Network instance = new Network();

    private final DefaultHttpClient httpClient;

    public static synchronized Network get()
    {
        return instance;
    }

    private Network()
    {
        BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONN_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", new PlainSocketFactory(), 80));
        try {
            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)); // new SSLSocketFactory(KeyStore.getInstance(KeyStore.getDefaultType())), 443));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(params, registry);
        httpClient = new DefaultHttpClient(connManager, params);
        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
            {
                Locale locale = Locale.getDefault();
                String localeStr = locale.getLanguage() + "_" + locale.getCountry();
                request.setHeader("User-Agent", "BTNetwork (Android; Android OS " + Build.VERSION.RELEASE + "; " + localeStr + ")");
                /*Header[] headers = request.getAllHeaders();
                for (Header h : headers) {
                    Log.d(TAG, h.getName() + ": " + h.getValue());
                }*/
            }
        });

        /*
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
*/
    }

    public void setCredentials(HttpUriRequest request, String username, String password)
    {
        URI uri = request.getURI();
        if (uri == null)
            return;

        String host = uri.getHost();
        if (host == null)
            return;

        int port = uri.getPort();
        if (port == 0) {
            if (uri.getScheme() == null)
                return;

            if (uri.getScheme().equals("http"))
                port = 80;
            else if (uri.getScheme().equals("https"))
                port = 443;
            else
                return;
        }

        httpClient.getCredentialsProvider().setCredentials(new AuthScope(host, port),
                                                           new UsernamePasswordCredentials(username, password));
    }

    public Future<HttpResponse> execute(HttpUriRequest request,
                                        RequestListener listener,
                                        Handler handler)
    {
        NetworkTask task = new NetworkTask(httpClient, request, listener, handler);
        Future<HttpResponse> requestToken = pool.submit((Callable<HttpResponse>)task);
        task.setToken(requestToken);
        return requestToken;
    }

    public Future<HttpResponse> execute(HttpUriRequest request,
                                        RequestListener listener)
    {
        return execute(request, listener, null);
    }
}
