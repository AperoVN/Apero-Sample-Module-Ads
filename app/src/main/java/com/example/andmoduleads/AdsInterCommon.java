package com.example.andmoduleads;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ads.control.ads.AperoAd;
import com.ads.control.ads.AperoAdCallback;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.ads.wrapper.ApInterstitialAd;
import com.ads.control.billing.AppPurchase;

public class AdsInterCommon {
    private static final String TAG = "AdsInterCommon";
    private final static int NUMBER_RELOAD_WHEN_LOAD_FAILED = 2;
    private static AdsInterCommon instance;
    private int numberReloadPriorityWhenFail = 0;
    private int numberReloadNormalWhenFail = 0;

    public static AdsInterCommon getInstance() {
        if (instance == null) {
            instance = new AdsInterCommon();
        }
        return instance;
    }

    public void loadInterSameTime(final Context context, String idAdInterPriority, String idAdInterNormal, boolean reload, AperoAdCallback adListener) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        numberReloadPriorityWhenFail = 0;
        numberReloadNormalWhenFail = 0;
        if (MyApplication.getApplication().getStorageCommon().interPriority == null) {
            Log.i(TAG, "getAdsInterPriority: ");

            AperoAdCallback adPriorityCallBack = new AperoAdCallback() {
                @Override
                public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {
                    super.onInterstitialLoad(interstitialAd);
                    adListener.onInterPriorityLoaded(interstitialAd);
                }

                @Override
                public void onAdFailedToLoad(@Nullable ApAdError adError) {
                    super.onAdFailedToLoad(adError);
                    Log.e(TAG, "onAdFailedToLoad: ad Priority");
                    if (reload && numberReloadPriorityWhenFail < NUMBER_RELOAD_WHEN_LOAD_FAILED) {
                        numberReloadPriorityWhenFail++;
                        AperoAd.getInstance().getInterstitialAds(context, idAdInterPriority, this);
                    } else {
                        adListener.onAdPriorityFailedToLoad(adError);
                    }
                }
            };

            AperoAd.getInstance().getInterstitialAds(context, idAdInterPriority, adPriorityCallBack);
        }
        if (MyApplication.getApplication().getStorageCommon().interNormal == null) {
            Log.i(TAG, "getAdsInterNormal: ");
            AperoAdCallback adNormalCallBack = new AperoAdCallback() {
                @Override
                public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {
                    super.onInterstitialLoad(interstitialAd);
                    adListener.onInterstitialLoad(interstitialAd);
                }

                @Override
                public void onAdFailedToLoad(@Nullable ApAdError adError) {
                    super.onAdFailedToLoad(adError);
                    Log.e(TAG, "onAdFailedToLoad: ad Normal");
                    if (reload && numberReloadNormalWhenFail < NUMBER_RELOAD_WHEN_LOAD_FAILED) {
                        numberReloadNormalWhenFail++;
                        AperoAd.getInstance().getInterstitialAds(context, idAdInterNormal, this);
                    } else {
                        adListener.onAdFailedToLoad(adError);
                    }
                }
            };

            AperoAd.getInstance().getInterstitialAds(context, idAdInterNormal, adNormalCallBack);
        }
    }

    public void showInterSameTime(Context context, ApInterstitialAd interPriority, ApInterstitialAd interNormal, Boolean reload, AdsInterCallBack adCallback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adCallback != null) {
                adCallback.onNextAction();
            }
            return;
        }
        if (interPriority != null) {
            Log.e(TAG, "showInterSameTime: Ad priority");
            AperoAd.getInstance().forceShowInterstitial(
                    context,
                    interPriority,
                    new AperoAdCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            adCallback.onAdClosed();
                        }

                        @Override
                        public void onNextAction() {
                            super.onNextAction();
                            adCallback.onNextAction();
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            adCallback.onAdClicked();
                        }

                        @Override
                        public void onInterstitialShow() {
                            super.onInterstitialShow();
                            adCallback.onInterstitialPriorityShowed();
                        }
                    },
                    reload);
        } else if (interNormal != null) {
            Log.e(TAG, "showInterSameTime: Ad normal");
            AperoAd.getInstance().forceShowInterstitial(
                    context,
                    interNormal,
                    new AperoAdCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            adCallback.onAdClosed();
                        }

                        @Override
                        public void onNextAction() {
                            super.onNextAction();

                            adCallback.onNextAction();
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            adCallback.onAdClicked();
                        }
                        public void onInterstitialShow() {
                            super.onInterstitialShow();
                            adCallback.onInterstitialNormalShowed();
                        }
                    },
                    reload);
        } else {
            adCallback.onNextAction();
        }
    }

}
