package org.spurint.android.viewcontroller;

import org.spurint.android.R;

import android.app.Activity;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewController extends ViewController
{
    private final ListView listView;
    
    public ListViewController(Activity activity)
    {
        super(activity, R.layout.list_content);
        listView = (ListView)getContentView();
    }
    
    public ListView getListView()
    {
        return listView;
    }
    
    public void setListAdapter(ListAdapter adapter)
    {
        listView.setAdapter(adapter);
    }
}
