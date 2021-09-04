package com.skithub.resultdear.agent.utils.api

import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.skithub.resultdear.agent.BuildConfig
import com.skithub.resultdear.agent.application.MyApplication
import com.skithub.resultdear.agent.model.GmailInfo
import com.skithub.resultdear.agent.utils.Constant
import io.paperdb.Paper
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {

    companion object {
        const val HEADER_CACHE_CONTROL = "Cache-Control-agent"
        const val HEADER_PRAGMA = "Pragma-agent"
        //const val BASE_URL = "http://192.168.0.103/resuldearagent/api/"
        const val BASE_URL = "https://agent.lotterysambadpro.xyz/api/"

        @Volatile
        private var iRetrofitApiCall: IRetrofitApiCall? = null
        private val LOCK = Any()

        operator fun invoke() = iRetrofitApiCall ?: synchronized(LOCK) {
            iRetrofitApiCall ?: getRetrofitClient().also {
                iRetrofitApiCall = it
            }
        }

        private fun getRetrofitClient(): IRetrofitApiCall {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.setLenient()
            val gson = gsonBuilder.create()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(IRetrofitApiCall::class.java)
        }



        private fun okHttpClient(): OkHttpClient{
            return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor())
                .addNetworkInterceptor(networkInterceptor())
                .addInterceptor(getAuth()!!)
                .build()
        }

        private fun httpLoggingInterceptor(): HttpLoggingInterceptor {

            return HttpLoggingInterceptor().apply {

                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            }
        }

        private fun networkInterceptor(): Interceptor{
            return Interceptor.invoke {
                chain -> chain.proceed(chain.request()).also {
                    val cacheControl: CacheControl = CacheControl.Builder()
                        .maxAge(5, TimeUnit.SECONDS)
                        .build()

                it.newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                    .build()

            }
            }
        }

        private fun getAuth(): Interceptor? {
            Paper.init(MyApplication.appContext)
            val gmailInfo: GmailInfo? = Paper.book().read(Constant.GMAILINFO)
            return object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    var email = ""
                    var uid = ""
                    var token = ""
                    if (gmailInfo != null) {
                        email = gmailInfo.gmail
                        uid = gmailInfo.user_id
                        token = gmailInfo.access_token
                    } else {
                        val auth = FirebaseAuth.getInstance()

                        email = auth.currentUser!!.email!!
                        uid = auth.currentUser!!.uid
                        auth.getAccessToken(true)
                            .addOnCompleteListener(OnCompleteListener {
                               token = it.result.token!!
                            })

                    }
                    Log.d("AuthData", "$email $uid $token")
                    val request: Request = chain.request()
                    val modifiedRequest = request.newBuilder()
                        .addHeader("email", email)
                        .addHeader("uid", uid)
                        .addHeader("token", token)
                        .build()
                    return chain.proceed(modifiedRequest)
                }
            }
        }


    }
}