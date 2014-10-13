package me.sdk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

public class MyListView extends ListView implements AbsListView.OnScrollListener {

    private static float SENSITIVITY = 0.75F;

    private Callback mCallback = null;
    private int mDelta = 0;

    public MyListView(final Context context) {
        super(context);
        init();
    }

    public MyListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_ALWAYS);
        setOnScrollListener(this);
    }

    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onTouchEvent(@NotNull final MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mCallback.overscroll(0);
            mDelta = 0;
        }
        return super.onTouchEvent(motionEvent);
    }

    private View mTrackedChild;
    private int mTrackedChildPrevPosition;
    private int mTrackedChildPrevTop;

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        ;
    }

    // uncharge and recharging
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (mTrackedChild == null) {
            if (getChildCount() > 0) {
                mTrackedChild = getChildAt(getChildCount() / 2);
                mTrackedChildPrevTop = mTrackedChild.getTop();
                mTrackedChildPrevPosition = getPositionForView(mTrackedChild);
            }
        }
        else {
            boolean childIsSafeToTrack = mTrackedChild.getParent() == this && getPositionForView(mTrackedChild) == mTrackedChildPrevPosition;
            if (childIsSafeToTrack) {
                final int top = mTrackedChild.getTop();
                final int delta = ((int) ((top - mTrackedChildPrevTop) * SENSITIVITY));
                // uncharge or recharging
                if (delta < 0 || mDelta > 0) {
                    mDelta += delta;
                    if (mCallback != null) {
                        mCallback.overscroll(mDelta);
                    }
                }
                mTrackedChildPrevTop = top;
            }
            else {
                mTrackedChild = null;
            }
        }
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        ;
//        if (getLastVisiblePosition() == totalItemCount - 1 && getChildAt(totalItemCount - 1).getBottom() <= getHeight()) {
//            mCallback.bottom();
//        }
    }

    // charges (and loads if >= 100)
    @Override
    protected boolean overScrollBy(final int deltaX, final int deltaY, final int scrollX, final int scrollY, final int scrollRangeX, final int scrollRangeY, final int maxOverScrollX, final int maxOverScrollY, final boolean isTouchEvent) {
        final boolean overscrolled = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        if (mCallback != null && isTouchEvent) {
            mDelta += deltaY * (-1 * SENSITIVITY);
            if (mDelta >= 100) {
                // loads
                mDelta = 100;
                if (getFirstVisiblePosition() == 0) {
                    mCallback.overscrollTop();
                }
                else {
                    mCallback.overscrollBottom();
                }
            }
            else {
                // charges
                mCallback.overscroll(mDelta);
            }
        }
        return overscrolled;
    }

    // TODO smoothScrollToPosition()

    public interface Callback {

        public void overscroll(final int n);
        public void overscrollTop();
        public void overscrollBottom();
        // TODO public void bottom();

    }

}
