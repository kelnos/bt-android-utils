package org.spurint.android.listview;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionHeaderRow implements ListRow {
    private String title;

    public SectionHeaderRow(String title) {
        this.title = title;
    }

    @Override
    public View getView(Context context) {
    	final LinearLayout layout = new LinearLayout(context);

    	final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final TextView text = (TextView)inflater.inflate(R.layout.preference_category, null);
        android.util.Log.d("foo", "inflated pref category is a " + text.getClass().getName());
        text.setText(title);
        
        layout.addView(text,
        			   LinearLayout.LayoutParams.MATCH_PARENT,
        			   LinearLayout.LayoutParams.MATCH_PARENT);

        layout.setOnClickListener(null);
        layout.setOnLongClickListener(null);
        layout.setClickable(false);
        layout.setLongClickable(false);

        return layout;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
