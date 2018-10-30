package com.myfittinglife.app.baidudemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
            case R.id.btn_request:
//                boolean permission= permissionRequest();    //false为未申请权限，true为权限都已申请
//                if(permission){
//                    Toast.makeText(getApplicationContext(),"请开启全部权限",Toast.LENGTH_SHORT).show();
//                    return;
//                }else {
//                    requestLocation();
//                }
                permissionsChecked();
                break;
            case R.id.btn_move:
                BDLocation bdLocation = new BDLocation();
                bdLocation.setLongitude(Double.parseDouble(longitude.getText().toString()));
                bdLocation.setLatitude(Double.parseDouble(latitude.getText().toString()));
                move(bdLocation);
                break;
            case R.id.btn_jump:
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
                if(isInstallByread("com.baidu.BaiduMap")){
                    Intent i1 = new Intent();
                    // 驾车导航
                    i1.setData(Uri.parse("baidumap://map/navi?location="+latitude.getText().toString()+","+longitude.getText().toString()+"&src=andr.baidu.openAPIdemo"));
                    /**
                     * intent = Intent.getIntent("intent://map/direction?origin=latlng:34.264642646862,108.95108518068|nam
                     e:我家&destination=大雁塔&mode=driving&region=西安&src=yourCompanyName|yourAppName#Inten
                     t;scheme=bdapp;package=com.baidu.BaiduMap;end");
                     */
                    Intent intent = new Intent();
                    intent.setData(Uri.parse("baidumap://map/navi?origin=latlng:"+latitude.getText().toString()+","+longitude.getText().toString()+"|name:我&destination=公司&mode=driving;scheme=bdapp;package=com.baidu.BaiduMap;end"));


                    startActivity(i1);
                }else {
                    Toast.makeText(getApplicationContext(),"请安装百度地图",Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                break;
        }
    }
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
    public  void permissionsChecked(){
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
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
            requestLocation();
        }
    }

//    public boolean permissionRequest() {
//        int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
//        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if(permission1  !=  PackageManager.PERMISSION_GRANTED|| permission2!=PackageManager.PERMISSION_GRANTED||permission3!=PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, permissions, 1);       //有一个不在就申请权限
//            return false;
//        }else {
//            return true;
//        }
//    }

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
        option.setCoorType("bd09ll");       //设置此才会在自己的位置中来，才会准确
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

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: 线程中赋值");
                    tv_location.append("经度：" + bdLocation.getLongitude() + "纬度：" + bdLocation.getLatitude()+"国家："+bdLocation.getCountry()+"省："+bdLocation.getProvince()+"市"+bdLocation.getCity()+"区/县:"+bdLocation.getDistrict()+"街道："+bdLocation.getStreet()+"详细地址信息："+bdLocation.getAddress());
                    myBDLocation = bdLocation;
                }
            });
            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation||bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo();
            }
        }
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
