package xu.ferris.touchview;

import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CustomViewPage viewPager;

    PagerAdapter pagerAdapter=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<View>  viewList = new ArrayList<View>();
        TextView mTouchView=new TextView(this);
        mTouchView.setBackgroundResource(R.color.colorPrimary);
        viewList.add(mTouchView);

        TextView mTouchView2=new TextView(this);
        mTouchView2.setBackgroundResource(R.color.colorPrimaryDark);
        viewList.add(mTouchView2);
        TextView mTouchView3=new TextView(this);
        mTouchView3.setBackgroundResource(R.color.colorAccent);
        viewList.add(mTouchView3);

        viewPager= (CustomViewPage) findViewById(R.id.mCustomViewpage);
        viewPager.setOffscreenPageLimit(3);
        pagerAdapter= new TouchAdapter(viewList);
        viewPager.setAdapter(pagerAdapter);
    }



    public class TouchAdapter extends PagerAdapter {
        ArrayList<View>  viewList;

        public TouchAdapter(ArrayList  viewList){
            this.viewList=viewList;
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == arg1;
        }

        @Override
        public int getCount() {

            return viewList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(viewList.get(position));

        }

        @Override
        public int getItemPosition(Object object) {

            return super.getItemPosition(object);
        }



        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position),ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            return viewList.get(position);
        }

    };
    private final String TAG = "MainActivity";
    @Override
    public boolean onTouchEvent(MotionEvent ev) {



        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:

                Log.d(TAG,TAG+"--onTouchEvent--ACTION_DOWN");
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG,TAG+"--onTouchEvent--ACTION_MOVE");
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,TAG+"--onTouchEvent--ACTION_UP,ACTION_CANCEL");
                break;
        }
        return super.onTouchEvent(ev);
    }




}
