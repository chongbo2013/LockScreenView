package xu.ferris.touchview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * http://blog.csdn.net/xiaanming/article/details/18311877
 * Created by Administrator on 2016/3/11.
 */
public class TouchView extends FrameLayout {

    private final String TAG = "TouchView";

    private OverScroller mScroller;


    protected int mTouchState;

    protected float mLastMotionX;
    protected float mLastMotionY;

    protected static final int INVALID_POINTER = -1;
    protected int mActivePointerId = INVALID_POINTER;

    private int mMaximumVelocity;


    public static final int MIN_SNAP_VELOCITY = 300;

    public int mTouchSlop;
    private Context mContext;

    private VelocityTracker mVelocityTracker;
    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;



    public TouchView(Context context) {
        super(context);
        init(context);
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(Context context) {
        this.mContext = context;
        mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mTouchState = TOUCH_STATE_REST;
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }



    private TextView tv_containt;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_containt= (TextView) findViewById(R.id.tv_containt);
    }


    //添加按下速度检测
    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    //释放速度检测
    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    //确定是否要滚动
    protected void determineScrollingStart(MotionEvent ev) {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1)
            return;

        final float y = ev.getY(pointerIndex);
        final int yDiff = (int) Math.abs(y - mLastMotionY);
        //如果大于最小移动阀值，就设置为滚动状态
        //并且更新上一次触摸位置
        if (yDiff > mTouchSlop) {
            mTouchState = TOUCH_STATE_SCROLLING;
            mLastMotionY = y;
        }
    }

    //重置触摸状态
    private void resetTouchState() {
        releaseVelocityTracker();
        mTouchState = TOUCH_STATE_REST;
        mActivePointerId = INVALID_POINTER;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        //添加按下速度检测
        acquireVelocityTrackerAndAddMovement(ev);

        //判断下，如果没有子类，不拦截事件
        if (getChildCount() <= 0)
            return super.onInterceptTouchEvent(ev);

        final int action = ev.getAction();
        //如果处于移动状态，并且是正在滚动，则直接拦截事件
        if ((action == MotionEvent.ACTION_MOVE) &&
                (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }


        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                //判断活动的手指是有效的，则进行判断其是否符合最小移动距离，才进行拦截
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                }
                Log.d(TAG,TAG+"--onInterceptTouchEvent--ACTION_MOVE");
                break;

            case MotionEvent.ACTION_DOWN: {
                Log.d(TAG,TAG+"--onInterceptTouchEvent--ACTION_DOWN");
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                //按下的时候，始终保持，只有一个手指按下
                mActivePointerId = ev.getPointerId(0);

                //如果滚动已经结束，就重置下触摸状态，停止动画
                if (mScroller.isFinished()) {
                    mTouchState = TOUCH_STATE_REST;
                    mScroller.abortAnimation();
                }

                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //当手指抬起的时候，进行各种状态重置
                Log.d(TAG,TAG+"--onInterceptTouchEvent--ACTION_UP,ACTION_CANCEL");
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //其他手指如果抬起来，是是否速度检测器
                Log.d(TAG,TAG+"--onInterceptTouchEvent--ACTION_POINTER_UP");
                releaseVelocityTracker();
                break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }

    private final float DELTA_RATIO = 2; //数值越大, 头部空白下拉越慢. 值为1为完全跟手.

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);


        if (getChildCount() <= 0)
            return super.onTouchEvent(event);

        acquireVelocityTrackerAndAddMovement(event);

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
//                int mScrollY = getScrollY();
//                int maxY = desireHeight - getHeight();
//                if (!(mScrollY > maxY||mScrollY < 0)&&!mScroller.isFinished()) {
//                    mScroller.abortAnimation();
//                }
                Log.d(TAG,TAG+"--onTouchEvent--ACTION_POINTER_UP");
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();

                mActivePointerId = event.getPointerId(0);
                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG,TAG+"--onTouchEvent--ACTION_MOVE");
                if (mTouchState == TOUCH_STATE_SCROLLING) {

                    final int pointerIndex = event.findPointerIndex(mActivePointerId);

                    if (pointerIndex == -1)
                        return true;

                    final float y = event.getY(pointerIndex);
                    final float deltaY = mLastMotionY - y;


//                    scrollBy(0, (int) deltaY);

                    ViewHelper.setTranslationY(tv_containt, tv_containt.getTranslationY()+(int) -deltaY);

                    mLastMotionY = y;

                } else {
                    determineScrollingStart(event);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,TAG+"--onTouchEvent--ACTION_UP,ACTION_CANCEL");
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityY = (int) mVelocityTracker.getYVelocity(mActivePointerId);
//                    completeMove(velocityY);
                }
                resetTouchState();

                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG,TAG+"--onTouchEvent--ACTION_POINTER_UP");
                releaseVelocityTracker();
                break;
        }

        return mTouchState == TOUCH_STATE_SCROLLING;
    }


    private void completeMove(float velocityY) {
        int desireHeight = 0;
        int mScrollY = getScrollY();
        int maxY = desireHeight - getHeight();

        if (mScrollY > maxY) {
            // 超出了下边界，弹回
            mScroller.startScroll(0, mScrollY, 0, maxY - mScrollY);
            invalidate();
        } else if (mScrollY < 0) {
            // 超出了上边界，弹回
            mScroller.startScroll(0, mScrollY, 0, -mScrollY);
            invalidate();
        } else if (Math.abs(velocityY) >= MIN_SNAP_VELOCITY && maxY > 0) {

            if (velocityY > 0) {
                //上滑，判断距离底部的距离
                Toast.makeText(getContext(), "velocityY=" + velocityY + "，上滑", Toast.LENGTH_SHORT).show();
                int distance = desireHeight - mScrollY - getHeight();
                mScroller.fling(0, mScrollY, 0, (int) -velocityY, 0, 0, 0, maxY);

                invalidate();
            } else {
                //下滑，判断距离顶部的距离
                int distance = mScrollY;
                mScroller.fling(0, mScrollY, 0, (int) -velocityY, 0, 0, 0, maxY);
                Toast.makeText(getContext(), "velocityY=" + velocityY + "，下滑", Toast.LENGTH_SHORT).show();
            }


        }

    }

//    @Override
//    public void computeScroll() {
//        if (mScroller.computeScrollOffset()) {
//            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
//            invalidate();
//        }
//    }
}
