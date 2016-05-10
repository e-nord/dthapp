package com.dthapp;

import android.app.Application;

import com.facebook.FacebookSdk;


public class DTHApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this);
        //Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        //ParseFacebookUtils.initialize(this);
    }
}

