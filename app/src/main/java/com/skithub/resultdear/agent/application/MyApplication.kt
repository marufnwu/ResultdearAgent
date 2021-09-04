package com.skithub.resultdear.agent.application

import android.app.Application
import android.content.Context
import com.skithub.resultdear.agent.utils.api.RetrofitClient

class MyApplication : Application(){

    public val iRetrofitApiCall by lazy {
        RetrofitClient.invoke()
    }


    override fun onCreate() {
        super.onCreate()
        MyApplication.appContext = applicationContext
    }



    companion object {

        lateinit  var appContext: Context

    }
}