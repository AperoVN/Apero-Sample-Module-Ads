package com.example.andmoduleads.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.AperoAd;
import com.ads.control.ads.AperoAdCallback;
import com.ads.control.ads.wrapper.ApInterstitialAd;
import com.ads.control.config.AperoAdConfig;
import com.ads.control.ads.AperoInitCallback;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.ads.control.billing.AppPurchase;
import com.ads.control.funtion.AdCallback;
import com.ads.control.funtion.BillingListener;
import com.example.andmoduleads.BuildConfig;
import com.example.andmoduleads.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private List<String> list = new ArrayList<>();
    private String idAdSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (AperoAd.getInstance().getMediationProvider() == AperoAdConfig.PROVIDER_ADMOB)
            idAdSplash = BuildConfig.ad_interstitial_splash;
        else
            idAdSplash = getString(R.string.applovin_test_inter);
        AppPurchase.getInstance().setBillingListener(new BillingListener() {
            @Override
            public void onInitBillingFinished(int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadAndShowInterPriority();
                    }
                });
            }
        }, 5000);

        AppPurchase.getInstance().setEventConsumePurchaseTest(findViewById(R.id.txtLoading));
//        testCase();
    }

    private void testCase(){
        findViewById(R.id.txtLoading).setOnClickListener(v -> startMain());
    }

    private void loadAndShowOpenAppSplash() {
        AppOpenManager.getInstance().setSplashAdId(BuildConfig.ads_open_app);
        AperoAd.getInstance().loadAppOpenSplashSameTime(this, BuildConfig.ad_interstitial_splash, 30000, 3000, false, new AperoAdCallback() {
            @Override
            public void onAdSplashReady() {
                super.onAdSplashReady();
                //App open ads ready
                AppOpenManager.getInstance().showAppOpenSplash(SplashActivity.this, new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        startMain();
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startMain();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        startMain();
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@Nullable ApAdError adError) {
                super.onAdFailedToLoad(adError);
                startMain();
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                startMain();
            }

            @Override
            public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                //App open ads failed, inter ads loaded
                AperoAd.getInstance().onShowSplash(SplashActivity.this, new AperoAdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startMain();
                    }
                });
            }
        });
    }

    private void loadAndShowInterPriority() {
        AperoAd.getInstance().loadSplashInterPrioritySameTime(
                this,
                BuildConfig.ads_inter_priority,
                BuildConfig.ad_interstitial_splash,
                30000,
                3000,
                false,
                new AperoAdCallback() {
                    @Override
                    public void onAdSplashReady() {
                        super.onAdSplashReady();
                        //Ads Inter priority ready
                        AperoAd.getInstance().onShowSplashPriority(SplashActivity.this, new AperoAdCallback() {
                            @Override
                            public void onNextAction() {
                                super.onNextAction();
                                startMain();
                            }
                        });
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startMain();
                    }

                    @Override
                    public void onNormalInterSplashLoaded() {
                        super.onNormalInterSplashLoaded();
                        //ads inter priority failed, ads inter normal loaded
                        AperoAd.getInstance().onShowSplash(SplashActivity.this, new AperoAdCallback() {
                            @Override
                            public void onNextAction() {
                                super.onNextAction();
                                startMain();
                            }
                        });
                    }
                });
    }


    AperoAdCallback adCallback = new AperoAdCallback() {
        @Override
        public void onAdFailedToLoad(@Nullable ApAdError i) {
            super.onAdFailedToLoad(i);
            Log.d(TAG, "onAdLoaded");
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            Log.d(TAG, "onAdLoaded");
        }

        @Override
        public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
            super.onNativeAdLoaded(nativeAd);
        }

        @Override
        public void onNextAction() {
            super.onNextAction();
            Log.d(TAG, "onNextAction");
            startMain();
        }

        @Override
        public void onAdSplashReady() {
            super.onAdSplashReady();

        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            Log.d(TAG, "onAdClosed");
        }

    };

    private void loadSplash() {
        Log.d(TAG, "onCreate: show splash ads");
        AperoAd.getInstance().setInitCallback(new AperoInitCallback() {
            @Override
            public void initAdSuccess() {
                AperoAd.getInstance().loadSplashInterstitialAds(SplashActivity.this, idAdSplash, 30000, 5000, true, adCallback);
            }
        });

    }


    private void loadAdmobAd() {
        AppOpenManager.getInstance().setSplashActivity(SplashActivity.class, getString(R.string.admod_app_open_ad_id), 30000);
        AppOpenManager.getInstance().setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                startMain();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                startMain();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }
        });
        AppOpenManager.getInstance().loadAndShowSplashAds(getString(R.string.admod_app_open_ad_id));
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "Splash onPause: " );
        AperoAd.getInstance().onCheckShowSplashWhenFail(this, adCallback, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "Splash onPause: " );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "Splash onStop: " );
    }

    @Override
    protected void onDestroy() {
//        AppOpenManager.getInstance().removeFullScreenContentCallback();
        super.onDestroy();
    }
}