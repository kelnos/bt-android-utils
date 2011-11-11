package org.spurint.android.viewcontroller;

import android.app.Activity;

public class NavigationActivity extends Activity
{
    private NavigationController navigationController;

    @Override
    public void onBackPressed()
    {
        if (navigationController != null && navigationController.getViewControllerCount() > 1)
            navigationController.popViewController();
        else
            finish();
    }

    public void setNavigationController(NavigationController navigationController)
    {
        this.navigationController = navigationController;
        navigationController.attachContentView();
    }

    protected NavigationController getNavigationController()
    {
        return navigationController;
    }
}
