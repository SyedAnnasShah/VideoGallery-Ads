package com.syedannasshah.videogallery.helpers;


import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds


class MyApp :Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) // Initialize Mobile Ads SDK

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

}

