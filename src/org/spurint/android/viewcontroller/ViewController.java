package org.spurint.android.viewcontroller;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

/*
 * TODO:
 * * Need to use a weak-key/weak-value version of WeakHashMap to keep track
 *   of Activity->ViewController pairs so we know which VC is the active one
 *   on the Activity.  That way I can fire off the appear/disappear callbacks
 *   properly.  With a regular WeakHashMap we'll retain the Activity instances
 *   and they'll never get GC'd.
 */

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
        onViewWillAppear();
        activity.setContentView(contentView);
        onViewAppeared();
    }

    protected void onViewWillAppear() { }
    protected void onViewAppeared() { }
    //protected void onViewWillDisappear() { }
    //protected void onViewDisappeared() { }

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
