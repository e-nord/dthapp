package com.dth.app;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import io.branch.referral.Branch;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class DTHApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("DTHApplication", "Crash", ex);
                handler.uncaughtException(thread, ex);
            }
        });
//        Fabric.with(this, new Crashlytics());
        Hawk.init(this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                .setStorage(HawkBuilder.newSharedPrefStorage(this))
                .setLogLevel(LogLevel.FULL)
                .build();
        Branch.getAutoInstance(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(this).
                applicationId(getString(R.string.parse_app_id)).
                clientKey(getString(R.string.parse_client_key)).
//                server(getString(R.string.parse_server)).
                build());
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        ParseFacebookUtils.initialize(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(null)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}

