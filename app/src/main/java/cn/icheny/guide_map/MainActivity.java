package cn.icheny.guide_map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 使用Demo
 *
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
        mMapContainer = findViewById(R.id.mc_map);
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

