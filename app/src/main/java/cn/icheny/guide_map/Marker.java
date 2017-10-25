package cn.icheny.guide_map;

import android.widget.ImageView;

/**
 * 地图上的小标记图标
 * @author www.icheny.cn
 * @date 2017/10/17
 */

public class Marker {
    private float scaleX;//x坐标比例，用比例值来自适应缩放的地图
    private float scaleY;//y坐标比例
    private ImageView markerView;//标记图标
    private int imgSrcId;//标记图标资源id

    public Marker() {
    }

    public Marker(float scaleX, float scaleY, int imgSrcId) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.imgSrcId = imgSrcId;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setMarkerView(ImageView markerView) {
        this.markerView = markerView;
    }

    public int getImgSrcId() {
        return imgSrcId;
    }

    public void setImgSrcId(int imgSrcId) {
        this.imgSrcId = imgSrcId;
    }

    public ImageView getMarkerView() {
        return markerView;
    }
}
