package it.sephiroth.android.library.overlaymenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.AnimRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by alessandro crugnola
 * alessandro.crugnola@gmail.com
 * on 29/03/15.
 */
public class OverMenuView extends View implements GestureDetector.OnGestureListener {
    static final String TAG = "OverMenuView";
    public static final boolean DEBUG = true;
    private boolean mActivateOnLongPress;
    private final GestureDetector gestureDetector;
    private CharSequence[] mEntries;
    private OverMenuLayout mOverMenuLayout;
    private boolean mMenuShown;
    private Animation mInAnimation;
    private Animation mOutAnimation;
    private int mTargetViewId;
    private OnMenuVisibilityChangeListener menuVisibilityChangeListener;
    private boolean mEntriesChanged;

    public OverMenuView(final Context context) {
        this(context, null);
    }

    public OverMenuView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverMenuView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        gestureDetector = new GestureDetector(context, this);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        TypedArray array =
            context.obtainStyledAttributes(attrs, R.styleable.OverMenuView, defStyleAttr, R.style.OverMenuViewDefaultStyle);

        final boolean activateOnLongPress = array.getBoolean(R.styleable.OverMenuView_overmenu_activateOnLongPress, false);
        final int menuStyle = array.getResourceId(R.styleable.OverMenuView_overmenu_menuStyle, R.style.OverMenuLayoutDefaultStyle);
        final CharSequence[] entries = array.getTextArray(R.styleable.OverMenuView_android_entries);
        setInAnimation(context, array.getResourceId(R.styleable.OverMenuView_android_inAnimation, 0));
        setOutAnimation(context, array.getResourceId(R.styleable.OverMenuView_android_outAnimation, 0));

        mTargetViewId = array.getResourceId(R.styleable.OverMenuView_overmenu_centerInView, 0);

        array.recycle();

        setActivateOnLongPress(activateOnLongPress);
        createMenuOverlay(menuStyle);
        setEntries(entries);
        setWillNotDraw(true);
    }

    private void setInAnimation(final Context context, @AnimRes final int resourceId) {
        if (0 != resourceId) {
            try {
                mInAnimation = AnimationUtils.loadAnimation(context, resourceId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void setOutAnimation(final Context context, @AnimRes final int resourceId) {
        if (resourceId != 0) {
            try {
                mOutAnimation = AnimationUtils.loadAnimation(context, resourceId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnSelectionChangedListener(final OnSelectionChangeListener listener) {
        mOverMenuLayout.setOnSelectionChangeListener(listener);
    }

    public OnSelectionChangeListener getOnSelectionChangedListener() {
        return mOverMenuLayout.getOnSelectionChangeListener();
    }

    public void setOnMenuVisibilityChangeListener(
        final OnMenuVisibilityChangeListener menuVisibilityChangeListener) {
        this.menuVisibilityChangeListener = menuVisibilityChangeListener;
    }

    public OnMenuVisibilityChangeListener getOnMenuVisibilityChangeListener() {
        return menuVisibilityChangeListener;
    }

    public Animation getInAnimation() {
        return mInAnimation;
    }

    public Animation getOutAnimation() {
        return mOutAnimation;
    }

    public void setActivateOnLongPress(final boolean value) {
        if (DEBUG) {
            Log.d(TAG, "setActivateOnLongPress: " + value);
        }
        mActivateOnLongPress = value;
        gestureDetector.setIsLongpressEnabled(value);
    }

    public boolean getActivateOnLongPress() {
        return mActivateOnLongPress;
    }

    public void setEntries(CharSequence[] entries) {
        if (DEBUG) {
            Log.i(TAG, "setEntries");
        }
        mEntries = entries;
        mEntriesChanged = true;
    }

    @SuppressWarnings ("unused")
    public CharSequence[] getEntries() {
        return mEntries;
    }

    private void createMenuOverlay(final int menuStyle) {
        if (DEBUG) {
            Log.i(TAG, "createMenuOverlay");
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        mOverMenuLayout = new OverMenuLayout(getContext(), null, 0, menuStyle);
        mOverMenuLayout.setId(R.id.OverMenuId);
        mOverMenuLayout.setLayoutParams(params);
    }

    private void showMenuOverlay() {
        if (DEBUG) {
            Log.i(TAG, "showMenuOverlay");
        }

        if (mMenuShown) {
            Log.w(TAG, "menu already shown");
            return;
        }

        FrameLayout root = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();
        int translationY = 0;
        int translationX = 0;

        if (mTargetViewId != 0) {
            View view = ((Activity) getContext()).findViewById(mTargetViewId);
            if (null != view) {
                Rect outRect = new Rect();
                Rect outRootRect = new Rect();
                root.getGlobalVisibleRect(outRootRect);
                view.getGlobalVisibleRect(outRect);

                final int[] point = new int[2];
                view.getLocationOnScreen(point);

                translationY = -(outRootRect.height() / 2 - (outRect.height() / 2 + outRect.top));
                translationX = -(outRootRect.width() / 2 - (outRect.width() / 2 + outRect.left));

                if (DEBUG) {
                    Log.v(TAG, "outRootRect: " + outRootRect);
                    Log.v(TAG, "outRect: " + outRect);
                    Log.v(TAG, "position: " + point[0] + "x" + point[1]);

                    Log.v(TAG, "translationX: " + translationX);
                    Log.v(TAG, "translationY: " + translationY);
                }
            }
        }

        final boolean entriesChanged = mEntriesChanged;

        if (entriesChanged) {
            mOverMenuLayout.setEntries(mEntries);
            mEntriesChanged = false;
        }

        if (root.findViewById(R.id.OverMenuId) == null) {
            root.addView(mOverMenuLayout);
            mOverMenuLayout.setTranslationY(translationY);
            mOverMenuLayout.setTranslationX(translationX);
        } else if (!entriesChanged) {
            mOverMenuLayout.updatePosition();
        }

        if (null != mInAnimation) {
            OverMenuLayout.showView(mOverMenuLayout, mInAnimation);
        } else {
            mOverMenuLayout.setVisibility(View.VISIBLE);
        }

        if (null != menuVisibilityChangeListener) {
            menuVisibilityChangeListener.onVisibilityChanged(this, true);
        }
        mMenuShown = true;
    }

    private void hideMenuOverlay() {
        if (DEBUG) {
            Log.i(TAG, "hideMenuOverlay");
        }
        if (mMenuShown) {
            if (null != mOutAnimation) {
                OverMenuLayout.hideView(mOverMenuLayout, mOutAnimation);
            } else {
                mOverMenuLayout.setVisibility(View.INVISIBLE);
            }

            if (null != menuVisibilityChangeListener) {
                menuVisibilityChangeListener.onVisibilityChanged(this, false);
            }

            mMenuShown = false;
        } else {
            Log.w(TAG, "menu not visible");
        }
    }

    private void removeMenuOverlay() {
        if (DEBUG) {
            Log.i(TAG, "removeMenuOverlay");
        }
        FrameLayout root = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();
        View view = root.findViewById(R.id.OverMenuId);
        if (null != view) {
            root.removeView(view);
        }
        mMenuShown = false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        final int action = event.getActionMasked();

        if (!gestureDetector.onTouchEvent(event) && action == MotionEvent.ACTION_UP) {
            return onUp(event);
        }

        return true;
    }

    public boolean onUp(final MotionEvent event) {
        if (DEBUG) {
            Log.i(TAG, "onUp");
        }
        hideMenuOverlay();
        return true;
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        if (!getActivateOnLongPress()) {
            if (DEBUG) {
                Log.i(TAG, "onDown");
            }
            showMenuOverlay();
            return true;
        }
        return false;
    }

    @Override
    public void onShowPress(final MotionEvent e) { }

    @Override
    public boolean onSingleTapUp(final MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
        if (mMenuShown) {
            mOverMenuLayout.onScroll(e1, e2, distanceX, distanceY);
        }
        return false;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        if (getActivateOnLongPress()) {
            if (DEBUG) {
                Log.i(TAG, "onLongPress");
            }
            showMenuOverlay();
        }
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMenuShown = false;
        mOverMenuLayout = null;
    }

    public void setDefaultPosition(final int position) {
        mOverMenuLayout.setNextPosition(position);
    }

    public int getDefaultPosition() {
        return mOverMenuLayout.getNextPosition();
    }

    public int getSelectedPosition() {
        return mOverMenuLayout.getSelectedPosition();
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(final int position);
    }

    public interface OnMenuVisibilityChangeListener {
        void onVisibilityChanged(View view, boolean visible);
    }
}
