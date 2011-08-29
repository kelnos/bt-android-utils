package org.spurint.android.listview;

import java.util.List;
import java.util.WeakHashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ListRowAdapter extends ArrayAdapter<ListRow> {
    private List<ListRow> items;
    private Context context;

    private WeakHashMap<Integer,View> views = new WeakHashMap<Integer,View>();

    public ListRowAdapter(Context context, List<ListRow> items) {
        super(context, 0, items);
        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;

        ListRow row = items.get(position);
        if (row != null) {
            v = views.get(position);
            if (v == null) {
                v = row.getView(context);
                views.put(position, v);
            }
        }

        return v;
    }

    @Override
    public boolean isEnabled(int position) {
        return !(items.get(position) instanceof SectionHeaderRow);
    }
}
