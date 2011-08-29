package org.spurint.android.net;

import java.io.IOException;
import java.util.ArrayDeque;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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

import android.os.AsyncTask;
import android.util.Log;

public class Network {
    private static final String TAG = "Network";

    public interface Cancellable {
        public boolean cancel();
        public boolean cancelled();
        public boolean finished();
    }

    public interface RequestListener {
        void onRequestFinished(HttpResponse resp);
        void onRequestError(Exception e);
    }

    private static class NetworkAsyncTask extends AsyncTask<HttpUriRequest, Void, Object> implements Cancellable {
        private HttpClient httpClient;
        private RequestListener listener;
        private Network network;
        private HttpUriRequest request;
        private boolean cancelled;

        NetworkAsyncTask(HttpClient httpClient,
                         HttpUriRequest request,
                         RequestListener listener,
                         Network network)
        {
            this.httpClient = httpClient;
            this.request = request;
            this.listener = listener;
            this.network = network;
        }

        HttpUriRequest getRequest() {
            return request;
        }

        @Override
        protected Object doInBackground(HttpUriRequest...requests)
        {
            try {
                Log.d(TAG, "starting http request");
                HttpResponse resp = httpClient.execute(requests[0]);
                return resp;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object obj)
        {
            network.notifyDone(this);

            Log.d(TAG, "post execute: " + obj.toString());

            if (obj instanceof HttpResponse) {
                HttpResponse resp = (HttpResponse)obj;
                Log.d(TAG, "got http response code " + resp.getStatusLine().getStatusCode());
                HttpEntity entity = resp.getEntity();
                Log.d(TAG, "body is type " + entity.getContentType() + ", length " + entity.getContentLength());
                listener.onRequestFinished((HttpResponse)obj);
            } else
                listener.onRequestError((Exception)obj);
        }

        // annoyingly, isCancelled() is final, so we can't override
        @Override
        public boolean cancelled()
        {
            return cancelled;
        }

        @Override
        public boolean cancel()
        {
            if (!cancelled) {
                if (network.cancelTask(this)) {
                    cancelled = true;
                    listener.onRequestError(new Exception("Cancelled"));
                }
            }

            return cancelled;
        }

        @Override
        public boolean finished()
        {
            return getStatus() == Status.FINISHED;
        }
    }

    private static final int CONN_TIMEOUT = 30000;
    private static final int SO_TIMEOUT = 45000;

    private static final int MAX_TASKS_IN_FLIGHT = 32;

    private static final Network instance = new Network();

    private HttpClient httpClient;
    // we can't set the depth of the thread pool before honeycomb, so this
    // is how we'll prevent overrunning the thread pool's queue...
    private ArrayDeque<NetworkAsyncTask> queue = new ArrayDeque<NetworkAsyncTask>(64);
    private int tasksInFlight;

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
    }

    public Cancellable executeAsync(final HttpUriRequest request, final RequestListener listener)
    {
        NetworkAsyncTask task = new NetworkAsyncTask(httpClient, request, listener, this);
        queue.add(task);
        handleQueue();
        return task;
    }

    public void executeSync(HttpUriRequest request, RequestListener listener)
    {
        try {
            HttpResponse resp = httpClient.execute(request);
            listener.onRequestFinished(resp);
        } catch (ClientProtocolException e) {
            listener.onRequestError(e);
        } catch (IOException e) {
            listener.onRequestError(e);
        }
    }

    private void handleQueue()
    {
        while (tasksInFlight < MAX_TASKS_IN_FLIGHT && !queue.isEmpty()) {
            NetworkAsyncTask task = queue.remove();
            task.execute(task.getRequest());
            ++tasksInFlight;
        }
    }

    void notifyDone(NetworkAsyncTask task)
    {
        --tasksInFlight;
        handleQueue();

    }

    boolean cancelTask(NetworkAsyncTask task)
    {
        return queue.remove(task) || task.cancel(false);
    }
}
