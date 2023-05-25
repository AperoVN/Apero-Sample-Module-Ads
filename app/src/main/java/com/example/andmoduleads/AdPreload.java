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
import com.ads.control.ads.AperoAdCallback;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.billing.AppPurchase;
import com.ads.control.dialog.PrepareLoadingAdsDialog;
import com.ads.control.event.AperoLogEventManager;
import com.ads.control.funtion.AdCallback;
import com.ads.control.funtion.AdType;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

import java.util.Calendar;

public class AdPreload {
    private static final String TAG = "AdPreload";
    private final static int NUMBER_RELOAD_WHEN_LOAD_FAILED = 3;
    private static AdPreload instance;
    InterstitialAd interPreloadPriority;
    InterstitialAd interPreloadNormal;
    private PrepareLoadingAdsDialog dialog;

    boolean isTimeDelayPreload = false; //xử lý delay time show ads, = true mới show ads
    private boolean isTimeoutPreload;
    private Handler handlerTimeoutPreload;
    private Runnable rdTimeoutPreload;
    private int numberReloadPriorityWhenFail = 0;
    private int numberReloadNormalWhenFail = 0;
    private boolean isAdPreloadShowed;
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

    public void loadAdInterPreloadSametime(final Context context, AperoAdCallback adCallback) {
        isAdPreloadShowed = true;
        isNextActionWhenLimitLoad = false;
        numberReloadPriorityWhenFail = 0;
        numberReloadNormalWhenFail = 0;
        loadAdsInterPreloadPriority(context, setLoadAdInterPreloadCallBack(context, 0, 0, adCallback));
        loadAdsInterPreloadNormal(context, setLoadAdInterPreloadCallBack(context, 0, 0, adCallback));
    }

    public void showAdInterPreloadSametime(
            final Context context,
            long timeOut,
            long timeDelay,
            AperoAdCallback adListener
    ) {
        numberReloadPriorityWhenFail = 0;
        numberReloadNormalWhenFail = 0;
        isAdPreloadShowed = false;
        isNextActionWhenLimitLoad = true;
        isTimeDelayPreload = false;
        isTimeoutPreload = false;
        activityName = context.getClass().getSimpleName();

        if (interPreloadPriority != null) {
            onShowAdsInterPreloadPriority((AppCompatActivity) context,
                    setShowAdInterPreloadPriorityCallBack(context, timeOut, timeDelay, adListener));
        } else {
            loadAdsInterPreloadPriority(context,
                    setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));

            if (interPreloadNormal != null) {
                onShowAdsInterPreloadNormal((AppCompatActivity) context,
                        setShowAdInterPreloadNormalCallBack(context, timeOut, timeDelay, adListener));
            } else {
                loadAdsInterPreloadNormal(context,
                        setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
            }
        }

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            AppCompatActivity activity = (AppCompatActivity) context;
            try {
                try {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                dialog = new PrepareLoadingAdsDialog(activity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    adListener.onNextAction();
                    return;
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (!activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                        dialog.dismiss();
                }
                checkAdReadyAndShow(context, timeOut, timeDelay, adListener);
            }, 800);

        }

        new Handler().postDelayed(() -> {
            checkAdReadyAndShow(context, timeOut, timeDelay, adListener);
            isTimeDelayPreload = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeoutPreload = new Handler();
            rdTimeoutPreload = new Runnable() {
                @Override
                public void run() {
                    isTimeoutPreload = true;
                    if (dialog != null && dialog.isShowing() && !((AppCompatActivity) context).isDestroyed())
                        dialog.dismiss();
                    checkAdReadyAndShow(context, timeOut, timeDelay, adListener);
                    if (!isAdPreloadShowed) {
                        adListener.onNextAction();
                    }
                }
            };
            handlerTimeoutPreload.postDelayed(rdTimeoutPreload, timeOut);
        }
    }

    public void loadAdsInterPreloadPriority(final Context context, AperoAdCallback adListener) {
        Log.i(TAG, "loadAdInterstitialPreloadPriority: ");

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onNextAction();
                if (handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }
            return;
        }

        Admob.getInstance().getInterstitialAds(context, idAdInterPreloadPriority, new AdCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "loadAdInterstitialPriority end time loading success:" + Calendar.getInstance().getTimeInMillis());
                if (isTimeoutPreload)
                    return;
                if (interstitialAd != null) {
                    interPreloadPriority = interstitialAd;
                    interPreloadPriority.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
                        AperoLogEventManager.logPaidAdImpression(context,
                                adValue,
                                interPreloadPriority.getAdUnitId(),
                                interPreloadPriority.getResponseInfo()
                                        .getMediationAdapterClassName(), AdType.INTERSTITIAL);
                    });
                    if (isTimeDelayPreload) {
                        adListener.onAdSplashPriorityReady();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);

                if (isTimeoutPreload)
                    return;
                Log.e(TAG, "loadAdInterstitialPriority end time loading error:" + Calendar.getInstance().getTimeInMillis());
                if (adListener != null) {
                    adListener.onAdPriorityFailedToLoad(new ApAdError((i)));
                }
            }
        });
    }

    public void onShowAdsInterPreloadPriority(AppCompatActivity activity, AperoAdCallback adListener) {
        if (isAdPreloadShowed) return;
        isAdPreloadShowed = true;
        Log.i(TAG, "onShowAdInterstitial: Priority ");

        if (interPreloadPriority == null) {
            adListener.onAdPriorityFailedToShow(new ApAdError("interstitial ads null "));
            return;
        }

        if (handlerTimeoutPreload != null) {
            handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }
        interPreloadPriority.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial:onAdShowedFullScreenContent ");
                AppOpenManager.getInstance().setInterstitialShowing(true);
                AppOpenManager.getInstance().disableAppResume();
                AperoLogEventManager.onTrackEvent("inter_show_" + activityName);
                interPreloadPriority = null;
                if (handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial:onAdDismissedFullScreenContent ");
                AppOpenManager.getInstance().setInterstitialShowing(false);
                AppOpenManager.getInstance().enableAppResume();
                interPreloadPriority = null;
                if (adListener != null) {
                    adListener.onAdClosed();
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.e(TAG, "Interstitial onAdFailedToShowFullScreenContent: " + adError.getMessage());
                interPreloadPriority = null;
                if (adListener != null) {
                    adListener.onAdPriorityFailedToShow(new ApAdError(adError));
                }
                if (dialog != null) {
                    dialog.dismiss();
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
                    if (interPreloadPriority != null) {
                        Log.i(TAG, "start show InterstitialAdPriority " + activity.getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
                        interPreloadPriority.show(activity);
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

    public void loadAdsInterPreloadNormal(final Context context, AperoAdCallback adListener) {
        Log.i(TAG, "loadAdInterstitialPreloadNormal: ");

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onNextAction();
                if (handlerTimeoutPreload != null) {
                    handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
                }
            }
            return;
        }

        Admob.getInstance().getInterstitialAds(context, idAdInterPreloadNormal, new AdCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "loadAdInterstitialNormal end time loading success:" + Calendar.getInstance().getTimeInMillis());
                if (isTimeoutPreload)
                    return;
                if (interstitialAd != null) {
                    interPreloadNormal = interstitialAd;
                    interPreloadNormal.setOnPaidEventListener(adValue -> {
                        AperoLogEventManager.logPaidAdImpression(context,
                                adValue,
                                interPreloadNormal.getAdUnitId(),
                                interPreloadNormal.getResponseInfo()
                                        .getMediationAdapterClassName(), AdType.INTERSTITIAL);
                    });
                    if (isTimeDelayPreload) {
                        adListener.onAdSplashReady();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (isTimeoutPreload)
                    return;
                Log.e(TAG, "loadAdInterstitialNormal end time loading error:" + Calendar.getInstance().getTimeInMillis());
                if (adListener != null) {
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

        if (handlerTimeoutPreload != null) {
            handlerTimeoutPreload.removeCallbacks(rdTimeoutPreload);
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
                if (handlerTimeoutPreload != null) {
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
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.e(TAG, "Interstitial onAdFailedToShowFullScreenContent: " + adError.getMessage());
                interPreloadNormal = null;
                if (adListener != null) {
                    adListener.onAdPriorityFailedToShow(new ApAdError(adError));
                }
                if (dialog != null) {
                    dialog.dismiss();
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
                        Log.i(TAG, "start show InterstitialAdNormal " + activity.getLifecycle().getCurrentState().name() + "/" + ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name());
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

    private void checkAdReadyAndShow(Context context, long timeOut, long timeDelay, AperoAdCallback adListener) {
        if (interPreloadPriority != null && !isAdPreloadShowed) {
            Log.i(TAG, "loadSplashInterstitialAdsPriority:show ad on delay ");
            onShowAdsInterPreloadPriority((AppCompatActivity) context,
                    setShowAdInterPreloadPriorityCallBack(context, timeOut, timeDelay, adListener));
        } else if (interPreloadNormal != null && !isAdPreloadShowed) {
            Log.i(TAG, "loadAdInterstitialNormal:show ad on delay ");
            onShowAdsInterPreloadNormal((AppCompatActivity) context,
                    setShowAdInterPreloadNormalCallBack(context, timeOut, timeDelay, adListener));
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
                Log.i(TAG, "onAdSplashReady: ");
                onShowAdsInterPreloadNormal((AppCompatActivity) context,
                        setShowAdInterPreloadNormalCallBack(context, timeOut, timeDelay, adListener));
            }

            @Override
            public void onAdPriorityFailedToLoad(@Nullable ApAdError adError) {
                super.onAdPriorityFailedToLoad(adError);
                Log.e(TAG, "onAdPriorityFailedToLoad: ");
                adListener.onAdPriorityFailedToLoad(adError);
                if (numberReloadPriorityWhenFail < NUMBER_RELOAD_WHEN_LOAD_FAILED && interPreloadPriority == null) {
                    numberReloadPriorityWhenFail++;
                    loadAdsInterPreloadPriority(context,
                            setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
                } else if (numberReloadNormalWhenFail >= NUMBER_RELOAD_WHEN_LOAD_FAILED && isNextActionWhenLimitLoad) {
                    isNextActionWhenLimitLoad = false;
                    adListener.onNextAction();
                    if (handlerTimeoutPreload != null) {
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
                    loadAdsInterPreloadNormal(context,
                            setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
                } else if (numberReloadPriorityWhenFail >= NUMBER_RELOAD_WHEN_LOAD_FAILED && isNextActionWhenLimitLoad) {
                    isNextActionWhenLimitLoad = false;
                    adListener.onNextAction();
                    if (handlerTimeoutPreload != null) {
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
                if (interPreloadNormal != null) {
                    onShowAdsInterPreloadNormal((AppCompatActivity) context,
                            setShowAdInterPreloadNormalCallBack(context, timeOut, timeDelay, adListener));
                } else {
                    adListener.onNextAction();
                    if (handlerTimeoutPreload != null) {
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
                adListener.onAdClosed();
                super.onAdClosed();
                Log.i(TAG, "onAdClosed and load ad priority: ");
                loadAdsInterPreloadPriority(context,
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
                if (interPreloadPriority != null
                ) {
                    onShowAdsInterPreloadPriority((AppCompatActivity) context,
                            setShowAdInterPreloadPriorityCallBack(context, timeOut, timeDelay, adListener));
                } else {
                    adListener.onNextAction();
                    if (handlerTimeoutPreload != null) {
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
                adListener.onAdClosed();
                super.onAdClosed();
                Log.i(TAG, "onAdClosed and load ad normal: ");
                loadAdsInterPreloadNormal(context,
                        setLoadAdInterPreloadCallBack(context, timeOut, timeDelay, adListener));
            }
        };
    }
}
