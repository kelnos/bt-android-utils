package org.spurint.android.helpers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class StringHelper {
    public static String stringFromInputStream(InputStream is) {
        final char[] buffer = new char[1024];
        StringBuilder out = new StringBuilder();

        try {
            Reader in = new InputStreamReader(is, "UTF-8");
            int bin;
            do {
                bin = in.read(buffer);
                if (bin > 0) {
                    out.append(buffer, 0, bin);
                }
            } while (bin >= 0);
        } catch (Exception e) {
            return null;
        }

        return out.toString();
    }
}
