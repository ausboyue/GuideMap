# GuideMap


GuideMap，一个模仿地图分布和浏览的android自定义组件。

## 组件结构
主要由以下3个部分组成：

- **MapView，一个自定义ImageView。负责地图的加载、缩放和移动显示，同时向父容器View传达这些信息。**

- **MapContainer，一个自定义ViewGroup。承载MapView等子View的显示，同时转达MapView加载、缩放和移动相关信息给MarkerView。**

- **Marker，一个实体类。携带自身定位参数以及一个用来显示标记图标的ImageView对象（叫做MarkerView）。**


> 具体实现请访问  [乘月网](http://www.icheny.cn/)  —— <a href="http://www.icheny.cn/android%E8%87%AA%E5%AE%9A%E4%B9%89%E5%AF%BC%E8%A7%88%E5%9C%B0%E5%9B%BE%E7%BB%84%E4%BB%B6%E4%B8%80/" target="_blank"> [ Android自定义导览地图组件 ]
 
-----------------

## 如何集成使用？

- 直接将MapView.java，MapContainer.java和Marker.java拷贝到合适的Android项目目录。

### demo示例

项目中Java代码使用示例：

``` java
/**
 * 使用Demo
 * @author www.icheny.cn
 * @date 2017/10/18
 */
public class MainActivity extends AppCompatActivity implements MapContainer.OnMarkerClickListner {
    MapContainer mMapContainer;
    ArrayList<Marker> mMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNavigationBarVisibility(false);
        setContentView(R.layout.activity_main);
        mMapContainer = (MapContainer) findViewById(R.id.mc_map);
        //这里用女神赵丽颖的照片作地图~~
        mMapContainer.getMapView().setImageResource(R.drawable.zhaoliyin);
        mMarkers = new ArrayList<>();
        mMarkers.add(new Marker(0.1f, 0.2f, R.drawable.location));
        mMarkers.add(new Marker(0.3f, 0.7f, R.drawable.location));
        mMarkers.add(new Marker(0.3f, 0.3f, R.drawable.location));
        mMarkers.add(new Marker(0.2f, 0.4f, R.drawable.location));
        mMarkers.add(new Marker(0.8f, 0.4f, R.drawable.location));
        mMarkers.add(new Marker(0.5f, 0.6f, R.drawable.location));
        mMarkers.add(new Marker(0.8f, 0.8f, R.drawable.location));
        mMapContainer.setMarkers(mMarkers);
        mMapContainer.setOnMarkerClickListner(this);
    }

    @Override
    public void onClick(View view, int position) {
        Toast.makeText(MainActivity.this, "你点击了第" + position + "个marker", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置导航栏显示状态
     *
     * @param visible
     */
    private void setNavigationBarVisibility(boolean visible) {
        int flag = 0;
        if (!visible) {
            flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(flag);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
}
```
项目中xxx_layout.xml代码使用示例：

``` xml
<?xml version="1.0" encoding="utf-8"?>
<cn.icheny.guide_map.MapContainer xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/mc_map"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:marker_anim_duration="1200"
    app:marker_height="94px"
    app:marker_width="66px" />
```
示例运行效果：

![GuideMap](http://media.icheny.cn/image/20171023130449400.gif)
