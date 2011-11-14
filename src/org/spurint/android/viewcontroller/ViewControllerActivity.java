package org.spurint.android.viewcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ViewFlipper;

public class ViewControllerActivity extends Activity
{
    private static final String TAG = "ViewControllerActivity";
    
    static enum TransitionAnimationType
    {
        SLIDE_HORIZONTAL,
        SLIDE_VERTICAL,
        FLIP,
        FADE,
        ZOOM
    }
    
    private static class IdentityAnimation extends Animation { }

    private ViewFlipper rootView;
    private ViewController rootViewController;
    
    private ViewController visibleViewController;
    private View visibleView;
    
    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        
        rootView = new ViewFlipper(this);
        rootView.setAutoStart(false);
        super.setContentView(rootView);
    }
    
    @Override
    public void onBackPressed()
    {
        if (rootViewController != null && (rootViewController instanceof NavigationController)) {
            NavigationController navigationController = (NavigationController)rootViewController;
            if (navigationController.getViewControllerCount() > 1) {
                navigationController.popViewController(navigationController.getTransitionAnimation() != null);
                return;
            }
        }

        super.onBackPressed();
    }
    
    public void setRootViewController(ViewController vc)
    {
        this.rootViewController = vc;
        setContentViewController(vc);
    }
    
    public ViewController getRootViewController()
    {
        return rootViewController;
    }

    private static final int SLIDE_DURATION = 200;  /* milliseconds */
    private static final int ALPHA_DURATION = 350;  /* milliseconds */
    private static final int ZOOM_DURATION = 200; /* milliseconds */
    
    private void doAnimationCommon(int duration,
                                   final Animation oldViewAnim,
                                   final Animation newViewAnim,
                                   final ViewController newViewController)
    {
        final ViewController oldViewController = visibleViewController;
        final View oldView = visibleView;
        final View newView = newViewController != null ? newViewController.getContentView() : null;

        oldViewAnim.setDuration(duration);
        oldViewAnim.setInterpolator(new AccelerateInterpolator());
        newViewAnim.setDuration(duration);
        newViewAnim.setInterpolator(new AccelerateInterpolator());
        
        oldViewAnim.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if (oldViewController != null)
                    oldViewController.dispatchViewWillDisappear(oldView);
            }

            @Override public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                rootView.setOutAnimation(null);

                if (newViewAnim.hasEnded() && newView != visibleView) {
                    newViewAnim.setDuration(0);

                    if (oldView != null)
                        rootView.removeView(oldView);

                    visibleViewController = newViewController;
                    visibleView = newView;
                }

                if (oldViewController != null)
                    oldViewController.dispatchViewDisappeared(oldView);
            }
        });
        newViewAnim.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if (newViewController != null)
                    newViewController.dispatchViewWillAppear(newView);
            }

            @Override public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                rootView.setInAnimation(null);
                if (oldViewAnim.hasEnded() && newView != visibleView) {
                    if (oldView != null)
                        rootView.removeView(oldView);

                    visibleViewController = newViewController;
                    visibleView = newView;
                }

                if (newViewController != null)
                    newViewController.dispatchViewAppeared(newView);
            }
        });

        setContentViewControllerInternal(visibleViewController, visibleView);

        rootView.addView(newView, 1,
                         new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT));

        rootView.setOutAnimation(oldViewAnim);
        rootView.setInAnimation(newViewAnim);
        rootView.showNext();
    }
    
    private void doSlideHorizAnimation(final ViewController newViewController, boolean reverse)
    {
        final Animation oldViewAnim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, reverse ? 1f : -1f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        final Animation newViewAnim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, reverse ? -1f : 1f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        doAnimationCommon(SLIDE_DURATION, oldViewAnim,
                          newViewAnim, newViewController);
    }
    
    private void doSlideVertAnimation(final ViewController newViewController, boolean reverse)
    {
        final Animation oldViewAnim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, reverse ? 1f : -1f);
        final Animation newViewAnim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, reverse ? -1f : 1f,
                                                             TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        doAnimationCommon(SLIDE_DURATION, oldViewAnim,
                          newViewAnim, newViewController);
    }

    private void doFadeAnimation(final ViewController newViewController)
    {
        final Animation oldViewAnim = new AlphaAnimation(1f, 0f);
        final Animation newViewAnim = new AlphaAnimation(0f, 1f);
        doAnimationCommon(ALPHA_DURATION, oldViewAnim,
                          newViewAnim, newViewController);
    }

    private void doZoomAnimation(final ViewController newViewController, boolean reverse)
    {
        float start, end;
        if (reverse) {
            start = 1f;
            end = 0f;
        } else {
            start = 0f;
            end = 1f;
        }

        final Animation viewAnim = new ScaleAnimation(start, end, start, end,
                                                      Animation.RELATIVE_TO_SELF, 0.5f,
                                                      Animation.RELATIVE_TO_SELF, 0.5f);
        final Animation nullAnim = new IdentityAnimation();

        doAnimationCommon(ZOOM_DURATION, reverse ? viewAnim : nullAnim,
                          reverse ? nullAnim : viewAnim, newViewController);
    }

    private void setContentViewControllerInternal(ViewController newViewController,
                                                  View newView)
    {
        rootView.removeAllViews();
        if (newView != null) {
            rootView.addView(newView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                    ViewGroup.LayoutParams.MATCH_PARENT));
            rootView.setDisplayedChild(0);
        }

        visibleViewController = newViewController;
        visibleView = newView;
    }

    protected void setContentViewController(ViewController newViewController)
    {
        ViewController oldViewController = visibleViewController;
        View oldView = visibleView;
        View newView = newViewController != null ? newViewController.getContentView() : null;

        if (oldViewController != null)
            oldViewController.dispatchViewWillDisappear(oldView);
        if (newViewController != null)
            newViewController.dispatchViewWillAppear(newView);

        setContentViewControllerInternal(newViewController, newView);

        if (oldViewController != null)
            oldViewController.dispatchViewDisappeared(oldView);
        if (newViewController != null)
            newViewController.dispatchViewAppeared(newView);
    }

    protected void setContentViewController(ViewController viewController,
                                            TransitionAnimationType animationType,
                                            boolean reverse)
    {
        if (animationType == null)
            throw new IllegalArgumentException("Animation Type cannot be null");

        if (visibleViewController == null)
            setContentViewController(viewController);
        else {
            switch (animationType) {
                case SLIDE_HORIZONTAL:
                    doSlideHorizAnimation(viewController, reverse);
                    break;
                case SLIDE_VERTICAL:
                    doSlideVertAnimation(viewController, reverse);
                    break;
                case FLIP:
                    throw new UnsupportedOperationException("Transition animation type FLIP not yet implemented");
                case FADE:
                    doFadeAnimation(viewController);
                    break;
                case ZOOM:
                    doZoomAnimation(viewController, reverse);
                    break;
            }
        }
    }
}
