package com.skithub.resultdear.agent.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skithub.resultdear.agent.model.UserLogin
import com.skithub.resultdear.agent.utils.api.ApiRepo
import com.skithub.resultdear.agent.utils.api.IRetrofitApiCall
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class EntryActivityViewModel(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModel() {
    val repo = ApiRepo(iRetrofitApiCall)
    var loading = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()
    var userLogin = MutableLiveData<UserLogin?>()

    fun isUserExits(){
        loading.value = true
        compositeDisposable.add(
            repo.isUserExits()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSingleObserver<UserLogin>(){
                        override fun onSuccess(res: UserLogin?) {
                            loading.value = false
                            userLogin.value =res
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

class EntryViewModelFactory(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryActivityViewModel(iRetrofitApiCall) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}