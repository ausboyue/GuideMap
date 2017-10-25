package cn.icheny.guide_map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.List;

/**
 * 地图界面承载容器，ViewGroup
 * @author www.icheny.cn
 * @date 2017/10/18
 */

public class MapContainer extends ViewGroup implements MapView.OnMapStateChangedListner {
    /**
     * 这个Flag标记是为了不让ViewGroup不断地绘制子View，
     * 导致不断地重置，  因为之后MapView的缩放，
     * 移动以及markerView的移动等所涉及的重绘都是由逻辑代码控制好了
     */
    private boolean isFirstLayout = true;
    private Context mContext;//上下文
    private int MARKER_ANIM_DURATION;//动画时间
    private int MARKER_WIDTH; //marker宽度
    private int MARKER_HEIGHT; //marker高度
    private boolean isAnimFinished = false;
    private MapView mMapView;//地图View
    private List<Marker> mMarkers;//marker集合

    public MapContainer(Context context) {
        this(context, null);
    }

    public MapContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttributes(attrs);
        initMapView();
    }

    /**
     * 初始化地图属性配置
     * @param attrs
     */
    private void initAttributes(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.MapView);
        MARKER_WIDTH = a.getDimensionPixelOffset(R.styleable.MapView_marker_width, 30);//默认30px
        MARKER_HEIGHT = a.getDimensionPixelOffset(R.styleable.MapView_marker_height, 60);//默认60px
        MARKER_ANIM_DURATION = a.getInteger(R.styleable.MapView_marker_anim_duration, 1200);//默认1.2完成下落动画
        a.recycle();
    }

    /**
     * 初始化MapView并添加到MapContainer中
     */
    private void initMapView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mMapView = new MapView(mContext);
        mMapView.setOnMapStateChangedListner(this);
        addView(mMapView);
        mMapView.setLayoutParams(params);
    }


    /**
     * 地图自适应屏幕缩放、手势移动以及缩放的状态变化触发的方法
     *
     * @param rectF 地图Rect矩形
     */
    @Override
    public void onChanged(RectF rectF) {
        if (mMarkers == null) {
            return;
        }
        float pWidth = rectF.width();//地图宽度
        float pHeight = rectF.height();//地图高度
        float pLeft = rectF.left;//地图左边x坐标
        float pTop = rectF.top;//地图顶部y坐标

        Marker marker = null;
        for (int i = 0, size = mMarkers.size(); i < size; i++) {

            marker = mMarkers.get(i);

           /* 计算marker显示的矩形坐标，定位坐标以marker的中下边为基准*/
            int left = roundValue(pLeft + pWidth * marker.getScaleX() - MARKER_WIDTH * 1f / 2);
            int top = roundValue(pTop + pHeight * marker.getScaleY() - MARKER_HEIGHT);
            int right = roundValue(pLeft + pWidth * marker.getScaleX() + MARKER_WIDTH * 1f / 2);
            int bottom = roundValue(pTop + pHeight * marker.getScaleY());

            if (!isAnimFinished) {//下落动画，第一次状态改变会调用，即图片自适应屏幕缩放后会调用
                TranslateAnimation ta = new TranslateAnimation(0, 0, -top, 0);
                ta.setDuration(MARKER_ANIM_DURATION);
                marker.getMarkerView().startAnimation(ta);
            }

            //移动marker
            marker.getMarkerView().layout(left, top, right, bottom);
        }
        isAnimFinished = true;
    }


    /**
     * 获取MapView对象，建议仅仅拿来加载图片资源
     *
     * @return
     */
    public MapView getMapView() {
        return this.mMapView;
    }


    /**
     * 传入marker集合
     *
     * @param markers
     */
    public void setMarkers(List<Marker> markers) {
        this.mMarkers = markers;

        /*移除上次传入的所有marker(即移除已显示的markers),至于要不要移除看需求，这里仅仅提供方法*/
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            Object tag = child.getTag(R.id.is_marker);
            if (tag instanceof Boolean && (((Boolean) tag).booleanValue())) {
                //确认当前child是markerView即从ViewGroup中移除
                removeView(child);
            }
        }
        //初始化markers
        initMarkers();
    }

    /**
     * 初始化所有的标记图标（marker）
     */
    private void initMarkers() {
        if (mMarkers == null) {
            return;
        }

        //markerview布局参数，设定宽高
        LayoutParams params = new LayoutParams(MARKER_WIDTH, MARKER_HEIGHT);

        /* 遍历所有marker对象并新建ImageView对象markerView,作相关赋值*/
        for (int i = 0, size = mMarkers.size(); i < size; i++) {

            Marker marker = mMarkers.get(i);
            final ImageView markerView = new ImageView(mContext);
            marker.setMarkerView(markerView);
            addView(markerView);

            //设定tag标识，便于根据tag判定是否是markerView
            markerView.setTag(R.id.is_marker, true);
            markerView.setLayoutParams(params);
            markerView.setImageResource(marker.getImgSrcId());
            final int position = i;
            markerView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onMarkerClickListner != null) {
                        //点击事件交给业务类处理
                        onMarkerClickListner.onClick(markerView, position);
                    }
                }
            });
        }
    }

    private OnMarkerClickListner onMarkerClickListner;//maker被点击监听接口对象

    /**
     * 传入需要处理marker点击事件的业务类对象
     *
     * @param l
     */
    public void setOnMarkerClickListner(OnMarkerClickListner l) {
        this.onMarkerClickListner = l;
    }

    /**
     * maker被点击监听接口,便于回调给业务类处理事件
     */
    public interface OnMarkerClickListner {
        void onClick(View view, int position);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            if (isFirstLayout) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
                }
            }
        }
    }

    /**
     * 此方法返回参数的最接近的整数，目的是为了减小误差
     * 否则marker容易变大或变小，坐标偏差也会越来越大，
     * 毕竟markerView.layout只能传入整数
     *
     * @param value
     * @return
     */
    private int roundValue(float value) {
        return Math.round(value);
    }

}