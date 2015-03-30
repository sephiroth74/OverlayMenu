package it.sephiroth.android.library.overlaymenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by alessandro crugnola
 * on 29/03/15.
 */
final class OverMenuLayout extends FrameLayout implements ViewSwitcher.ViewFactory {
    static final String TAG = "OverMenuLayout";
    private LinearLayout menuLayout;
    private TextSwitcher selectedTextView;
    private CharSequence[] mEntries;
    private int mMenuTextAppearance;
    private int mSelectedTextAppearance;
    private int mItemsGap;
    private int mHeight;
    private int mTop;
    private int mBottom;
    private int mMaxScroll;
    private int mMinScroll;
    private Point mCenter;
    private int mMenuTextGravity;
    private int mMenuTextPadding;
    private int mTextHeight;
    private int mDefaultPosition = -1;
    private boolean mAnimateItems;
    private int mTextSwitcherInAnimation = 0;
    private int mTextSwitcherOutAnimation = 0;
    private final Rect tempRect = new Rect();
    private OverMenuView.OnSelectionChangeListener listener;
    private int mMenuBackground = 0;
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;
    private int mSelectedTextBackground;

    public OverMenuLayout(final Context context) {
        this(context, null);
    }

    public OverMenuLayout(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverMenuLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OverMenuLayout(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.OverMenuLayout, defStyleAttr, defStyleRes);

        int textAppearance = R.style.TextAppearance_AppCompat;
        int selectedTextAppearance = textAppearance;
        int itemsGap = 0;
        int textGravity = Gravity.CENTER;
        int textPadding = 0;
        int selectedTextBackground = R.drawable.overmenu_selectedtext_background;

        final int N = array.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.OverMenuLayout_overmenu_textAppearance) {
                textAppearance = array.getResourceId(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_overmenu_selectedTextAppearance) {
                selectedTextAppearance = array.getResourceId(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_overmenu_selectedTextBackground) {
                selectedTextBackground = array.getResourceId(attr, R.drawable.overmenu_selectedtext_background);

            } else if (attr == R.styleable.OverMenuLayout_android_background) {
                mMenuBackground = array.getResourceId(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_android_padding) {
                paddingLeft = array.getDimensionPixelSize(attr, 0);
                paddingRight = paddingLeft;
                paddingTop = paddingLeft;
                paddingBottom = paddingTop;

            } else if (attr == R.styleable.OverMenuLayout_android_paddingLeft) {
                paddingLeft = array.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_android_paddingRight) {
                paddingRight = array.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_android_paddingTop) {
                paddingTop = array.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_android_paddingBottom) {
                paddingBottom = array.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_android_verticalGap) {
                itemsGap = array.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_overmenu_textGravity) {
                textGravity = array.getInt(attr, Gravity.CENTER);

            } else if (attr == R.styleable.OverMenuLayout_overmenu_textPaddingHorizontal) {
                textPadding = array.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.OverMenuLayout_overmenu_selectedPosition) {
                mDefaultPosition = array.getInteger(attr, -1);

            } else if (attr == R.styleable.OverMenuLayout_android_inAnimation) {
                mTextSwitcherInAnimation = array.getResourceId(attr, R.anim.overmenu_textswitcher_in);

            } else if (attr == R.styleable.OverMenuLayout_android_outAnimation) {
                mTextSwitcherOutAnimation = array.getResourceId(attr, R.anim.overmenu_textswitcher_out);

            } else if (attr == R.styleable.OverMenuLayout_overmenu_animateItems) {
                mAnimateItems = array.getBoolean(attr, true);

            }
        }

        array.recycle();

        setMenuTextPadding(textPadding);
        setMenuTextGravity(textGravity);
        setMenuTextAppearance(textAppearance);
        setSelectedTextAppearance(selectedTextAppearance);
        setSelectedTextBackground(selectedTextBackground);
        setItemsGap(itemsGap);

        initializeMenu(context);

        setWillNotDraw(true);
    }

    private void setSelectedTextBackground(@DrawableRes final int resId) {
        mSelectedTextBackground = resId;
    }

    private void initializeMenu(@NonNull final Context context) {
        menuLayout = new LinearLayout(context);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setMeasureWithLargestChildEnabled(true);
        if (mMenuBackground != 0) {
            menuLayout.setBackgroundResource(mMenuBackground);
        }
        menuLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        menuLayout.setLayerType(LAYER_TYPE_HARDWARE, null);

        LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        addView(menuLayout, params);
    }

    private void initializeTextSwitcher(@NonNull final Context context) {
        selectedTextView = new TextSwitcher(getContext());
        selectedTextView.setFactory(this);
        selectedTextView.setAnimateFirstView(false);
        selectedTextView.setBackgroundResource(mSelectedTextBackground);

        if (mTextSwitcherInAnimation != 0) {
            selectedTextView.setInAnimation(context, mTextSwitcherInAnimation);
        }

        if (mTextSwitcherOutAnimation != 0) {
            selectedTextView.setOutAnimation(context, mTextSwitcherOutAnimation);
        }

        LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        selectedTextView.setLayoutParams(params);
        selectedTextView.setLayerType(LAYER_TYPE_HARDWARE, null);
        addView(selectedTextView);
    }

    public void setOnSelectionChangeListener(final OverMenuView.OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    public OverMenuView.OnSelectionChangeListener getOnSelectionChangeListener() {
        return listener;
    }

    @Override
    public View makeView() {
        TextView textView = new TextView(getContext());
        textView.setTextAppearance(getContext(), mSelectedTextAppearance);
        textView.setGravity(mMenuTextGravity);
        textView.setPadding(mMenuTextPadding, mItemsGap, mMenuTextPadding, mItemsGap);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return textView;
    }

    private void setMenuTextPadding(final int textPadding) {
        mMenuTextPadding = textPadding;
    }

    private void setMenuTextGravity(final int textGravity) {
        mMenuTextGravity = textGravity;
    }

    private void setItemsGap(final int gap) {
        mItemsGap = gap;
    }

    public void setMenuTextAppearance(@StyleRes final int resId) {
        mMenuTextAppearance = resId;
    }

    private void setSelectedTextAppearance(@StyleRes final int resId) {
        mSelectedTextAppearance = resId;
    }

    public void setEntries(CharSequence[] entries) {

        if (entries == mEntries) {
            return;
        }

        mCurrentPosition = -1;
        mCurrentSelectedTextView = null;

        if (null != selectedTextView) {
            removeView(selectedTextView);
            selectedTextView = null;
        }

        initializeTextSwitcher(getContext());
        menuLayout.removeAllViews();
        mEntries = entries;

        if (null == mEntries) {
            return;
        }

        LayoutParams params;

        for (CharSequence string : entries) {
            TextView textView = (TextView) makeView();
            textView.setTextAppearance(getContext(), mMenuTextAppearance);
            textView.setText(string);
            params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            textView.setLayoutParams(params);
            menuLayout.addView(textView);
        }

        menuLayout.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    menuLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    mHeight = menuLayout.getHeight();
                    mTop = menuLayout.getTop();
                    mBottom = menuLayout.getBottom();
                    mTextHeight =
                        selectedTextView.getHeight();

                    int padding = menuLayout.getPaddingBottom() + menuLayout.getPaddingTop();
                    mMaxScroll = (mHeight / 2) - (padding + mItemsGap * 2);
                    mMinScroll = -mMaxScroll;
                    mCenter = new Point(menuLayout.getWidth() / 2, mHeight / 2);

                    if (OverMenuView.DEBUG) {
                        Log.v(TAG, "padding: " + padding);
                        Log.v(TAG, "top: " + mTop);
                        Log.v(TAG, "bottom: " + mBottom);
                        Log.v(TAG, "textHeight: " + mTextHeight);
                        Log.v(TAG, "maxScroll: " + mMaxScroll);
                        Log.v(TAG, "minScroll: " + mMinScroll);
                        Log.v(TAG, "center: " + mCenter);
                        Log.v(TAG, "defaultPosition: " + mDefaultPosition);
                        Log.v(TAG, "currentPosition: " + mCurrentPosition);
                    }

                    ViewGroup.LayoutParams params1 = selectedTextView.getLayoutParams();
                    params1.width = menuLayout.getWidth() - (menuLayout.getPaddingLeft() + menuLayout.getPaddingRight());
                    selectedTextView.setLayoutParams(params1);

                    int selectedPosition = mDefaultPosition;
                    if (selectedPosition < 0 || selectedPosition > mEntries.length - 1) {
                        selectedPosition = mEntries.length / 2;
                    }
                    setSelectedPosition(selectedPosition);
                }
            });
    }

    @SuppressWarnings ("unused")
    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setNextPosition(final int position) {
        if (OverMenuView.DEBUG) {
            Log.i(TAG, "setNextPosition: " + position);
        }
        mDefaultPosition = position;
    }

    @SuppressWarnings ("unused")
    public int getNextPosition() {
        return mDefaultPosition;
    }

    void updatePosition() {
        if (OverMenuView.DEBUG) {
            Log.i(TAG, "updatePosition");
        }
        setSelectedPosition(mCurrentPosition);
    }

    public void setSelectedPosition(int position) {
        if (null != mEntries) {
            if (OverMenuView.DEBUG) {
                Log.i(TAG, "setSelectedPosition: " + position);
            }
            position = clamp(position, 0, mEntries.length - 1);
            int topScroll = mMaxScroll;
            int translationY = topScroll - (position * mTextHeight);
            translationY = clamp(translationY, mMinScroll, mMaxScroll);
            menuLayout.setTranslationY(translationY);
            onPostScroll(menuLayout.getTranslationY());
        }
    }

    @SuppressWarnings ("unused")
    public int getSelectedPosition() {
        return mCurrentPosition;
    }

    private void onPostScroll(final float current) {
        if (null == mEntries) {
            return;
        }

        for (int i = 0; i < menuLayout.getChildCount(); i++) {
            View item = menuLayout.getChildAt(i);
            tempRect.set(item.getLeft(), (int) (item.getTop() + current), item.getRight(), (int) (item.getBottom() + current));
            if (tempRect.contains(mCenter.x, mCenter.y)) {
                updateSelected(i, (TextView) item);
                break;
            }
        }
    }

    private int mCurrentPosition = -1;
    private TextView mCurrentSelectedTextView;

    private void updateSelected(final int position, final TextView textView) {
        if (textView == mCurrentSelectedTextView) {
            return;
        }

        selectedTextView.setText(textView.getText());

        if (null != mCurrentSelectedTextView) {
            if (mAnimateItems) {
                showView(mCurrentSelectedTextView);
            } else {
                mCurrentSelectedTextView.setVisibility(View.VISIBLE);
            }
        }

        if (mAnimateItems) {
            hideView(textView);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }

        mCurrentSelectedTextView = textView;
        mDefaultPosition = mCurrentPosition = position;

        if (null != listener) {
            listener.onSelectionChanged(position);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCurrentPosition = -1;
        mCurrentSelectedTextView = null;
        menuLayout = null;
        selectedTextView = null;
    }

    void onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
        if (null != mEntries) {
            float current = menuLayout.getTranslationY();
            current += distanceY * 1.2f;
            current = clamp(current, mMinScroll, mMaxScroll);
            menuLayout.setTranslationY(current);
            onPostScroll(current);
        }
    }

    static int clamp(int currentValue, int minValue, int maxValue) {
        return Math.min(Math.max(currentValue, minValue), maxValue);
    }

    static float clamp(float currentValue, int minValue, int maxValue) {
        return Math.min(Math.max(currentValue, minValue), maxValue);
    }

    static void hideView(@NonNull final View view) {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(100);
        hideView(view, animation);
    }

    static void showView(@NonNull final View view) {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(100);
        showView(view, animation);
    }

    static void hideView(@NonNull final View view, @NonNull final Animation animation) {
        animation.setAnimationListener(
            new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {}

                @Override
                public void onAnimationEnd(final Animation animation) {
                    view.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(final Animation animation) { }
            });
        view.startAnimation(animation);
    }

    static void showView(@NonNull final View view, @NonNull final Animation animation) {
        animation.setAnimationListener(
            new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(final Animation animation) { }

                @Override
                public void onAnimationRepeat(final Animation animation) { }
            });
        view.startAnimation(animation);
    }
}
