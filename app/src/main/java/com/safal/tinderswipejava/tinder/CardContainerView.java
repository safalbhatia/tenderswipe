package com.safal.tinderswipejava.tinder;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;

import com.safal.tinderswipejava.R;

public class CardContainerView extends FrameLayout {

    private CardStackOption option;

    private float viewOriginX = 0f;
    private float viewOriginY = 0f;
    private float motionOriginX = 0f;
    private float motionOriginY = 0f;
    private boolean isDragging = false;
    private boolean isDraggable = true;
    private boolean isSwiping = false;

    private ViewGroup contentContainer = null;
    private ViewGroup overlayContainer = null;
    private View leftOverlayView = null;
    private View rightOverlayView = null;
    private View bottomOverlayView = null;
    private View topOverlayView = null;

    private long timeOnPressed;

    private ContainerEventListener containerEventListener = null;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (containerEventListener != null) {
                containerEventListener.onContainerClicked();
            }
            return true;
        }
    };
    private GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);

    public interface ContainerEventListener {
        void onContainerDragging(float percentX, float percentY);
        void onContainerSwiped(CardContainerView container, Point point, SwipeDirection direction);
        void onContainerMovedToOrigin();
        void onContainerClicked();
    }

    public CardContainerView(Context context) {
        this(context, null);
    }

    public CardContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflate(getContext(), R.layout.card_frame, this);
        contentContainer = (ViewGroup) findViewById(R.id.card_frame_content_container);
        overlayContainer = (ViewGroup) findViewById(R.id.card_frame_overlay_container);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (!option.isSwipeEnabled || !isDraggable) {
            return true;
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                if (getParent() == null) break;
                getParent().getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                if (getParent() == null) break;
                getParent().getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (getParent() == null) break;
                getParent().getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
        }

        return true;
    }

    private void handleActionDown(MotionEvent event) {
        motionOriginX = event.getRawX();
        motionOriginY = event.getRawY();
        Log.d("SwipeView Raw x", String.valueOf(event.getRawX()));
        timeOnPressed = System.currentTimeMillis();
    }

    public boolean isDragging() {
        return isDragging;
    }

    public boolean isSwiping() {
        return isSwiping;
    }

    public void setSwiping() {
        isSwiping = true;
    }

    private void handleActionUp(MotionEvent event) {
        if (isDragging) {
            isDragging = false;

            float motionCurrentX = event.getRawX();
            float motionCurrentY = event.getRawY();
            long timeOnReleased = System.currentTimeMillis();

            Log.d("Time diff", String.valueOf(timeOnReleased - timeOnPressed));
            Log.d("X Diff", String.valueOf(Math.abs(motionCurrentX - motionOriginX)));


            Point point = Util.getTargetPoint(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY);
            Quadrant quadrant = Util.getQuadrant(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY);
            double radian = Util.getRadian(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY);
            double degree = 0;
            SwipeDirection direction = null;
            switch (quadrant) {
                case TopLeft:
                    degree = Math.toDegrees(radian);
                    degree = 180 - degree;
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < -0.5) {
                        direction = SwipeDirection.Left;
                    } else {
                        direction = SwipeDirection.Top;
                    }
                    break;
                case TopRight:
                    degree = Math.toDegrees(radian);
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < 0.5) {
                        direction = SwipeDirection.Top;
                    } else {
                        direction = SwipeDirection.Right;
                    }
                    break;
                case BottomLeft:
                    degree = Math.toDegrees(radian);
                    degree = 180 + degree;
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < -0.5) {
                        direction = SwipeDirection.Left;
                    } else {
                        direction = SwipeDirection.Bottom;
                    }
                    break;
                case BottomRight:
                    degree = Math.toDegrees(radian);
                    degree = 360 - degree;
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < 0.5) {
                        direction = SwipeDirection.Bottom;
                    }else{
                        direction = SwipeDirection.Right;
                    }
                    break;
            }

            float percent = 0f;
            if (direction == SwipeDirection.Left || direction == SwipeDirection.Right) {
                percent = getPercentX();
            } else {
                percent = getPercentY();
            }

            if (Math.abs(percent) > option.swipeThreshold || (timeOnReleased - timeOnPressed < 210 && Math.abs(motionOriginX - motionCurrentX) > 215)) {
                if (option.swipeDirection.contains(direction)) {
                    isSwiping = true;
                    if (containerEventListener != null) {
                        containerEventListener.onContainerSwiped(this, point, direction);
                    }
                } else {
                    moveToOrigin();
                    if (containerEventListener != null) {
                        containerEventListener.onContainerMovedToOrigin();
                    }
                }
            } else {
                moveToOrigin();
                if (containerEventListener != null) {
                    containerEventListener.onContainerMovedToOrigin();
                }
            }
        }

        motionOriginX = event.getRawX();
        motionOriginY = event.getRawY();
    }

    private void handleActionMove(MotionEvent event) {
        isDragging = true;

        updateTranslation(event);
        updateRotation();
        updateAlpha();

        if (containerEventListener != null) {
            containerEventListener.onContainerDragging(getPercentX(), getPercentY());
        }
    }

    private void updateTranslation(MotionEvent event) {
        ViewCompat.setTranslationX(this, viewOriginX + event.getRawX() - motionOriginX);
        ViewCompat.setTranslationY(this, viewOriginY + event.getRawY() - motionOriginY);
    }

    private void updateRotation() {
        ViewCompat.setRotation(this, getPercentX() * 20);
    }

    private void updateAlpha() {
        float percentX = getPercentX();
        float percentY = getPercentY();

        //ge??i??i ????z??m
        percentX = percentX * 2;

        if (option.swipeDirection.equals(SwipeDirection.FREEDOM) ||
                option.swipeDirection.equals(SwipeDirection.FREEDOM_NO_BOTTOM)) {
            if (Math.abs(percentX) > Math.abs(percentY)){
                if (percentX < 0) {
                    showLeftOverlay();
                } else {
                    showRightOverlay();
                }
                setOverlayAlpha(Math.abs(percentX));
            }else{
                if (percentY < 0) {
                    showTopOverlay();
                } else {
                    showBottomOverlay();
                }
                setOverlayAlpha(Math.abs(percentY));
            }
        } else if (option.swipeDirection.equals(SwipeDirection.HORIZONTAL)) {
            if (percentX < 0) {
                showLeftOverlay();
            } else {
                showRightOverlay();
            }
            setOverlayAlpha(Math.abs(percentX));
        } else if (option.swipeDirection.equals(SwipeDirection.VERTICAL)) {
            if (percentY < 0) {
                showTopOverlay();
            } else {
                showBottomOverlay();
            }
            setOverlayAlpha(Math.abs(percentY));
        }
    }

    private void moveToOrigin() {
        animate().translationX(viewOriginX)
                .translationY(viewOriginY)
                .setDuration(300L)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .setListener(null)
                .start();
    }

    public void setContainerEventListener(ContainerEventListener listener) {
        this.containerEventListener = listener;
        //viewOriginX = ViewCompat.getTranslationX(this);
        //viewOriginY = ViewCompat.getTranslationY(this);
    }

    public void setCardStackOption(CardStackOption option) {
        this.option = option;
    }

    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public void reset() {
        ViewCompat.setAlpha(contentContainer, 1f);
        ViewCompat.setAlpha(overlayContainer, 0f);
        isDragging = false;
        isSwiping = false;
    }

    public ViewGroup getContentContainer() {
        return contentContainer;
    }

    public ViewGroup getOverlayContainer() {
        return overlayContainer;
    }

    public void setOverlay(int left, int right, int bottom, int top) {
        if (leftOverlayView != null) {
            overlayContainer.removeView(leftOverlayView);
        }
        if (left != 0) {
            try {
                leftOverlayView = LayoutInflater.from(getContext()).inflate(left, overlayContainer, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(overlayContainer != null && leftOverlayView != null){
                overlayContainer.addView(leftOverlayView);
                ViewCompat.setAlpha(leftOverlayView, 0f);
            }

        }

        if (rightOverlayView != null) {
            overlayContainer.removeView(rightOverlayView);
        }
        if (right != 0) {
            rightOverlayView = LayoutInflater.from(getContext()).inflate(right, overlayContainer, false);
            overlayContainer.addView(rightOverlayView);
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            overlayContainer.removeView(bottomOverlayView);
        }
        if (bottom != 0) {
            bottomOverlayView = LayoutInflater.from(getContext()).inflate(bottom, overlayContainer, false);
            overlayContainer.addView(bottomOverlayView);
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }

        if (topOverlayView != null) {
            overlayContainer.removeView(topOverlayView);
        }
        if (top != 0) {
            topOverlayView = LayoutInflater.from(getContext()).inflate(top, overlayContainer, false);
            overlayContainer.addView(topOverlayView);
            ViewCompat.setAlpha(topOverlayView, 0f);
        }
    }

    public void setOverlayAlpha(float alpha) {
        ViewCompat.setAlpha(overlayContainer, alpha);
    }

    public void showLeftOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 1f);
        }
        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }
        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }
        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f);
        }
    }

    public void showRightOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }

        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f);
        }

        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 1f);
        }
    }

    public void showBottomOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 1f);
        }

        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f);
        }

        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }
    }


    public void showTopOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }

        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 1f);
        }

        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }
    }

    public float getViewOriginX() {
        return viewOriginX;
    }

    public float getViewOriginY() {
        return viewOriginY;
    }

    public float getPercentX() {
        float percent = 2f * (ViewCompat.getTranslationX(this) - viewOriginX) / getWidth();
        if (percent > 1) {
            percent = 1;
        }
        if (percent < -1) {
            percent = -1;
        }
        return percent;
    }

    public float getPercentY() {
        float percent = 2f * (ViewCompat.getTranslationY(this) - viewOriginY) / getHeight();
        if (percent > 1) {
            percent = 1;
        }
        if (percent < -1) {
            percent = -1;
        }
        return percent;
    }

}
