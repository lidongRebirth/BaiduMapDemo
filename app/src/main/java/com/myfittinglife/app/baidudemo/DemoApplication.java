package com.myfittinglife.app.baidudemo;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

/**
 * 作者    LD
 * 时间    2018/10/30 11:24
 * 描述    初始化百度地图SDK各组件
 * 注意：在SDK各功能组件使用之前都需要调用
        SDKInitializer.initialize(getApplicationContext());，因此我们建议该方法放在Application的初始化方法中
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
}
