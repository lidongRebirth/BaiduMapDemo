# 百度地图相关配置、供下次使用（详细）
[APK下载](https://github.com/myfittinglife/MarkDown-Resource/blob/master/%E7%99%BE%E5%BA%A6%E5%9C%B0%E5%9B%BEDemo.apk)

### 1. 百度控制台创建应用

  测试时发布版SHA1和开发版SHA1填写一样即可
  **获取SHA1:**

![图一](https://github.com/myfittinglife/MarkDown-Resource/blob/master/%E7%99%BE%E5%BA%A6%E5%9C%B0%E5%9B%BE%E4%BD%BF%E7%94%A8%E9%85%8D%E7%BD%AE1.png) 

![图二](https://github.com/myfittinglife/MarkDown-Resource/blob/master/%E7%99%BE%E5%BA%A6%E5%9C%B0%E5%9B%BE%E4%BD%BF%E7%94%A8%E9%85%8D%E7%BD%AE2.png)

  将内容填写完整后，请保存好自己的AK

![图三](https://github.com/myfittinglife/MarkDown-Resource/blob/master/%E7%99%BE%E5%BA%A6%E5%9C%B0%E5%9B%BE%E4%BD%BF%E7%94%A8%E9%85%8D%E7%BD%AE3.png)

### 2. 下载百度SDK开发包

![图四](https://github.com/myfittinglife/MarkDown-Resource/blob/master/%E7%99%BE%E5%BA%A6%E5%9C%B0%E5%9B%BE%E4%BD%BF%E7%94%A8%E9%85%8D%E7%BD%AE4.png)

勾选需要的功能，别都选，会增大apk体积，最基本的这两个一定要选，不然到时候都没办法定位。

[地址](http://lbsyun.baidu.com/index.php?title=sdk/download&action#selected=location_all)

### 3. AndroidStudio工程配置

- 将开发包拷贝到工程
  将下载的压缩包解压，将内部的BaiduLBS_Android.jar文件放入到工程的libs文件夹下，在工程的src文件夹下新建directory，名为JniLibs，将解压的其他几个文件夹全部放入。
  ![AndroidSDK_JniLibs.png](http://mapopen-pub-androidsdk.cdn.bcebos.com/map/images/AndroidSDK_JniLibs.png) 
- 官方文档介绍的往工程中添加Jar文件步骤可省略，直接点击sync即可，可以看到dependencies中已经依赖`implementation fileTree(dir: 'libs', include: ['*.jar'])`

### 4. Hello BaiduMap程序配置

- 添加开发密钥，即创建应用时的AK

```java
<application>  
    <meta-data  
        android:name="com.baidu.lbsapi.API_KEY"  
        android:value="开发者 AK" />  
</application>
```

- 添加权限

```java
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/> 
//获取设备网络状态，禁用后无法获取网络状态
<uses-permission android:name="android.permission.INTERNET"/> 
//网络权限，当禁用后，无法进行检索等相关业务
<uses-permission android:name="android.permission.READ_PHONE_STATE" /> 
//读取设备硬件信息，统计数据
<uses-permission android:name="com.android.launcher.permission.READ_SETTINGS" /> 
//读取系统信息，包含系统版本等信息，用作统计
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> 
//获取设备的网络状态，鉴权所需网络代理
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/> 
//允许sd卡写权限，需写入地图数据，禁用后无法显示地图
<uses-permission android:name="android.permission.WRITE_SETTINGS" /> 
//获取统计数据
<uses-permission android:name="android.permission.CAMERA" />  
//使用步行AR导航，配置Camera权限
//以下为自己填的，动态申请权限也需要在这里声明
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
```

- 新建类继承自Application，然后在onCreate()方法内初始化context信息

```java
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
```

记得写完之后，要在manifest文件内加入

```java
 <application
        android:name=".DemoApplication"
        ...
 </application>
```

### 5、使用

* 建立LocationClient对象

* 建立地理位置监听器`MyLocationListener extends BDAbstractLocationListener`

* 注册监听器`mlocationClient.registerLocationListener(new MyLocationListener());`

* 在监听器中的`onReceiveLocation()`方法中进行移动地图操作（`调用navigateTo()方法`）

* 显示地图首先有MapView控件，其次通过mapview.getMap()方法来获取BaiduMap地图控制器

  通过`baiduMap.setMyLocationEnabled(true);`方法允许我显示在地图上

* navigateTo()方法：

  ```java
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
  ```

* 跳转至客户端导航

  首先通过isInstallByread()方法来判断是否安装了该地图应用

  若安装了，则先将经纬度转化一下，百度使用的是BD09LL，而高德和腾讯使用GCJ02

  通过各家提供的url来进行跳转

  参考：

  [百度地图](http://lbsyun.baidu.com/index.php?title=uri/api/android )

  [高德地图](https://lbs.amap.com/api/amap-mobile/guide/android/navigation)

  [腾讯地图](http://lbs.qq.com/uri_v1/guide-mobile-navAndRoute.html)


### 6、注意事项：

- JniLibs文件夹要放到main文件夹下，否则会出现so文件加载异常
  加载so文件异常	请看这两篇文章，加载若jar文件和so都放到libs中的话，需要在build.gradle中加入

```java
    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
```

若其他的so文件的加载方式是这样而百度地图使用的方式是另一种，在libs中加入jar，而在main文件夹下创建jniLibs文件夹放入其他的so文件就会导致加载异常。解决方法：使用同一种方式来加载so文件
[百度地图接入的两种方式](https://blog.csdn.net/Timmy_zzh/article/details/52515674)
[导入so文件遇到问题](https://blog.csdn.net/baidu_33968826/article/details/53193739)

- 找不到LocationClient，因为自己只下载了一个基础地图，而没下载基础定位，导致找不到LocationClient这个类

### 7、相关文章

[判断是否安装](https://blog.csdn.net/xj626361950/article/details/42915423)
[第三方调用](http://lbsyun.baidu.com/index.php?title=uri/api/android#service-page-anchor1)
[判断是否安装某应用](https://blog.csdn.net/sinat_19917631/article/details/54343791)

[api获取图片网址](https://blog.csdn.net/qq5132834/article/details/12260409?tdsourcetag=s_pcqq_aiomsg)

[web服务API](http://lbsyun.baidu.com/index.php?title=static)
[静态地图](https://blog.csdn.net/qq5132834/article/details/12260409?tdsourcetag=s_pcqq_aiomsg)

[坐标转换](https://blog.csdn.net/ever69/article/details/82427085)

[全景地图](http://api.map.baidu.com/lbsapi/andsdk_review.html)

```java
/**
	 * 检查手机上是否安装了指定的软件
	 * @param context
	 * @param packageName：应用包名
	 * @return
	 */
	private boolean isAvilible(Context context, String packageName){
		//获取packagemanager
		final PackageManager packageManager = context.getPackageManager();
		//获取所有已安装程序的包信息
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
		//用于存储所有已安装程序的包名
		List<String> packageNames = new ArrayList<String>();
		//从pinfo中将包名字逐一取出，压入pName list中
		if(packageInfos != null){
			for(int i = 0; i < packageInfos.size(); i++){
				String packName = packageInfos.get(i).packageName;
				packageNames.add(packName);
			}
		}
		//判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
		return packageNames.contains(packageName);
	}
 
 
 
@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.common_right:
				//如果已安装,
				if(isAvilible(mcontext,"com.baidu.BaiduMap")) {//传入指定应用包名
					WinToast.makeText(mcontext,"即将用百度地图打开导航").show();
					Uri mUri = Uri.parse("geo:"+jingdu+","+weidu+"?q="+title);
					Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
					startActivity(mIntent);
				}else if(isAvilible(mcontext,"com.autonavi.minimap")){
					WinToast.makeText(mcontext,"即将用高德地图打开导航").show();
					Uri mUri = Uri.parse("geo:"+jingdu+","+weidu+"?q="+title);
					Intent intent = new Intent("android.intent.action.VIEW",mUri);
					startActivity(intent);
				}else {
					WinToast.makeText(mcontext,"请安装第三方地图方可导航").show();
					return;
				}
				break;
			default:
				break;
		}
	}
	
```

