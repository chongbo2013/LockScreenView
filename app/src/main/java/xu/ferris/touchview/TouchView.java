package xu.ferris.touchview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 *
 * http://blog.csdn.net/xiaanming/article/details/18311877
 * Created by ferris on 2016/3/11.
 */
public class TouchView extends FrameLayout {
    private final String TAG = "TouchView";
    protected int mTouchState;
    protected float mLastMotionX;
    protected float mLastMotionY;
    protected static final int INVALID_POINTER = -1;
    protected int mActivePointerId = INVALID_POINTER;
    private int mMaximumVelocity;
    public   int mMinimumVelocity ;
    public int mTouchSlop;
    private Context mContext;
    private VelocityTracker mVelocityTracker;
    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    //屏幕的一半
    private static final float screenHalfRadio=0.5f;
    protected long mAnimationTime = 200;
    protected long mFlingAnimationTime = 150;

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
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchState = TOUCH_STATE_REST;
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity= configuration.getScaledMinimumFlingVelocity();
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

        final float x = ev.getX(pointerIndex);
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        //如果大于最小移动阀值，就设置为滚动状态
        //并且更新上一次触摸位置
        if (yDiff > mTouchSlop&&yDiff>xDiff) {
            mTouchState = TOUCH_STATE_SCROLLING;
            mLastMotionY = y;
            mLastMotionX = x;
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
        acquireVelocityTrackerAndAddMovement(ev);
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) &&
                (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                }
                break;
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetTouchState();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                releaseVelocityTracker();
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        acquireVelocityTrackerAndAddMovement(event);
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final int pointerIndex = event.findPointerIndex(mActivePointerId);
                    if (pointerIndex == -1)
                        return true;

                    final float y = event.getY(pointerIndex);
                    final float deltaY = mLastMotionY - y;

                    final float x = event.getX(pointerIndex);

                    float translationYMin=-(getHeight()-tv_containt.getHeight());
                    float tanslationYMax=0;
                    float currentTranslationY=tv_containt.getTranslationY()+(int) -deltaY;
                    if(currentTranslationY>=translationYMin&&currentTranslationY<=tanslationYMax){
                        ViewHelper.setTranslationY(tv_containt, currentTranslationY);

                    }
                    mLastMotionY = y;
                    mLastMotionX = x;
                } else {
                    determineScrollingStart(event);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityY = (int) mVelocityTracker.getYVelocity(mActivePointerId);
                   completeMove(velocityY);
                }
                resetTouchState();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                releaseVelocityTracker();
                break;
        }

        return mTouchState == TOUCH_STATE_SCROLLING;
    }

    private void completeMove(int velocityY) {
        boolean isflingUp=false;
        boolean isflingDown=false;
        if(Math.abs(velocityY)>mMinimumVelocity){
            if(velocityY>0) {
                isflingDown=true;
            }else{
                isflingUp=true;
            }
        }
        float translationHalf=tv_containt.getHeight()-getHeight()*screenHalfRadio;
        float translationYMin=-(getHeight()-tv_containt.getHeight());
        float tanslationYMax=0;
        float currentTranslationY=tv_containt.getTranslationY();
        if(currentTranslationY==tanslationYMax||currentTranslationY==translationYMin){
            return;
        }
        if(isflingDown){
            ViewPropertyAnimator.animate(tv_containt)
                    .translationY(tanslationYMax)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(mFlingAnimationTime);
        }else if(isflingUp){
            //时间，可以根据， 距离顶部，和速度。来做处理。
            ViewPropertyAnimator.animate(tv_containt)
                    .translationY(translationYMin)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(mFlingAnimationTime);
        }else{
            if(currentTranslationY>=translationHalf){
                ViewPropertyAnimator.animate(tv_containt)
                        .translationY(tanslationYMax)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(mAnimationTime);
            }else{
                ViewPropertyAnimator.animate(tv_containt)
                        .translationY(translationYMin)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(mAnimationTime);
            }
        }
    }

}
