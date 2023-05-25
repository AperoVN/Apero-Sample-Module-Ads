package com.example.andmoduleads.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.AperoAd;
import com.ads.control.ads.AperoAdCallback;
import com.ads.control.config.AperoAdConfig;
import com.ads.control.ads.bannerAds.AperoBannerAdView;
import com.ads.control.ads.nativeAds.AperoNativeAdView;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.ads.wrapper.ApInterstitialAd;
import com.ads.control.ads.wrapper.ApRewardAd;
import com.ads.control.billing.AppPurchase;
import com.ads.control.dialog.DialogExitApp1;
import com.ads.control.dialog.InAppDialog;
import com.ads.control.event.AperoAdjust;
import com.ads.control.funtion.AdCallback;
import com.ads.control.funtion.DialogExitListener;
import com.ads.control.funtion.PurchaseListener;
import com.example.andmoduleads.AdPreload;
import com.example.andmoduleads.BuildConfig;
import com.example.andmoduleads.R;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.nativead.NativeAd;

public class MainActivity extends AppCompatActivity {
    public static final String PRODUCT_ID = "android.test.purchased";
    private static final String TAG = "MAIN_TEST";
    //adjust
    private static final String EVENT_TOKEN_SIMPLE = "g3mfiw";
    private static final String EVENT_TOKEN_REVENUE = "a4fd35";


    private FrameLayout frAds;
    private NativeAd unifiedNativeAd;
    private ApInterstitialAd mInterstitialAd;
    private ApRewardAd rewardAd;

    private boolean isShowDialogExit = false;

    private String idBanner = "";
    private String idNative = "";
    private String idInter = "";

    private int layoutNativeCustom;
    private AperoNativeAdView aperoNativeAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aperoNativeAdView = findViewById(R.id.aperoNativeAds);


        configMediationProvider();
        AperoAd.getInstance().setCountClickToShowAds(3);

        AppOpenManager.getInstance().setEnableScreenContentCallback(true);
        AppOpenManager.getInstance().setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                Log.e("AppOpenManager", "onAdShowedFullScreenContent: ");

            }
        });
        /**
         * Sample integration native ads
         */
        /*
        AperoAd.getInstance().loadNativeAd(this, idNative, layoutNativeCustom);
        aperoNativeAdView.setLayoutLoading(R.layout.loading_native_medium);
        aperoNativeAdView.setLayoutCustomNativeAd(layoutNativeCustom);
        aperoNativeAdView.loadNativeAd(this, idNative,layoutNativeCustom,R.layout.loading_native_medium);
        */
        aperoNativeAdView.loadNativeAd(this, idNative);


        AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                Log.e("PurchaseListioner", "ProductPurchased:" + productId);
                Log.e("PurchaseListioner", "transactionDetails:" + transactionDetails);
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Log.e("PurchaseListioner", "displayErrorMessage:" + errorMsg);
            }

            @Override
            public void onUserCancelBilling() {

            }
        });

//        AperoAd.getInstance().loadBanner(this, idBanner);
        AperoBannerAdView bannerAdView = findViewById(R.id.bannerView);
        bannerAdView.loadBanner(this, idBanner);
        loadAdInterstitial();

        findViewById(R.id.btShowAds).setOnClickListener(v -> {
            if (mInterstitialAd.isReady()) {

                ApInterstitialAd inter = AperoAd.getInstance().getInterstitialAds(this, idInter);

                AperoAd.getInstance().showInterstitialAdByTimes(this, mInterstitialAd, new AperoAdCallback() {
                    @Override
                    public void onNextAction() {
                        Log.i(TAG, "onNextAction: start content and finish main");
                        startActivity(new Intent(MainActivity.this, ContentActivity.class));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable ApAdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.i(TAG, "onAdFailedToShow:" + adError.getMessage());
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        Log.d(TAG, "onInterstitialShow");
                    }
                }, true);
            } else {
                Toast.makeText(this, "start loading ads", Toast.LENGTH_SHORT).show();
                loadAdInterstitial();
            }
        });

        findViewById(R.id.btForceShowAds).setOnClickListener(v -> {
            if (mInterstitialAd.isReady()) {
                AperoAd.getInstance().forceShowInterstitial(this, mInterstitialAd, new AperoAdCallback() {
                    @Override
                    public void onNextAction() {
                        Log.i(TAG, "onAdClosed: start content and finish main");
                        startActivity(new Intent(MainActivity.this, SimpleListActivity.class));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable ApAdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.i(TAG, "onAdFailedToShow:" + adError.getMessage());
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        Log.d(TAG, "onInterstitialShow");
                    }
                }, true);
            } else {
                loadAdInterstitial();
            }

        });

        findViewById(R.id.btnShowReward).setOnClickListener(v -> {
            if (rewardAd != null && rewardAd.isReady()) {
                AperoAd.getInstance().forceShowRewardAd(this, rewardAd, new AperoAdCallback());
                return;
            }
            rewardAd = AperoAd.getInstance().getRewardAd(this,  BuildConfig.ad_reward);
        });

        Button btnIAP = findViewById(R.id.btIap);
        if (AppPurchase.getInstance().isPurchased()) {
            btnIAP.setText("Consume Purchase");
        } else {
            btnIAP.setText("Purchase");
        }
        btnIAP.setOnClickListener(v -> {
            if (AppPurchase.getInstance().isPurchased()) {
                AppPurchase.getInstance().consumePurchase(AppPurchase.PRODUCT_ID_TEST);
            } else {
                InAppDialog dialog = new InAppDialog(this);
                dialog.setCallback(() -> {
                    AppPurchase.getInstance().purchase(this, PRODUCT_ID);
                    dialog.dismiss();
                });
                dialog.show();
            }
        });

        AdPreload.getInstance().loadAdInterPreloadSametime(this,new AperoAdCallback());
        findViewById(R.id.btnInterPreload).setOnClickListener(v -> {
            AdPreload.getInstance().showAdInterPreloadSametime(
                    this,
                    7000,
                    3000,
                    new AperoAdCallback(){
                        @Override
                        public void onNextAction() {
                            super.onNextAction();
                            Log.e("AdPreload", "onNextAction: ");
                            startActivity(new Intent(MainActivity.this, SimpleListActivity.class));
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            Log.e("AdPreload", "onAdClosed: ");
                            startActivity(new Intent(MainActivity.this, SimpleListActivity.class));
                        }
                    }
            );
        });

    }

    private void configMediationProvider() {
        if (AperoAd.getInstance().getMediationProvider() == AperoAdConfig.PROVIDER_ADMOB) {
            idBanner = BuildConfig.ad_banner;
            idNative = BuildConfig.ad_native;
            idInter = BuildConfig.ad_interstitial_splash;
            layoutNativeCustom = com.ads.control.R.layout.custom_native_admod_medium_rate;
        } else {
            idBanner = getString(R.string.applovin_test_banner);
            idNative = getString(R.string.applovin_test_native);
            idInter = getString(R.string.applovin_test_inter);
            layoutNativeCustom = com.ads.control.R.layout.custom_native_max_medium;
        }
    }

    private void loadAdInterstitial() {

        mInterstitialAd = AperoAd.getInstance().getInterstitialAds(this, idInter);
    }


    public void onTrackSimpleEventClick(View v) {
        AperoAdjust.onTrackEvent(EVENT_TOKEN_SIMPLE);
    }

    public void onTrackRevenueEventClick(View v) {
        AperoAdjust.onTrackRevenue(EVENT_TOKEN_REVENUE, 1f, "EUR");
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNativeExit();
    }

    private void loadNativeExit() {

        if (unifiedNativeAd != null)
            return;
        Admob.getInstance().loadNativeAd(this,BuildConfig.ad_native, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(NativeAd unifiedNativeAd) {
                MainActivity.this.unifiedNativeAd = unifiedNativeAd;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (unifiedNativeAd == null)
            return;

        DialogExitApp1 dialogExitApp1 = new DialogExitApp1(this, unifiedNativeAd, 1);
        dialogExitApp1.setDialogExitListener(new DialogExitListener() {
            @Override
            public void onExit(boolean exit) {
                MainActivity.super.onBackPressed();
            }
        });
        dialogExitApp1.setCancelable(false);
        dialogExitApp1.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        AppPurchase.getInstance().handleActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult", "ProductPurchased:" + data.toString());
        if (AppPurchase.getInstance().isPurchased(this)) {
            findViewById(R.id.btIap).setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}