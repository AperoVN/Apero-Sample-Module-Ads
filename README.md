
# AperoModuleAds
This is SDK ads by [Apero](https://apero.vn/). It has built in some sdk for easy use like
- Admob
- MAX Mediation(Applovin)
- Google Billing
- Adjust
- Appsflyer
- Facebook SDK
- Firebase auto log tracking event, tROAS

# Import Module
Contact us for account
~~~
    maven {
        url 'https://artifactory.apero.vn/artifactory/gradle-release/'
            credentials {
                username "$username"
                password "$password"
            }
        }
    maven {
        url "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea"
    }
    maven {
        url 'https://artifact.bytedance.com/repository/pangle'
    }
    implementation 'apero-inhouse:apero-ads:1.0.6-alpha09'
~~~  
# Summary
* [Setup AperoAd](#setup_aperoad)
  * [Setup id ads](#set_up_ads)
  * [Config ads](#config_ads)
  * [Ads Formats](#ads_formats)

* [Billing App](#billing_app)
* [Ads rule](#ads_rule)

# <a id="setup_aperoad"></a>Setup AperoAd
## <a id="set_up_ads"></a>Setup enviroment with id ads for project

We recommend you to setup 2 environments for your project, and only use test id during development, ids from your admob only use when needed and for publishing to Google Store
* The name must be the same as the name of the marketing request
* Config variant test and release in gradle
* appDev: using id admob test while dev
* appProd: use ids from your admob,  build release (build file .aab)

~~~    
      productFlavors {
      appDev {
              manifestPlaceholders = [ ad_app_id:"AD_APP_ID_TEST" ]
              buildConfigField "String", "ads_inter_turn_on", "\"AD_ID_INTERSTIAL_TEST\""
              buildConfigField "String", "ads_inter_turn_off", "\"AD_ID_INTERSTIAL_TEST\""
              buildConfigField "Boolean", "build_debug", "true"
           }
       appProd {
            // ADS CONFIG BEGIN (required)
               manifestPlaceholders = [ ad_app_id:"AD_APP_ID" ]
               buildConfigField "String", "ads_inter_splash", "\"AD_ID_INTERSTIAL\""
               buildConfigField "String", "ads_inter_turn_on", "\"AD_ID_INTERSTIAL\""
               buildConfigField "Boolean", "build_debug", "false"
            // ADS CONFIG END (required)
           }
      }
~~~
AndroidManifest.xml
~~~
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="${ad_app_id}" />
        <meta-data
            android:name="com.facebook.sdk.ApplicationId"
            android:value="@string/facebook_app_id" />
        <meta-data
            android:name="com.facebook.sdk.AutoInitEnabled"
            android:value="true" />
        <meta-data
            android:name="com.facebook.sdk.AutoLogAppEventsEnabled"
            android:value="true" />
        <meta-data
            android:name="com.facebook.sdk.AdvertiserIDCollectionEnabled"
            android:value="true" />
~~~
* NOTE : do not set applicationId containing ".example" to avoid the case that id ads no fill

## <a id="config_ads"></a>Config ads
Create class Application

Configure your mediation here. using PROVIDER_ADMOB or PROVIDER_MAX

*** Note:
- Don't use id ad test for production environment
- Environment:
  - ENVIRONMENT_DEVELOP: for test ads and billing.
  - ENVIRONMENT_PRODUCTION: for prdouctions ads and billing.
~~~
class App : AdsMultiDexApplication(){
    @Override
    public void onCreate() {
        super.onCreate();
    ...
        String environment = BuildConfig.build_debug ? AperoAdConfig.ENVIRONMENT_DEVELOP : AperoAdConfig.ENVIRONMENT_PRODUCTION;
        aperoAdConfig = new AperoAdConfig(this, AperoAdConfig.PROVIDER_ADMOB, environment);

        // Optional: setup Adjust event
        AdjustConfig adjustConfig = new AdjustConfig(true,ADJUST_TOKEN);
        adjustConfig.setEventAdImpression(EVENT_AD_IMPRESSION_ADJUST);
        adjustConfig.setEventNamePurchase(EVENT_PURCHASE_ADJUST);
        aperoAdConfig.setAdjustConfig(adjustConfig);

        // Optional: setup Appsflyer event
        AppsflyerConfig appsflyerConfig = new AppsflyerConfig(true,APPSFLYER_TOKEN);
        aperoAdConfig.setAppsflyerConfig(appsflyerConfig);

        // Optional: enable ads resume
        aperoAdConfig.setIdAdResume(BuildConfig.ads_open_app);

        // Optional: setup list device test - recommended to use
        listTestDevice.add(DEVICE_ID_TEST);
        aperoAdConfig.setListDeviceTest(listTestDevice);

        AperoAd.getInstance().init(this, aperoAdConfig, false);

        // Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(false);
    }
}
~~~

## <a id="ads_formats"></a>Ads formats
### Ad Splash Interstitial
SplashActivity
~~~ 
    AperoAdCallback adCallback = new AperoAdCallback() {
        @Override
        public void onNextAction() {
            super.onNextAction();
            Log.d(TAG, "onNextAction");
            startMain();
        }
    };
~~~
~~~
        AperoAd.getInstance().setInitCallback(new AperoInitCallback() {
            @Override
            public void initAdSuccess() {
                AperoAd.getInstance().loadSplashInterstitialAds(SplashActivity.this, idAdSplash, TIME_OUT, TIME_DELAY_SHOW_AD, true, adCallback);
            }
        });
~~~
### Interstitial
Load ad interstital before show
~~~
  private fun loadInterCreate() {
    ApInterstitialAd mInterstitialAd = AperoAd.getInstance().getInterstitialAds(this, idInter);
  }
~~~
Show and auto release ad interstitial
~~~
         if (mInterstitialAd.isReady()) {
                AperoAd.getInstance().forceShowInterstitial(this, mInterstitialAd, new AperoAdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                Log.d(TAG, "onNextAction");
               startActivity(new Intent(MainActivity.this, MaxSimpleListActivity.class));
            }
                
                }, true);
            } else {
                loadAdInterstitial();
            }
~~~
### Ad Banner

#### Latest way:
~~~
    <com.ads.control.ads.bannerAds.AperoBannerAdView
        android:id="@+id/bannerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        app:layout_constraintBottom_toBottomOf="parent" />
~~~
call load ad banner
~~~
    bannerAdView.loadBanner(this, idBanner);
~~~
#### The older way:
~~~
  <include
  android:id="@+id/include"
  layout="@layout/layout_banner_control"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:layout_alignParentBottom="true"
  app:layout_constraintBottom_toBottomOf="parent" />
~~~
#### Call load ad banner

Normal banner in Activity/Fragment
~~~
AperoAd.getInstance().loadBanner(this, idBanner);
or
AperoAd.getInstance().loadBannerFragment(final Activity mActivity, String id, final View rootView);
~~~
Inline banner in Activity/Fragment
inlineStyle:
- Admob.BANNER_INLINE_SMALL_STYLE: for small inline banner
- Admob.BANNER_INLINE_LARGE_STYLE: for large inline banner
~~~
Admob.getInstance().loadInlineBanner(activity, idBanner, inlineStyle, adCallback);
or
Admob.getInstance().loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle);
~~~
Collapsible banner in Activity/Fragment
gravity:
* AppConstant.TOP: banner anchor at the top of layout
* AppConstant.BOTTOM: banner anchor at the bottom of layout
~~~
Admob.getInstance().loadCollapsibleBanner(final Activity mActivity, String id, String gravity, final AdCallback callback)
or
Admob.getInstance().loadCollapsibleBannerFragment(final Activity mActivity, String id, final View rootView, String gravity, final AdCallback callback);
~~~


### Ad Native
Load ad native before show

*** Notes: Admob and MAX use different layout

~~~
        AperoAd.getInstance().loadNativeAdResultCallback(this,ID_NATIVE_AD, com.ads.control.R.layout.custom_native_max_small,new AperoAdCallback(){
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
               //save or show native 
            }
            
            @Override
            public void onAdFailedToLoad(@Nullable ApAdError adError) {
                super.onAdFailedToLoad(adError);
                // gone layout ad native
            }
        });
        
        // Load priority native and default native ad by sametime:
        AperoAd.getInstance().loadNativePrioritySameTime(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_admod_medium_rate,
            object : AperoAdCallback() {
              override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
              }
              
              override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
              }
            }
        )
	    
    	// Load priority native and default native ad by alternate:
        AperoAd.getInstance().loadNativePriorityAlternate(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_admod_medium_rate,
            object : AperoAdCallback() {
               override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
               }
              
               override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
               }
            }
        )
        
        // Load priority native, medium native and default native ad by sametime:
        AperoAd.getInstance().loadNative3SameTime(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_MEDIUM,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_ad,
            object : AperoAdCallback() {
               override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
               }
              
               override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
              }
            }
        )
        
        // Load priority native, medium native and default native ad by alternate:
        AperoAd.getInstance().loadNative3Alternate(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_MEDIUM,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_ad,
            object : AperoAdCallback() {
               override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
               }
              
               override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
               }
            }
        )
~~~

Populate native ad to view
~~~
    AperoAd.getInstance().populateNativeAdView(MainApplovinActivity.this,nativeAd,flParentNative,shimmerFrameLayout);
~~~

Layout native admob sample
~~~
    <?xml version="1.0" encoding="utf-8"?>
    <com.google.android.gms.ads.nativead.NativeAdView xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="@dimen/_5sdp">
    
        <RelativeLayout
            android:id="@+id/ad_unit_content"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:backgroundTint="#F4F4F4"
            android:orientation="vertical">
    
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">
    
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:paddingStart="25dip"
                    android:paddingTop="8dip"
                    android:paddingEnd="8dip"
                    android:paddingBottom="8dip">
    
                    <ImageView
                        android:id="@+id/ad_app_icon"
                        android:layout_width="35dip"
                        android:layout_height="35dip"
                        android:adjustViewBounds="true"
                        android:src="@color/colorPrimary" />
    
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginLeft="5dip"
                        android:orientation="vertical">
    
                        <TextView
                            android:id="@+id/ad_headline"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:ellipsize="end"
                            android:maxLines="2"
                            android:text="sdsdsdsdsd"
                            android:textColor="@color/black"
                            android:textSize="@dimen/_10sdp" />
    
    
                        <TextView
                            android:id="@+id/ad_advertiser"
                            android:layout_width="wrap_content"
                            android:layout_height="0dp"
                            android:layout_weight="1"
                            android:ellipsize="end"
                            android:lines="2"
                            android:text="sdsdsdsdsdádasd"
                            android:textColor="@color/colorMain"
                            android:textSize="12sp"
                            android:textStyle="bold" />
    
    
                    </LinearLayout>
    
                </LinearLayout>
    
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:paddingHorizontal="10dp"
                    android:paddingVertical="5dp">
    
                    <TextView
                        android:id="@+id/ad_body"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="ádas"
                        android:textColor="@color/black"
                        android:textSize="12sp" />
    
                </LinearLayout>
    
                <com.google.android.gms.ads.nativead.MediaView
                    android:id="@+id/ad_media"
                    android:layout_width="match_parent"
                    android:layout_height="120dp"
                    android:layout_gravity="center_horizontal"
                    android:layout_marginTop="@dimen/_5sdp" />
    
                <Button
                    android:id="@+id/ad_call_to_action"
                    android:layout_width="match_parent"
                    android:layout_height="@dimen/_35sdp"
                    android:layout_marginHorizontal="@dimen/_10sdp"
                    android:layout_marginTop="@dimen/_5sdp"
                    android:layout_marginBottom="@dimen/_10sdp"
                    android:gravity="center"
                    android:text="Install"
                    android:textColor="@color/colorWhite"
                    android:textSize="@dimen/_12sdp"
                    android:textStyle="bold" />
            </LinearLayout>
    
            <TextView
                style="@style/AppTheme.Ads"
                android:background="@drawable/border_radius_ad" />
    
        </RelativeLayout>
    
    </com.google.android.gms.ads.nativead.NativeAdView>
~~~

Layout native max sample
~~~
    <?xml version="1.0" encoding="utf-8"?>
    <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    
        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical">
    
            <LinearLayout
                android:id="@+id/linearLayout3"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp"
                android:layout_marginEnd="8dp"
                android:orientation="horizontal"
                android:padding="8dip"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@+id/textView3">
    
                <ImageView
                    android:id="@+id/ad_app_icon"
                    android:layout_width="35dip"
                    android:layout_height="35dip"
                    android:adjustViewBounds="true"
                    android:src="@color/colorPrimary" />
    
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:orientation="vertical">
    
                    <TextView
                        android:id="@+id/ad_headline"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:ellipsize="end"
                        android:maxLines="2"
                        android:text="@string/offline_notification_text"
                        android:textColor="@android:color/black"
                        android:textSize="@dimen/_14sdp" />
    
                      <TextView
                          android:id="@+id/ad_advertiser"
                          android:layout_width="wrap_content"
                          android:layout_height="wrap_content"
                          android:layout_weight="1"
                          android:gravity="bottom"
                          android:lines="1"
                          android:text="@string/bottom_sheet_behavior"
                          android:textColor="@color/colorAds"
                          android:textSize="@dimen/_8sdp"
                          android:textStyle="bold" />
    
                </LinearLayout>
            </LinearLayout>
    
            <TextView
                android:id="@+id/ad_body"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:maxLines="3"
                android:text="@string/appbar_scrolling_view_behavior"
                android:textSize="@dimen/_10sdp"
                app:layout_constraintEnd_toEndOf="@+id/linearLayout3"
                app:layout_constraintStart_toStartOf="@+id/linearLayout3"
                app:layout_constraintTop_toBottomOf="@+id/linearLayout3" />
    
            <FrameLayout
                android:id="@+id/ad_media"
                android:layout_width="0dp"
                android:layout_height="0dp"
                android:layout_gravity="center_horizontal"
                android:layout_marginTop="8dp"
                android:layout_marginBottom="8dp"
                android:layout_weight="1"
                android:minWidth="120dp"
                android:minHeight="120dp"
                app:layout_constraintBottom_toTopOf="@+id/ad_call_to_action"
                app:layout_constraintEnd_toEndOf="@+id/ad_body"
                app:layout_constraintStart_toStartOf="@+id/ad_body"
                app:layout_constraintTop_toBottomOf="@+id/ad_body" />
    
            <Button
                android:id="@+id/ad_call_to_action"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginBottom="8dp"
                android:background="@drawable/ads_bg_lib"
                android:gravity="center"
                android:text="Cài Đặt"
                android:textColor="@color/colorWhite"
                android:textSize="@dimen/_12sdp"
                android:textStyle="bold"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="@+id/ad_body"
                app:layout_constraintStart_toStartOf="@+id/ad_body" />
    
            <FrameLayout
                android:id="@+id/ad_options_view"
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:layout_marginBottom="8dp"
                android:orientation="horizontal"
                app:layout_constraintBottom_toTopOf="@+id/ad_advertiser"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintHorizontal_bias="1.0"
                app:layout_constraintStart_toEndOf="@+id/ad_headline"
                app:layout_constraintTop_toTopOf="parent" />
                
            <TextView
                android:id="@+id/textView3"
                style="@style/AppTheme.Ads"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />
        </androidx.constraintlayout.widget.ConstraintLayout>
    </FrameLayout>
~~~

Layout container native ad
~~~
    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        ...

        <FrameLayout
            android:id="@+id/layoutAdNative"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="@dimen/_10sdp"
            android:orientation="vertical"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent">

            <include
                android:id="@+id/layoutShimmer"
                layout="@layout/layout_loading_ads_native" />
        </FrameLayout>
    </androidx.constraintlayout.widget.ConstraintLayout>
~~~

Layout loading ad native
~~~
    The layout is the same as that of the native ad layout but will use ShimmerFrameLayout instead of NativeAdView to create the loading animation
~~~

Call load native ad
~~~
 aperoNativeAdView.loadNativeAd(this, idNative);
~~~
Load Ad native for recyclerView
~~~~
    // ad native repeating interval
    AperoAdAdapter     adAdapter = AperoAd.getInstance().getNativeRepeatAdapter(this, idNative, layoutCustomNative, com.ads.control.R.layout.layout_native_medium,
                originalAdapter, listener, 4);
    
    // ad native fixed in position
        AperoAdAdapter   adAdapter = AperoAd.getInstance().getNativeFixedPositionAdapter(this, idNative, layoutCustomNative, com.ads.control.R.layout.layout_native_medium,
                originalAdapter, listener, 4);
    
        recyclerView.setAdapter(adAdapter.getAdapter());
        adAdapter.loadAds();
~~~~
### Ad Reward
Get and show reward
~~~
  ApRewardAd rewardAd = AperoAd.getInstance().getRewardAd(this, idAdReward);

   if (rewardAd != null && rewardAd.isReady()) {
                AperoAd.getInstance().forceShowRewardAd(this, rewardAd, new AperoAdCallback());
            }
});
~~~
### Ad resume
In Application
~~~ 
  override fun onCreate() {
    super.onCreate()
    AppOpenManager.getInstance().enableAppResume()
    // normal
    aperoAdConfig.setIdAdResume(BuildConfig.ad_resume_normal);
    // medium
    aperoAdConfig.setIdAdResumeMedium(BuildConfig.ad_resume_medium);
    // high
    aperoAdConfig.setIdAdResumeHigh(BuildConfig.ad_resume_high);
    ...
  }
    

~~~
### Ad open app splash
Set id ad
~~~
  AppOpenManager.getInstance().setSplashAdId(BuildConfig.ads_open_app);
~~~
Load ad app open splash at the same time as ad interstital splash:
* param1: context,
* param2: id ad interstital splash,
* param3: time out,
* param4: time delay to show ads after ad loaded,
* param5: true if show ad as soon as ad loaded, otherwise false,
* param6: callback for action ad:
~~~
  AperoAd.getInstance().loadAppOpenSplashSameTime(final Context context, String interId, long timeOut, long timeDelay, boolean showSplashIfReady, AperoAdCallback adListener)
~~~
Load ad app open splash, if false start loading ad interstital splash (params similar to same time way):
~~~
  AperoAd.getInstance().loadAppOpenSplashAlternate(final Context context, String interId, long timeOut, long timeDelay, boolean showSplashIfReady, AperoAdCallback adListener)
~~~

*NOTE: onInterstitalLoad() is called when app open ads splash loaded unsuccessfully but interstital splash is loaded successfully*

Show ad open app splash:
~~~
  AppOpenManager.getInstance().showAppOpenSplash(this, new AdCallback())
~~~
when hide app -> reopen app will be loaded forever, we will use this function in onResume
* Note : First time opening the app ignore this function
~~~
  AppOpenManager.getInstance().onCheckShowAppOpenSplashWhenFail(this, new AdCallback())
~~~

### Ads interstital splash priority
Load interstital splash priority at the same time with interstital splash default:
~~~
  AperoAd.getInstance().loadSplashInterPrioritySameTime(Context context, String interIdPriority, String interIdDefault, long timeOut, long timeDelay, boolean showSplashIfReady, AperoAdCallback adListener);
~~~
Load interstital splash priority, if false start loading ad interstital splash default:
~~~
 AperoAd.getInstance().loadSplashInterPriorityAlternate(Context context, String interIdPriority, String interIdDefault, long timeOut, long timeDelay, boolean showSplashIfReady, AperoAdCallback adListener);
~~~

*NOTE: onNormalInterSplashLoaded() is called when interstital splash priority loaded unsuccessfully but interstital splash default is loaded successfully*

Show ads interstital splash priority
~~~
  AperoAd.getInstance().onShowSplashPriority(Context context, AperoAdCallback adCallback)
~~~
when hide app -> reopen app will be loaded forever, we will use this function in onResume
* Note : First time opening the app ignore this function
~~~
  AperoAd.getInstance().onCheckShowSplashPriorityWhenFail(Context context, AperoAdCallback adCallback)
~~~

### Interstitial Splash 3 ( Update medium )
Load sametime
~~~
   AperoAd.getInstance().loadSplashInterPriority3SameTime(context,
                id_ads_inter_priority,
                id_ads_inter_medium,
                id_ads_inter_normal,
                timeout,
                timedelay,
                false,
                AperoAdCallback);
~~~
Load alternate
~~~
  AperoAd.getInstance().loadSplashInterPriority3Alternate(context,
                id_ads_inter_priority,
                id_ads_inter_medium,
                id_ads_inter_normal,
                timeout,
                timedelay,
                false,
                AperoAdCallback);
~~~
Show ad
~~~
  AperoAd.getInstance().onShowSplashPriority3(activity, AperoAdCallback);
~~~
when hide app -> reopen app will be loaded forever, we will use this function in onResume
* Note : First time opening the app ignore this function
~~~
  AperoAd.getInstance().onCheckShowSplashPriority3WhenFail(activity, AperoAdCallback, timedelay);
~~~

### Ad open app splash 3 ( update medium )
Set id ad
~~~
  AppOpenManager.getInstance().setAdIdAppOpen3High(BuildConfig.ads_open_app_high);
  AppOpenManager.getInstance().setAdIdAppOpen3Medium(BuildConfig.ads_open_app_medium);
~~~
Load sametime
~~~
  AperoAd.getInstance().loadAppOpenSplash3SameTime(context, id_inter, timeOut, timeDelay, true, AperoAdCallback);
~~~
when hide app -> reopen app will be loaded forever, we will use this function in onResume
* Note : First time opening the app ignore this function
~~~
  AperoAd.getInstance().onCheckShowedAppOpen3WhenFail(context, timeDelay, true, AperoAdCallback)
~~~

# <a id="billing_app"></a>Billing app
## Init Billing
Application
~~~
    @Override
    public void onCreate() {
        super.onCreate();
        AppPurchase.getInstance().initBilling(this,listINAPId,listSubsId);
    }
~~~
## Check status billing init
~~~
 if (AppPurchase.getInstance().getInitBillingFinish()){
            loadAdsPlash();
        }else {
            AppPurchase.getInstance().setBillingListener(new BillingListener() {
                @Override
                public void onInitBillingListener(int code) {
                         loadAdsPlash();
                }
            },5000);
        }
~~~
## Check purchase status
    //check purchase with PRODUCT_ID
     AppPurchase.getInstance().isPurchased(this,PRODUCT_ID);
     //check purchase all
     AppPurchase.getInstance().isPurchased(this);
##  Purchase
     AppPurchase.getInstance().purchase(this,PRODUCT_ID);
     AppPurchase.getInstance().subscribe(this,SUBS_ID);
## Purchase Listener
             AppPurchase.getInstance().setPurchaseListioner(new PurchaseListioner() {
                 @Override
                 public void onProductPurchased(String productId,String transactionDetails) {

                 }

                 @Override
                 public void displayErrorMessage(String errorMsg) {

                 }
             });

## Get id purchased
      AppPurchase.getInstance().getIdPurchased();
## Consume purchase
      AppPurchase.getInstance().consumePurchase(PRODUCT_ID);
## Get price
      AppPurchase.getInstance().getPrice(PRODUCT_ID)
      AppPurchase.getInstance().getPriceSub(SUBS_ID)
## Get owner items by user
	AppPurchase.getInstance().getOwnerIdSubs() // for subsciptions items
	AppPurchase.getInstance().getOwnerIdInapps() // for purchase items
### Show iap dialog
    InAppDialog dialog = new InAppDialog(this);
    dialog.setCallback(() -> {
         AppPurchase.getInstance().purchase(this,PRODUCT_ID);
        dialog.dismiss();
    });
    dialog.show();



# <a id="ads_rule"></a>Ads rule
## Always add device test to idTestList with all of your team's device
To ignore invalid ads traffic
https://support.google.com/adsense/answer/16737?hl=en
## Before show full-screen ad (interstitial, app open ad), alway show a short loading dialog
To ignore accident click from user. This feature is existed in library
## Never reload ad on onAdFailedToLoad
To ignore infinite loop
