package com.skithub.resultdear.agent.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skithub.resultdear.agent.model.UserLogin
import com.skithub.resultdear.agent.utils.api.ApiRepo
import com.skithub.resultdear.agent.utils.api.IRetrofitApiCall
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class SplashActivityViewModel(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModel(){
    private val compositeDisposable = CompositeDisposable()
    var userLogin = MutableLiveData<UserLogin?>()

    fun isUserExits(){
        compositeDisposable.add(
            iRetrofitApiCall.isUserExits()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSingleObserver<UserLogin>(){
                        override fun onSuccess(res: UserLogin?) {
                           userLogin.value =res
                        }

                        override fun onError(e: Throwable?) {
                            Log.d("RetrofitError", e?.message!!)
                        }

                    }
                )
        )
    }
}

class SplashActivityViewModelFactory(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashActivityViewModel(iRetrofitApiCall) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}