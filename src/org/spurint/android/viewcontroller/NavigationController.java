package org.spurint.android.viewcontroller;

import java.util.Stack;

import android.app.Activity;

public class NavigationController extends ViewController
{
    private final Stack<ViewController> viewControllers = new Stack<ViewController>();

    public NavigationController(Activity activity, ViewController rootViewController)
    {
        super(activity, rootViewController.getLayoutId());
        viewControllers.push(rootViewController)._setNavigationController(this);
    }
    
    @Override
    public void attachContentView()
    {
        viewControllers.peek().attachContentView();
    }

    public void pushViewController(ViewController viewController)
    {
        viewControllers.push(viewController)._setNavigationController(this);
        attachContentView();
    }

    public void popViewController()
    {
        if (viewControllers.size() == 1)
            throw new RuntimeException("Can't pop root view controller");
        
        viewControllers.pop()._setNavigationController(null);
        attachContentView();
    }
    
    public int getViewControllerCount()
    {
        return viewControllers.size();
    }
}
