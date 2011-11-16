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

import java.util.Stack;

import org.spurint.android.viewcontroller.ViewControllerActivity.TransitionType;

import android.view.View;

public class NavigationController extends ViewController
{
    private final Stack<ViewController> viewControllers = new Stack<ViewController>();
    private TransitionType transitionAnimation = TransitionType.SLIDE_HORIZONTAL;

    public NavigationController(ViewControllerActivity activity, ViewController rootViewController)
    {
        super(activity, rootViewController.getLayoutId());
        viewControllers.push(rootViewController)._setNavigationController(this);
    }

    public void pushViewController(ViewController viewController, boolean animated)
    {
        viewControllers.push(viewController)._setNavigationController(this);

        if (animated && transitionAnimation != null)
            getActivity().setContentViewController(viewController, transitionAnimation, false);
        else
            getActivity().setContentViewController(viewController);
    }

    public void popViewController(boolean animated)
    {
        if (viewControllers.size() == 1)
            throw new RuntimeException("Can't pop root view controller");

        viewControllers.pop()._setNavigationController(null);

        if (animated && transitionAnimation != null)
            getActivity().setContentViewController(viewControllers.peek(), transitionAnimation, true);
        else
            getActivity().setContentViewController(viewControllers.peek());
    }
    
    public int getViewControllerCount()
    {
        return viewControllers.size();
    }

    public TransitionType getTransitionAnimation()
    {
        return transitionAnimation;
    }

    public void setTransitionAnimation(TransitionType transitionAnimation)
    {
        this.transitionAnimation = transitionAnimation;
    }
    
    @Override
    protected View getContentView()
    {
        return viewControllers.peek().getContentView();
    }
}
