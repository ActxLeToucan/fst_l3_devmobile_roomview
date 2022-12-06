package fr.antoinectx.roomview;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public abstract class MyActivity extends AppCompatActivity {
    protected MaterialToolbar toolbar;
    private int downX;
    private boolean showNavigationIcon = false;

    /**
     * Initialize the app bar
     *
     * @param title              The title of the app bar
     * @param subtitle           The subtitle of the app bar
     * @param showNavigationIcon Whether to show the navigation icon (the default icon is the back arrow)
     */
    protected void initAppBar(String title, @Nullable String subtitle, boolean showNavigationIcon) {
        initAppBar(title, subtitle, showNavigationIcon, R.drawable.ic_baseline_arrow_back_24, R.string.action_back);
    }

    /**
     * Initialize the app bar
     *
     * @param title                 The title of the app bar
     * @param subtitle              The subtitle of the app bar
     * @param showNavigationIcon    Whether to show the navigation icon
     * @param navigationIcon        The navigation icon to show
     * @param navigationDescription The content description of the navigation icon
     */
    protected void initAppBar(String title, @Nullable String subtitle, boolean showNavigationIcon, @DrawableRes int navigationIcon, @StringRes int navigationDescription) {
        this.showNavigationIcon = showNavigationIcon;

        toolbar = findViewById(R.id.materialToolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationContentDescription(navigationDescription);
        if (subtitle != null) toolbar.setSubtitle(subtitle);
        if (showNavigationIcon) toolbar.setNavigationIcon(navigationIcon);

        setSupportActionBar(toolbar);
    }

    /**
     * Set the navigation icon and its description in the app bar
     *
     * @param navigationIcon        The navigation icon to show
     * @param navigationDescription The content description of the navigation icon
     */
    protected void setAppBarNavigation(@DrawableRes int navigationIcon, @StringRes int navigationDescription) {
        toolbar.setNavigationIcon(navigationIcon);
        toolbar.setNavigationContentDescription(navigationDescription);
    }

    /**
     * This method is called when the user clicks on the back button in the app bar.
     *
     * @param item The item that was clicked.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.showNavigationIcon && item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Clear focus on the search bar when the user clicks outside of it
     * (from <a href="https://stackoverflow.com/a/61290481">StackOverflow</a>)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getRawX();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                //Was it a scroll - If skip all
                if (Math.abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event);
                }
                final int reducePx = 25;
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                //Bounding box is to big, reduce it just a little bit
                outRect.inset(reducePx, reducePx);
                if (!outRect.contains(x, y)) {
                    v.clearFocus();
                    boolean touchTargetIsEditText = false;
                    //Check if another editText has been touched
                    for (View vi : v.getRootView().getTouchables()) {
                        if (vi instanceof EditText) {
                            Rect clickedViewRect = new Rect();
                            vi.getGlobalVisibleRect(clickedViewRect);
                            //Bounding box is to big, reduce it just a little bit
                            clickedViewRect.inset(reducePx, reducePx);
                            if (clickedViewRect.contains(x, y)) {
                                touchTargetIsEditText = true;
                                break;
                            }
                        }
                    }
                    if (!touchTargetIsEditText) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Block the screen orientation in the current one
     * and prevent (indirectly) that the activity is destroyed when the screen is rotated.
     * It is still possible to rotate the screen 180° when it is blocked.
     * <br /><b>Warning:</b> Please do not abuse this method.
     * I wrote this but I'm pretty sure this is not a good practice.
     */
    protected void blockOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }
}
