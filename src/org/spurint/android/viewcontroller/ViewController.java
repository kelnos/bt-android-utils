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

package org.spurint.android.viewcontroller;

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
    private final ViewControllerActivity activity;
    private final View contentView;
    
    private NavigationController navigationController;
    
    public ViewController(ViewControllerActivity activity, int layoutId)
    {
        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");
        if (layoutId == 0)
            throw new IllegalArgumentException("Layout ID cannot be 0");
        
        this.activity = activity;
        this.layoutId = layoutId;
        
        this.contentView = LayoutInflater.from(activity).inflate(this.layoutId, null);
    }

    void dispatchViewWillAppear(View view)
    {
        if (view == contentView)
            onViewWillAppear();
    }
    
    void dispatchViewAppeared(View view)
    {
        if (view == contentView)
            onViewAppeared();
    }
    
    void dispatchViewWillDisappear(View view)
    {
        if (view == contentView)
            onViewWillDisappear();
    }
    
    void dispatchViewDisappeared(View view)
    {
        if (view == contentView)
            onViewDisappeared();
    }

    protected void onViewWillAppear() { }
    protected void onViewAppeared() { }
    protected void onViewWillDisappear() { }
    protected void onViewDisappeared() { }

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

    protected ViewControllerActivity getActivity()
    {
        return activity;
    }

    protected View getContentView()
    {
        return contentView;
    }
}
