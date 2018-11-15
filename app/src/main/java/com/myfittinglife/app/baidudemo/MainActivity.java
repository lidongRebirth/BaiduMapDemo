package com.myfittinglife.app.baidudemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者    LD
 * 时间    10.30
 * 描述    获取地理位置信息，并将自己移动到地图的相对位置
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MapView mapView = null;
    private BaiduMap baiduMap;

    private LocationClient mlocationClient;      //定位服务的客户端，只支持在主线程中调用
    private TextView tv_location;
    private Button btn_request,btn_move,btn_jump,btn_jump_navi;
    private static String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean isFirstLocate=true;     //是否是第一次定位
    private BDLocation myBDLocation;

    private EditText longitude,latitude;

    private static final String TAG = "ceshi";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.baidumap);

        baiduMap = mapView.getMap();         //获取baidumap
        baiduMap.setMyLocationEnabled(true);//我显示在地图上


        tv_location = findViewById(R.id.tv_location);
        btn_request = findViewById(R.id.btn_request);
        btn_move = findViewById(R.id.btn_move);
        btn_jump = findViewById(R.id.btn_jump);
        btn_jump_navi = findViewById(R.id.btn_jump_navi);
        btn_request.setOnClickListener(this);
        btn_move.setOnClickListener(this);
        btn_jump.setOnClickListener(this);
        btn_jump_navi.setOnClickListener(this);

        longitude = findViewById(R.id.et_Longitude);
        latitude = findViewById(R.id.et_latitude);


        mlocationClient = new LocationClient(getApplicationContext());
        mlocationClient.registerLocationListener(new MyLocationListener());


    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request:          //获取信息
                permissionsChecked();       //权限申请
                break;
            case R.id.btn_move:                                 //移动到指定经纬度位置
                if(TextUtils.isEmpty(longitude.getText())||TextUtils.isEmpty(latitude.getText())){
                    Toast.makeText(getApplicationContext(),"请输入经纬度信息",Toast.LENGTH_SHORT).show();
                }else {
                    BDLocation bdLocation = new BDLocation();
                    bdLocation.setLongitude(Double.parseDouble(longitude.getText().toString()));
                    bdLocation.setLatitude(Double.parseDouble(latitude.getText().toString()));
                    move(bdLocation);
                }
                break;
            case R.id.btn_jump://跳转至百度地图客户端
                if(isInstallByread("com.baidu.BaiduMap")){
                    Toast.makeText(getApplicationContext(),"已安装百度地图",Toast.LENGTH_SHORT).show();
                    try {
                        Intent i = new Intent();
                        // 展示地图
                        i.setData(Uri.parse("baidumap://map/show?center="+latitude.getText().toString()+","+longitude.getText().toString()+"+&zoom=8&traffic=on&bounds="+latitude.getText().toString()+","+longitude.getText().toString()+","+latitude.getText().toString()+","+longitude.getText().toString()+"&src=andr.baidu.openAPIdemo"));
                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"请安装百度地图",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_jump_navi:        //跳转并导航
                initPopWindow();
                break;
            default:
                break;
        }
    }
    //判断是否安装某应用
    private boolean isInstallByread(String packageName) {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);        //获取已安装程序的包信息
        List<String> packageNames = new ArrayList<String>();
        if(packageInfos!=null){
            for(int i = 0;i<packageInfos.size();i++){
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有为true，无为false
       return packageNames.contains(packageName);
    }
    //权限申请
    public  void permissionsChecked(){

        //针对android8.0的开启位置服务申请
//        if(!isLocationEnabled()){
//            Toast.makeText(getApplicationContext(),"请开启位置服务",Toast.LENGTH_SHORT).show();
//            //*跳转到系统设置来开启位置服务
//            Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//        }
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //安卓8.0统一权限组的也需要单独申请，但一个组的只会弹一个框
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String []permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
            Log.i(TAG, "setPermissions: 未全部申请");
        }else {
            Log.i(TAG, "setPermissions: 已全部申请");
            if(isLocationEnabled()){
                requestLocation();
            }else {
                Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
    }
    /**
     * 判断定位服务是否开启,进入系统设置里来设置
     * @return true 表示开启
     */
    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                Log.i(TAG, "onRequestPermissionsResult: "+grantResults.length);
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(isLocationEnabled()){
                        requestLocation();
                    }else {
                        Toast.makeText(getApplicationContext(),"请开启位置服务",Toast.LENGTH_SHORT).show();
                        Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"发生未知错误",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //发起请求
    public void requestLocation() {
        initLocation();
        mlocationClient.start();
        Log.i(TAG, "requestLocation: 发起请求");
    }

    public void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);           //每隔5秒扫描一次
        option.setIsNeedAddress(true);      //需要具体的位置信息
        option.setCoorType("bd09ll");       //设置此才会在自己的位置中来，才会准确!!!!
        mlocationClient.setLocOption(option);
    }
    //将地图移动自己的位置上来
    public void navigateTo(){
        if(isFirstLocate){
            LatLng latLng=new LatLng(myBDLocation.getLatitude(),myBDLocation.getLongitude());       //存放经纬度
            //以下部分地理信息的存储和地图的更新是和课本不一样的地方
            MapStatus.Builder builder=new MapStatus.Builder();      //建立地图状态构造器
            builder.target(latLng).zoom(16f);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            isFirstLocate=false;
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(myBDLocation.getLatitude());
        locationBuilder.longitude(myBDLocation.getLongitude());
        MyLocationData locationData=locationBuilder.build();//build方法用来生成一个MyLocationData实例
        baiduMap.setMyLocationData(locationData);
    }
    //移动到指定位置
    public void move(BDLocation bdLocation){
        Toast.makeText(getApplicationContext(),"移动",Toast.LENGTH_SHORT).show();
        LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());       //存放经纬度
        //以下部分地理信息的存储和地图的更新是和课本不一样的地方
        MapStatus.Builder builder=new MapStatus.Builder();      //建立地图状态构造器
        builder.target(latLng).zoom(16f);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData locationData=locationBuilder.build();//build方法用来生成一个MyLocationData实例
        baiduMap.setMyLocationData(locationData);
    }
    //监听器
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: 线程中赋值");
                    tv_location.append("经度：" + bdLocation.getLongitude()+"\n" + "纬度：" + bdLocation.getLatitude()+"\n"+"国家："+bdLocation.getCountry()+"\n"+"省："+bdLocation.getProvince()+"\n"+"市"+bdLocation.getCity()+"\n"+"区/县:"+bdLocation.getDistrict()+"\n"+"街道："+bdLocation.getStreet()+"\n"+"详细地址信息："+bdLocation.getAddrStr());
                    myBDLocation = bdLocation;
                }
            });
            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation||bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo();
            }
        }
    }

    //*-------------------------------------------------------------------------------
    //绘制悬浮框
    public void initPopWindow() {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.location_popwindow, null, false);
        Button btnBaidu = view.findViewById(R.id.btn_baidu);
        Button btnGaode = view.findViewById(R.id.btn_gaode);
        Button btnTencent = view.findViewById(R.id.btn_tencent);
        View view1 = view.findViewById(R.id.view);
        final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        //点击外面popupWindow消失
        popWindow.setOutsideTouchable(true);

        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
            }
        });

        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //要为popWindow设置一个背景才有效
        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        popWindow.setAnimationStyle(R.style.anim_menu_bottombar);

        //百度
        btnBaidu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInstallByread("com.baidu.BaiduMap")) {
                    if(TextUtils.isEmpty(longitude.getText())||TextUtils.isEmpty(latitude.getText())){
                        Toast.makeText(getApplicationContext(), "请输入经纬度", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent i1 = new Intent();
                        //打开App汽车导航
                        i1.setData(Uri.parse("baidumap://map/navi?location="+Double.parseDouble(latitude.getText().toString())+","+Double.parseDouble(longitude.getText().toString())+"&src=andr.baidu.openAPIdemo"));
                        startActivity(i1);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请安装百度地图", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //高德
        btnGaode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isInstallByread("com.autonavi.minimap")) {
                    if(TextUtils.isEmpty(longitude.getText())||TextUtils.isEmpty(latitude.getText())){
                        Toast.makeText(getApplicationContext(), "请输入经纬度", Toast.LENGTH_SHORT).show();

                    }else {
                        LatLng endPoint = BD2GCJ(Double.parseDouble(latitude.getText().toString()),Double.parseDouble(longitude.getText().toString()));//坐标转换终点位置
                        StringBuffer stringBuffer = new StringBuffer("androidamap://navi?sourceApplication=").append("amap");
                        stringBuffer.append("&lat=").append(endPoint.latitude)
                                .append("&lon=").append(endPoint.longitude)
                                .append("&dev=").append(0)
                                .append("&style=").append(2);
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(stringBuffer.toString()));
                        intent.setPackage("com.autonavi.minimap");
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请安装高德地图", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //腾讯
        btnTencent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInstallByread("com.tencent.map")) {
                    if(TextUtils.isEmpty(longitude.getText())||TextUtils.isEmpty(latitude.getText())){
                        Toast.makeText(getApplicationContext(), "请输入经纬度", Toast.LENGTH_SHORT).show();

                    }else {
                        LatLng endPoint = BD2GCJ(Double.parseDouble(latitude.getText().toString()),Double.parseDouble(longitude.getText().toString()));//坐标转换
                        StringBuffer stringBuffer = new StringBuffer("qqmap://map/routeplan?type=drive")
                                .append("&tocoord=").append(endPoint.latitude).append(",").append(endPoint.longitude);
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(stringBuffer.toString()));
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请安装腾讯地图", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //坐标系转化BD09LL百度坐标系转化为GCJ02腾讯、高德坐标系
    public LatLng BD2GCJ(double latitude, double longitude) {
        double x = longitude - 0.0065, y = latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);

        double lng = z * Math.cos(theta);//lng
        double lat = z * Math.sin(theta);//lat
        return new LatLng(lat, lng);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        mlocationClient.stop();
    }
}
