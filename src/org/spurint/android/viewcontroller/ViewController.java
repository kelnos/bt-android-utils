package org.spurint.android.viewcontroller;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

public abstract class ViewController
{
    private final int layoutId;
    private final Activity activity;
    private final View contentView;
    
    private NavigationController navigationController;
    
    public ViewController(Activity activity, int layoutId)
    {
        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");
        if (layoutId == 0)
            throw new IllegalArgumentException("Layout ID cannot be 0");
        
        this.activity = activity;
        this.layoutId = layoutId;
        
        this.contentView = LayoutInflater.from(activity).inflate(this.layoutId, null);
    }
    
    public void attachContentView()
    {
        activity.setContentView(contentView);
    }
    
    public void detachContentView()
    {
        activity.setContentView(null);
    }
    
    protected void _setNavigationController(NavigationController navigationController)
    {
        this.navigationController = navigationController;
    }
    
    protected NavigationController getNavigationController()
    {
        return navigationController;
    }

    protected int getLayoutId()
    {
        return layoutId;
    }

    protected Activity getActivity()
    {
        return activity;
    }

    protected View getContentView()
    {
        return contentView;
    }
}
