
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
    implementation 'apero-inhouse:apero-ads:1.8.0-alpha01'
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
AndroidManiafest.xml
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
AndroidManiafest.xml
~~~
<application
android:name=".App"
...
>
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
            }
        )
~~~
Populate native ad to view
~~~
    AperoAd.getInstance().populateNativeAdView(MainApplovinActivity.this,nativeAd,flParentNative,shimmerFrameLayout);
~~~
auto load and show native contains loading

in layout XML
~~~
      <com.ads.control.ads.nativeAds.AperoNativeAdView
        android:id="@+id/aperoNativeAds"
        android:layout_width="match_parent"
        android:layout_height="@dimen/_150sdp"
        android:background="@drawable/bg_card_ads"
        app:layoutCustomNativeAd="@layout/custom_native_admod_medium_rate"
        app:layoutLoading="@layout/loading_native_medium"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
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
App
~~~ 
  override fun onCreate() {
    super.onCreate()
    AppOpenManager.getInstance().enableAppResume()
    aperoAdConfig.setIdAdResume(AppOpenManager.AD_UNIT_ID_TEST);
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
Check show app open ad splash when failed
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
Check show interstital splash priority when failed
~~~
  AperoAd.getInstance().onCheckShowSplashPriorityWhenFail(Context context, AperoAdCallback adCallback)
~~~

# <a id="billing_app"></a>Billing app
## Init Billing
Application
~~~
    @Override
    public void onCreate() {
        super.onCreate();
        List<PurchaseItem> listPurchaseItem = new ArrayList<>();
        listPurchaseItem.add(new PurchaseItem(Constants.PRODUCT_ID, AppPurchase.TYPE_IAP.PURCHASE));
        listPurchaseItem.add(new PurchaseItem(Constants.ID_SUBS_WITH_FREE_TRIAL, "trial", AppPurchase.TYPE_IAP.SUBSCRIPTION));
        listPurchaseItem.add(new PurchaseItem(Constants.ID_SUBS_WITHOUT_FREE_TRIAL, AppPurchase.TYPE_IAP.SUBSCRIPTION));
        AppPurchase.getInstance().initBilling(this, listPurchaseItem);
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
