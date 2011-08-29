package org.spurint.android.listview;

import android.content.Context;
import android.view.View;

public interface ListRow {
    public View getView(Context context);
    public String getTitle();
}
