package cn.icheny.guide_map;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * 可手势缩放移动双击缩放ImageView
 *
 * @author www.icheny.cn
 * @date 2017/8/15
 */

public class MapView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener {

    private boolean isPicLoaded = false;//图片是否已加载
    float SCALE_MIN = 0.5f;//最小缩小比例值系数
    float SCALE_ADAPTIVE = 1.0f;//自适应ViewGroup(或屏幕)缩放比例值
    float SCALE_MID = 2.0f;//中间放大比例值系数，双击一次的放大值
    float SCALE_MAX = 5.0f;//最大放大比例值系数，双击两次的放大值
    private Matrix mScaleMatrix;//缩放矩阵
    private ScaleGestureDetector mScaleGestureDetector;//缩放手势探测测器
    private GestureDetector mGestureDetector;//手势探测器
    private boolean isAutoScaling = false;//是否处于自动缩放中,用于是否响应双击手势的flag
    private int mTouchSlop;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        mScaleMatrix = new Matrix();
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = initGestureDetector(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 初始化手势探测器
     *
     * @param context
     * @return GestureDetector
     */
    private GestureDetector initGestureDetector(Context context) {
        GestureDetector.SimpleOnGestureListener listner = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isAutoScaling) {//如果不在自动缩放
                    isAutoScaling = true;
                    float x = e.getX();//双击触点x坐标
                    float y = e.getY();//双击触点y坐标
                    float scale = getDrawableScale();
                    if (scale < SCALE_MID) {//当前缩放比例小于一级缩放比例
                        //一级放大
                        post(new AutoScaleTask(SCALE_MID, x, y));
                    } else if (scale >= SCALE_MID && scale < SCALE_MAX) {//当前缩放比例在一级缩放和二级缩放比例之间
                        //二级放大
                        post(new AutoScaleTask(SCALE_MAX, x, y));
                    } else if (scale == SCALE_MAX) {//当前缩放比例等于二级缩放比例
                        //缩小至自适应view比例
                        post(new AutoScaleTask(SCALE_ADAPTIVE, x, y));
                    } else {
                        isAutoScaling = false;
                    }
                }
                return super.onDoubleTap(e);
            }
        };
        return new GestureDetector(context, listner);
    }


    /**
     * 订阅者的onGlobalLayout函数
     */
    @Override
    public void onGlobalLayout() {
        if (!isPicLoaded) {
            Drawable drawable = getDrawable();
            if (null == drawable) {//图片不存在就继续监听
                return;
            }
            isPicLoaded = true;//图片存在，已加载完成，停止监听
            //获取图片固有的宽高（不是指本身属性:分辨率，因为android系统在加载显示图片前可能对其压缩）
            int iWidth = drawable.getIntrinsicWidth();
            int iHeight = drawable.getIntrinsicHeight();

            //获取当前View(ImageView)的宽高，即父View给予的宽高
            int width = getWidth();
            int height = getHeight();

            //对比图片宽高和当前View的宽高，针对性的缩放
            if (iWidth >= width && iHeight <= height) {//如果图片固宽大于View宽,固高小于View高，
                SCALE_ADAPTIVE = height * 1f / iHeight;   // 那么只需针对高度等比例放大图片（这里有别于查看大图的处理方式）
            } else if (iWidth <= width && iHeight >= height) {//固宽小于View宽,固高大于View高，针对宽度放大
                SCALE_ADAPTIVE = width * 1f / iWidth;
            } else if (iWidth >= width && iHeight >= height || iWidth <= width && iHeight <= height) {//固宽和固高都大于或都小于View的宽高，
                SCALE_ADAPTIVE = Math.max(width * 1f / iWidth, height * 1f / iHeight);//只取对宽和对高之间最小的缩放比例值（这里有别于查看大图的处理方式）
            }

            //先将图片移动到View中心位置
            mScaleMatrix.postTranslate((width - iWidth) * 1f / 2, (height - iHeight) * 1f / 2);
            //再对图片从View的中心点缩放
            mScaleMatrix.postScale(SCALE_ADAPTIVE, SCALE_ADAPTIVE, width * 1f / 2, height * 1f / 2);
            //执行偏移和缩放
            setImageMatrix(mScaleMatrix);
            onChangedListner.onChanged(getMatrixRect());

            //根据当前图片的缩放情况，重新调整图片的最大最小缩放值
            SCALE_MAX *= SCALE_ADAPTIVE;
            SCALE_MID *= SCALE_ADAPTIVE;
            SCALE_MIN *= SCALE_ADAPTIVE;
        }
    }

    /**
     * 缩放手势开始时调用该方法
     *
     * @param detector
     * @return
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        //返回为true，则缩放手势事件往下进行，否则到此为止，即不会执行onScale和onScaleEnd方法
        return true;
    }

    /**
     * 缩放手势进行时调用该方法
     * 缩放控制范围：SCALE_MIN——SCALE_MAX
     *
     * @param detector
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (getDrawable() == null) {
            return true;//没有图片就不用折腾了
        }
        //缩放因子(即将缩放的值)
        float scaleFactor = detector.getScaleFactor();
        //当前图片已缩放的值（如果onScale第一次被调用，scale就是自适应后的缩放值：SCALE_ADAPTIVE）
        float scale = getDrawableScale();
        //当前缩放值在最大放大值以内且手势检测缩放因子为缩小手势(小于1)，或当前缩放值在最小缩小值以内且缩放因子为放大手势，允许缩放
        if (scale <= SCALE_MAX && scaleFactor < 1 || scale >= SCALE_MIN && scaleFactor > 1) {
            //进一步考虑即将缩小后的缩放比例(scale*scaleFactor)低于规定SCALE_MIN-SCALE_MAX范围的最小值SCALE_MIN
            if (scale * scaleFactor < SCALE_MIN && scaleFactor < 1) {
                //强制锁定缩小后缩放比例为SCALE_MIN（scale*scaleFactor=SCALE_MIN）
                scaleFactor = SCALE_MIN / scale;
            }
            //进一步考虑即将放大后的缩放比例(scale*scaleFactor)高于规定SCALE_MIN-SCALE_MAX范围的最大值SCALE_MAX
            if (scale * scaleFactor > SCALE_MAX && scaleFactor > 1) {
                //强制锁定放大后缩放比例为SCALE_MAX（scale*scaleFactor=SCALE_MAX）
                scaleFactor = SCALE_MAX / scale;
            }
            //设定缩放值和缩放位置，这里缩放位置便是手势焦点的位置
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());

            //检查即将缩放后造成的留空隙和图片不居中的问题，及时调整缩放参数
            checkBoderAndCenter();

            //执行缩放
            setImageMatrix(mScaleMatrix);
            onChangedListner.onChanged(getMatrixRect());
        }
        return true;
    }

    /**
     * 缩放手势完成后调用该方法
     *
     * @param detector
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Drawable drawable = getDrawable();
        if (drawable == null) return;
        //当前缩放值
        float scale = getDrawableScale();
        //当前缩放值小于自适应缩放缩放比例，即图片小于View宽高
        if (scale < SCALE_ADAPTIVE) {
            post(new AutoScaleTask(SCALE_ADAPTIVE, getWidth() * 1f / 2, getHeight() * 1f));
        }
    }

    /**
     * 自动缩放任务
     */
    private class AutoScaleTask implements Runnable {
        float targetScale;//目标缩放值
        float x;//缩放焦点的x坐标
        float y;//缩放焦点的y坐标
        static final float TMP_AMPLIFY = 1.06f;//放大梯度
        static final float TMP_SHRINK = 0.94f;//缩小梯度
        float tmpScale = 1f;//缩小梯度

        public AutoScaleTask(float targetScale, float x, float y) {
            this.targetScale = targetScale;
            this.x = x;
            this.y = y;
            //当前缩放值小于目标缩放值，目标是放大图片
            if (getDrawableScale() < targetScale) {
                //设定缩放梯度为放大梯度
                tmpScale = TMP_AMPLIFY;
            } else {  //当前缩放值小于(等于可以忽略)目标缩放值，目标是缩小图片
                //设定缩放梯度为缩小梯度
                tmpScale = TMP_SHRINK;
            }
        }

        @Override
        public void run() {
            //设定缩放参数
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            //检查即将缩放后造成的留空隙和图片不居中的问题，及时调整缩放参数
            checkBoderAndCenter();
            setImageMatrix(mScaleMatrix);
            onChangedListner.onChanged(getMatrixRect());
            //当前缩放值
            float scale = getDrawableScale();
            //如果tmpScale>1即放大任务状态，且当前缩放值还是小于目标缩放值或
            // tmpScale<1即缩小任务状态，且当前缩放值还是大于目标缩放值就继续执行缩放任务
            if (tmpScale > 1 && scale < targetScale || scale > targetScale && tmpScale < 1) {
                post(this);
            } else {//缩放的略微过头了,需要强制设定为目标缩放值
                tmpScale = targetScale / scale;
                mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
                checkBoderAndCenter();
                setImageMatrix(mScaleMatrix);
                onChangedListner.onChanged(getMatrixRect());
                isAutoScaling = false;
            }
        }
    }

    /**
     * 通过Touch事件移动图片
     *
     * @param event
     */
    private void moveByTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE://手势移动
                RectF rect = getMatrixRect();
                if (rect.width() <= getWidth() && rect.height() <= getHeight()) {
                    //图片宽高小于等于View宽高，即图片可以完全显示于屏幕中，那就没必要拖动了
                    return;
                }
                //计算多个触点的中心坐标
                int x = 0;
                int y = 0;
                int pointerCount = event.getPointerCount();//获取触点数（手指数）
                for (int i = 0; i < pointerCount; i++) {
                    x += event.getX(i);
                    y += event.getY(i);
                }
                //得到最终的中心坐标
                x /= pointerCount;
                y /= pointerCount;

                //如果触点数（手指数）发生变化，需要重置上一次中心坐标和数量的参考值
                if (mLastPointCount != pointerCount) {
                    mLastX = x;
                    mLastY = y;
                    mLastPointCount = pointerCount;
                }
                int deltaX = x - mLastX;//X方向的位移
                int deltaY = y - mLastY;//Y方向的位移
                //如果可以拖拽
                if (isCanDrag(deltaX, deltaY)) {

                    //图片宽小于等于view宽，则X方向不需要移动
                    if (rect.width() <= getWidth()) {
                        deltaX = 0;
                    }
                    //图片高小于等于view高，则Y方向不需要移动
                    if (rect.height() <= getHeight()) {
                        deltaY = 0;
                    }
                    //完成缩放
                    mScaleMatrix.postTranslate(deltaX, deltaY);
                    checkBoderAndCenter();
                    setImageMatrix(mScaleMatrix);
                    onChangedListner.onChanged(getMatrixRect());
                }
                //交换中心坐标值，作为下次移动事件的参考值
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL://取消
            case MotionEvent.ACTION_UP://释放
                mLastPointCount = 0;//触点数置零，便于下次判断是否重置mLastX和mLastY
                break;
        }
    }

    //上一次触点中心坐标
    int mLastX;//上一次拖动图片的触点数（手指数）
    int mLastY;
    //上一次拖动图片的触点数（手指数）
    int mLastPointCount;

    /**
     * 处理缩放和移动后图片边界与屏幕有间隙或者不居中的问题
     */
    private void checkBoderAndCenter() {
        RectF rect = getMatrixRect();
        int width = getWidth();
        int height = getHeight();

        float deltaX = 0;//X轴方向偏移量
        float deltaY = 0;//Y轴方向偏移量

        //图片宽度大于View宽
        if (rect.width() >= width) {
            //图片左边坐标大于0，即左边有空隙
            if (rect.left > 0) {
                //向左移动rect.left个单位到View最左边,rect.left=0
                deltaX = -rect.left;
            }
            //图片右边坐标小于width，即右边有空隙
            if (rect.right < width) {
                //向右移动width - rect.left个单位到View最右边,rect.right=width
                deltaX = width - rect.right;
            }
        }
        //图片高度大于View高，同理
        if (rect.height() >= height) {
            //图片上面坐标大于0，即上面有空隙
            if (rect.top > 0) {
                //向上移动rect.top个单位到View最上边,rect.top=0
                deltaY = -rect.top;
            }
            //图片下面坐标小于height，即下面有空隙
            if (rect.bottom < height) {
                //向下移动height - rect.bottom个单位到View最下边,rect.bottom=height
                deltaY = height - rect.bottom;
            }
        }

        //图片宽度小于View宽
        if (rect.width() < width) {
            //计算需要移动到X方向View中心的距离
            deltaX = width * 1f / 2 - rect.right + rect.width() * 1f / 2;
        }

        //图片高度小于View高度
        if (rect.height() < height) {
            //计算需要移动到Y方向View中心的距离
            deltaY = height * 1f / 2 - rect.bottom + rect.height() * 1f / 2;
        }
        //设置移动参数
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 根据当前图片矩阵变换成的四个角的坐标，即left,top,right,bottom
     *
     * @return
     */
    private RectF getMatrixRect() {
        RectF rect = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        mScaleMatrix.mapRect(rect);
        return rect;
    }


    /**
     * 获取当前已经缩放的比例
     * 因为x方向和y方向比例相同，所以只返回x方向的缩放比例即可
     */
    private float getDrawableScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mScaleGestureDetector != null) {
            //绑定缩放手势探测器,由其处理touch事件
            mScaleGestureDetector.onTouchEvent(event);
        }
        if (mGestureDetector != null) {
            //绑定手势探测器,由其处理touch事件
            mGestureDetector.onTouchEvent(event);
        }
        //不在自动缩放中才可以拖动图片（这个判断可有可无，根据需求来）
        if (!isAutoScaling) {
            //绑定touch事件，处理移动图片逻辑
            moveByTouchEvent(event);
        }
        return true;
    }

    /**
     * 是否可以移动图片
     *
     * @param deltaX
     * @param deltaY
     */
    private boolean isCanDrag(int deltaX, int deltaY) {
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY) >= mTouchSlop;
    }

    private OnMapStateChangedListner onChangedListner;//地图状态变化监听对象

    public void setOnMapStateChangedListner(OnMapStateChangedListner l) {
        onChangedListner = l;
    }

    /**
     * 监听地图自适应屏幕缩放、手势移动以及缩放的状态变化接口
     */
    public interface OnMapStateChangedListner {
        void onChanged(RectF rectF);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //订阅布局监听
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //取消订阅
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
