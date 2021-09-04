package com.skithub.resultdear.agent.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skithub.resultdear.agent.model.UserLogin
import com.skithub.resultdear.agent.model.response.ReferUserResponse
import com.skithub.resultdear.agent.utils.api.ApiRepo
import com.skithub.resultdear.agent.utils.api.IRetrofitApiCall
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class UserListViewModel(private var iRetrofitApiCall: IRetrofitApiCall) :ViewModel() {
    val repo = ApiRepo(iRetrofitApiCall)
    var loading = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()
    var  referUserResponse = MutableLiveData<ReferUserResponse?>()

    fun getAllReferUser(){
        loading.value = true
        compositeDisposable.add(
            repo.getAllReferUser()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSingleObserver<ReferUserResponse>(){
                        override fun onSuccess(res: ReferUserResponse?) {
                            loading.value = false
                            referUserResponse.value =res
                        }

                        override fun onError(e: Throwable?) {
                            Log.d("RetrofitError", e?.message!!)
                            loading.value = false
                        }

                    }
                )
        )
    }

}

class UserListViewFactory(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserListViewModel(iRetrofitApiCall) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}