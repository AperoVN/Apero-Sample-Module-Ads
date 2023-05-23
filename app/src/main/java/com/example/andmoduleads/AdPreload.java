package com.example.andmoduleads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.event.AperoLogEventManager;
import com.ads.control.funtion.AdCallback;
import com.ads.control.funtion.AdType;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.ads.control.ads.AperoAdCallback;
import com.ads.control.billing.AppPurchase;

import java.util.Calendar;

public class AdPreload {
    private static final String TAG = "AdPreload";
    private final static int NUMBER_RELOAD_WHEN_LOAD_FAILED = 5;
    private static AdPreload instance;
    InterstitialAd interPreloadHigh;
    InterstitialAd interPreloadNormal;

    boolean isTimeDelayPreloadHigh = false; //xử lý delay time show ads, = true mới show ads
    boolean isTimeDelayPreloadNormal = false; //xử lý delay time show ads, = true mới show ads
    private Handler handlerTimeoutPreloadPriority;
    private Runnable rdTimeoutPreloadPriority;
    private Handler handlerTimeoutPreloadNormal;
    private Runnable rdTimeoutPreloadNormal;
    private Handler handlerTimeoutPreload;
    private Runnable rdTimeoutPreload;
    private boolean isTimeoutPreloadHigh;
    private boolean isTimeoutPreloadNormal;
    private int numberReloadPriorityWhenFail = 0;
    private int numberReloadNormalWhenFail = 0;
    private boolean isAdPreloadShowed;
    private boolean isAdPreloadPriorityShowFailed = false;
    private boolean isAdPreloadNormalShowFailed = false;

    private String idAdInterPreloadPriority;
    private String idAdInterPreloadNormal;
    private String activityName;

    private boolean isNextActionWhenLimitLoad = true;

    public static AdPreload getInstance() {
        if (instance == null) {
            instance = new AdPreload();
        }
        return instance;
    }

    public void setAdIdInterPreload(String adIdPriority, String adIdNormal) {
        idAdInterPreloadPriority = adIdPriority;
        idAdInterPreloadNormal = adIdNormal;
    }

    public void loadAdsInterPreloadPriority(final Context context, long timeOut, long timeDelay, AperoAdCallback adListener) {
        isTimeDelayPreloadHigh = false;
        isTimeoutPreloadHigh = false;
        Log.i(TAG, "loadAdInterstitialPreloadPriority: ");

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onNextAction();
                if(handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }
            return;
        }
        new Handler().postDelayed(() -> {
            if (interPreloadHigh != null) {
                Log.i(TAG, "loadSplashInterstitialAdsPriority:show ad on delay ");
                adListener.onAdSplashPriorityReady();
                return;
            }
            Log.i(TAG, "loadSplashInterstitialAdsPriority: delay validate");
            isTimeDelayPreloadHigh = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeoutPreloadPriority = new Handler();
            rdTimeoutPreloadPriority = () -> {
                Log.e(TAG, "loadSplashInterstitialAdsPriority: on timeout");
                isTimeoutPreloadHigh = true;
                if (interPreloadHigh != null) {
                    Log.i(TAG, "loadSplashInterstitialAdsPriority:show ad on timeout ");
                    adListener.onAdSplashPriorityReady();
                    return;
                }
            };
            handlerTimeoutPreloadPriority.postDelayed(rdTimeoutPreloadPriority, timeOut);
        }

        Admob.getInstance().getInterstitialAds(context, idAdInterPreloadPriority, new AdCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "loadSplashInterstitialAdsPriority  end time loading success:" + Calendar.getInstance().getTimeInMillis());
                if (isTimeoutPreloadHigh)
                    return;
                if (interstitialAd != null) {
                    interPreloadHigh = interstitialAd;
                    interPreloadHigh.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
                        AperoLogEventManager.logPaidAdImpression(context,
                                adValue,
                                interPreloadHigh.getAdUnitId(),
                                interPreloadHigh.getResponseInfo()
                                        .getMediationAdapterClassName(), AdType.INTERSTITIAL);
                    });
                    if (isTimeDelayPreloadHigh) {
                        adListener.onAdSplashPriorityReady();
                        Log.i(TAG, "loadSplashInterstitialAdsPriority:show ad on loaded ");
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "loadSplashInterstitialAdsPriority end time loading error:" + Calendar.getInstance().getTimeInMillis());
                if (isTimeoutPreloadHigh)
                    return;
                if (adListener != null) {
                    if (handlerTimeoutPreloadPriority != null && rdTimeoutPreloadPriority != null) {
                        handlerTimeoutPreloadPriority.removeCallbacks(rdTimeoutPreloadPriority);
                    }
                    if (i != null)
                        Log.e(TAG, "loadSplashInterstitialAdsPriority: load fail " + i.getMessage());
                    adListener.onAdPriorityFailedToLoad(new ApAdError((i)));
                }
            }
        });
    }

    public void onShowAdsInterPreloadPriority(AppCompatActivity activity, AperoAdCallback adListener) {
        if (isAdPreloadShowed) return;
        isAdPreloadShowed = true;
        Log.i(TAG, "onShowAdInterstitial: Priority ");

        if (interPreloadHigh == null) {
            adListener.onAdPriorityFailedToShow(new ApAdError("interstitial ads null "));
            return;
        }

        if (handlerTimeoutPreloadPriority != null && rdTimeoutPreloadPriority != null) {
            handlerTimeoutPreloadPriority.removeCallbacks(rdTimeoutPreloadPriority);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }
        interPreloadHigh.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial:onAdShowedFullScreenContent ");
                AppOpenManager.getInstance().setInterstitialShowing(true);
                AppOpenManager.getInstance().disableAppResume();
                AperoLogEventManager.onTrackEvent("inter_show_" + activityName);
                interPreloadHigh = null;
                if(handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial:onAdDismissedFullScreenContent ");
                AppOpenManager.getInstance().setInterstitialShowing(false);
                AppOpenManager.getInstance().enableAppResume();
                interPreloadHigh = null;
                if (adListener != null) {
                    adListener.onAdClosed();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.e(TAG, "Interstitial onAdFailedToShowFullScreenContent: " + adError.getMessage());
                interPreloadHigh = null;
                if (adListener != null) {
                    adListener.onAdPriorityFailedToShow(new ApAdError(adError));
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (adListener != null) {
                    adListener.onAdClicked();
                }
                AppOpenManager.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                if (adListener != null) {
                    adListener.onAdImpression();
                }
            }
        });

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            new Handler().postDelayed(() -> {
                if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    if (interPreloadHigh != null) {
                        Log.i(TAG, "start show InterstitialAd " + activity.getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
                        interPreloadHigh.show(activity);
                    } else if (adListener != null) {
                        adListener.onAdPriorityFailedToShow(new ApAdError("interstitial ads null "));
                    }
                } else {
                    Log.e(TAG, "onShowInterstitial:   show fail in background after show loading ad");
                    adListener.onAdPriorityFailedToShow(new ApAdError(new AdError(0, " show fail in background after show loading ad", "AperoAd")));
                }
            }, 800);

        } else {
            adListener.onAdPriorityFailedToShow(new ApAdError(new AdError(0, " show fail in background after show loading ad", "AperoAd")));
            Log.e(TAG, "onShowInterstitial: fail on background");
        }
    }

    public void loadAdsInterPreloadNormal(final Context context, long timeOut, long timeDelay, AperoAdCallback adListener) {
        isTimeDelayPreloadNormal = false;
        isTimeoutPreloadNormal = false;
        Log.i(TAG, "loadAdInterstitialPreloadNormal: ");

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onNextAction();
                if(handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }
            return;
        }
        new Handler().postDelayed(() -> {
            //check delay show ad splash
            if (interPreloadNormal != null) {
                Log.i(TAG, "loadAdInterstitialNormal:show ad on delay ");
                adListener.onAdSplashReady();
                return;
            }
            Log.i(TAG, "loadAdInterstitialNormal: delay validate");
            isTimeDelayPreloadNormal = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeoutPreloadNormal = new Handler();
            rdTimeoutPreloadNormal = () -> {
                Log.e(TAG, "loadAdInterstitialNormal: on timeout");
                isTimeoutPreloadNormal = true;
                if (interPreloadNormal != null) {
                    Log.i(TAG, "loadAdInterstitialNormal:show ad on timeout ");
                    adListener.onAdSplashReady();
                    return;
                }
            };
            handlerTimeoutPreloadNormal.postDelayed(rdTimeoutPreloadNormal, timeOut);
        }

        Admob.getInstance().getInterstitialAds(context, idAdInterPreloadNormal, new AdCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "loadAdInterstitialNormal  end time loading success:" + Calendar.getInstance().getTimeInMillis());
                if (isTimeoutPreloadNormal)
                    return;
                if (interstitialAd != null) {
                    interPreloadNormal = interstitialAd;
                    interPreloadNormal.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());

                        AperoLogEventManager.logPaidAdImpression(context,
                                adValue,
                                interPreloadNormal.getAdUnitId(),
                                interPreloadNormal.getResponseInfo()
                                        .getMediationAdapterClassName(), AdType.INTERSTITIAL);
                    });
                    if (isTimeDelayPreloadHigh) {
                        adListener.onAdSplashPriorityReady();
                        Log.i(TAG, "loadAdInterstitialNormal:show ad on loaded ");
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (isTimeoutPreloadNormal)
                    return;
                Log.e(TAG, "loadAdInterstitialNormal end time loading error:" + Calendar.getInstance().getTimeInMillis());
                if (adListener != null) {
                    if (handlerTimeoutPreloadNormal != null && rdTimeoutPreloadNormal != null) {
                        handlerTimeoutPreloadNormal.removeCallbacks(rdTimeoutPreloadNormal);
                    }
                    if (i != null)
                        Log.e(TAG, "loadAdInterstitialNormal: load fail " + i.getMessage());
                    adListener.onAdFailedToLoad(new ApAdError(i));
                }
            }
        });
    }

    public void onShowAdsInterPreloadNormal(AppCompatActivity activity, AperoAdCallback adListener) {
        if (isAdPreloadShowed) return;
        isAdPreloadShowed = true;
        Log.i(TAG, "onShowAdInterstitial: Normal ");

        if (interPreloadNormal == null) {
            adListener.onAdFailedToShow(new ApAdError("interstitial ads null "));
            return;
        }

        if (handlerTimeoutPreloadNormal != null && rdTimeoutPreloadNormal != null) {
            handlerTimeoutPreloadNormal.removeCallbacks(rdTimeoutPreloadNormal);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }
        interPreloadNormal.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial:onAdShowedFullScreenContent ");
                AppOpenManager.getInstance().setInterstitialShowing(true);
                AppOpenManager.getInstance().disableAppResume();
                AperoLogEventManager.onTrackEvent("inter_show_" + activityName);
                interPreloadNormal = null;
                if(handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial:onAdDismissedFullScreenContent ");
                AppOpenManager.getInstance().setInterstitialShowing(false);
                AppOpenManager.getInstance().enableAppResume();
                interPreloadNormal = null;
                if (adListener != null) {
                    adListener.onAdClosed();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.e(TAG, "Interstitial onAdFailedToShowFullScreenContent: " + adError.getMessage());
                interPreloadNormal = null;
                if (adListener != null) {
                    adListener.onAdPriorityFailedToShow(new ApAdError(adError));
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (adListener != null) {
                    adListener.onAdClicked();
                }
                AppOpenManager.getInstance().disableAdResumeByClickAction();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                if (adListener != null) {
                    adListener.onAdImpression();
                }
            }
        });

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            new Handler().postDelayed(() -> {
                if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    if (interPreloadNormal != null) {
                        Log.i(TAG, "start show InterstitialAd " + activity.getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
                        interPreloadNormal.show(activity);
                    } else if (adListener != null) {
                        adListener.onAdFailedToShow(new ApAdError("interstitial ads null "));
                    }
                } else {
                    Log.e(TAG, "onShowInterstitial:   show fail in background after show loading ad");
                    adListener.onAdFailedToShow(new ApAdError(new AdError(0, " show fail in background after show loading ad", "AperoAd")));
                }
            }, 800);

        } else {
            adListener.onAdFailedToShow(new ApAdError(new AdError(0, " show fail in background after show loading ad", "AperoAd")));
            Log.e(TAG, "onShowInterstitial: fail on background");
        }
    }

    public void preloadInterSameTime(
            final Context context,
            long timeOut,
            long timeDelay,
            AperoAdCallback adListener
    ) {
        numberReloadPriorityWhenFail = 0;
        numberReloadNormalWhenFail = 0;
        isAdPreloadShowed = false;
        isAdPreloadPriorityShowFailed = false;
        isAdPreloadNormalShowFailed = false;
        isNextActionWhenLimitLoad = true;
        activityName = context.getClass().getSimpleName();

        if (interPreloadHigh != null) {
            onShowAdsInterPreloadPriority((AppCompatActivity) context,
                    setShowAdInterPreloadPriorityCallBack(context, timeOut, timeDelay, adListener));
        } else {
            loadAdsInterPreloadPriority(context, timeOut, timeDelay,
                    setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));

            if (interPreloadNormal != null) {
                onShowAdsInterPreloadNormal((AppCompatActivity) context,
                        setShowAdInterPreloadNormalCallBack(context, timeOut, timeDelay, adListener));
            } else {
                loadAdsInterPreloadNormal(context, timeOut, timeDelay,
                        setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
            }
        }

        if (timeOut > 0) {
            handlerTimeoutPreload = new Handler();
            rdTimeoutPreload = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: da");
                    adListener.onNextAction();
                }
            };
            handlerTimeoutPreload.postDelayed(rdTimeoutPreload, timeOut);
        }
    }

    private AperoAdCallback setLoadAdInterPreloadCallBack(
            Context context,
            long timeOut,
            long timeDelay,
            AperoAdCallback adListener
    ) {
        return new AperoAdCallback() {
            @Override
            public void onAdSplashPriorityReady() {
                super.onAdSplashPriorityReady();
                adListener.onAdSplashPriorityReady();
                Log.i(TAG, "onAdSplashPriorityReady: ");
                onShowAdsInterPreloadPriority((AppCompatActivity) context,
                        setShowAdInterPreloadPriorityCallBack(context, timeOut, timeDelay, adListener));
            }

            @Override
            public void onAdSplashReady() {
                super.onAdSplashReady();
                adListener.onAdSplashReady();
                Log.i(TAG, "onAdSplashReady: "+ isAdPreloadShowed);
                if (!isAdPreloadShowed) {
                    onShowAdsInterPreloadNormal((AppCompatActivity) context,
                            setShowAdInterPreloadNormalCallBack(context, timeOut, timeDelay, adListener));
                }
            }

            @Override
            public void onAdPriorityFailedToLoad(@Nullable ApAdError adError) {
                super.onAdPriorityFailedToLoad(adError);
                Log.e(TAG, "onAdPriorityFailedToLoad: ");
                adListener.onAdPriorityFailedToLoad(adError);
                if (numberReloadPriorityWhenFail < NUMBER_RELOAD_WHEN_LOAD_FAILED && interPreloadHigh == null) {
                    numberReloadPriorityWhenFail++;
                    loadAdsInterPreloadPriority(context, timeOut, timeDelay,
                            setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
                } else if (numberReloadNormalWhenFail >= NUMBER_RELOAD_WHEN_LOAD_FAILED && isNextActionWhenLimitLoad) {
                    Log.e(TAG, "onAdPriorityFailedToLoad: priados");
                    isNextActionWhenLimitLoad = false;
                    adListener.onNextAction();
                    if(handlerTimeoutPreload != null) {
                        handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(@Nullable ApAdError adError) {
                super.onAdFailedToLoad(adError);
                Log.e(TAG, "onAdFailedToLoad: ");
                adListener.onAdFailedToLoad(adError);
                if (numberReloadNormalWhenFail < NUMBER_RELOAD_WHEN_LOAD_FAILED && interPreloadNormal == null) {
                    numberReloadNormalWhenFail++;
                    loadAdsInterPreloadNormal(context, timeOut, timeDelay,
                            setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
                } else if (numberReloadPriorityWhenFail >= NUMBER_RELOAD_WHEN_LOAD_FAILED && isNextActionWhenLimitLoad) {
                    isNextActionWhenLimitLoad = false;
                    adListener.onNextAction();
                    if(handlerTimeoutPreload != null) {
                        handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                    }
                }
            }
        };
    }

    private AperoAdCallback setShowAdInterPreloadPriorityCallBack(
            Context context,
            long timeOut,
            long timeDelay,
            AperoAdCallback adListener
    ) {
        return new AperoAdCallback() {
            @Override
            public void onAdPriorityFailedToShow(@Nullable ApAdError adError) {
                super.onAdPriorityFailedToShow(adError);
                adListener.onAdPriorityFailedToShow(adError);
                Log.e(TAG, "onAdPriorityFailedToShow: ");
                isAdPreloadPriorityShowFailed = true;
                if (interPreloadNormal != null && !isAdPreloadNormalShowFailed) {
                    onShowAdsInterPreloadNormal((AppCompatActivity) context,
                            setShowAdInterPreloadPriorityCallBack(context, timeOut, timeDelay, adListener));
                } else {
                    adListener.onNextAction();
                    if(handlerTimeoutPreload != null) {
                        handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                    }
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AperoLogEventManager.onTrackEvent("inter_click_" + activityName);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                adListener.onAdClosed();
                Log.i(TAG, "onAdClosed and load ad priority: ");
                loadAdsInterPreloadPriority(context, timeOut, timeDelay,
                        setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
            }

        };
    }

    private AperoAdCallback setShowAdInterPreloadNormalCallBack(
            Context context,
            long timeOut,
            long timeDelay,
            AperoAdCallback adListener
    ) {
        return new AperoAdCallback() {
            @Override
            public void onAdFailedToShow(@Nullable ApAdError adError) {
                super.onAdFailedToShow(adError);
                adListener.onAdFailedToShow(adError);
                Log.e(TAG, "onAdFailedToShow: ");
                isAdPreloadNormalShowFailed = true;
                if (interPreloadHigh != null && !isAdPreloadPriorityShowFailed) {
                    onShowAdsInterPreloadPriority((AppCompatActivity) context,
                            setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
                } else {
                    adListener.onNextAction();
                    if(handlerTimeoutPreload != null) {
                        handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                    }
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                AperoLogEventManager.onTrackEvent("inter_click_" + activityName);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                adListener.onAdClosed();
                Log.i(TAG, "onAdClosed and load ad normal: ");
                loadAdsInterPreloadNormal(context, timeOut, timeDelay,
                        setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
            }
        };
    }
}
