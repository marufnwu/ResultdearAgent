package com.skithub.resultdear.agent.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skithub.resultdear.agent.model.UserLogin
import com.skithub.resultdear.agent.model.response.ServerResponse
import com.skithub.resultdear.agent.utils.api.ApiRepo
import com.skithub.resultdear.agent.utils.api.IRetrofitApiCall
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class SignupViewModel(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModel() {
    val repo = ApiRepo(iRetrofitApiCall)

    var loading = MutableLiveData<Boolean>()
    var serverResponse = MutableLiveData<ServerResponse>()

    private val compositeDisposable = CompositeDisposable()
    fun signUp(name:String, email:String, age:String, gender:Int, photoUrl:String, phone:String){
        loading.value = true

        compositeDisposable.add(
            repo.signUp(name, email, age, gender, photoUrl, phone)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSingleObserver<ServerResponse>(){
                        override fun onSuccess(value: ServerResponse?) {
                            loading.value = false
                            serverResponse.value = value!!
                        }

                        override fun onError(e: Throwable?) {
                            loading.value = false
                        }

                    }
                )
        )
    }
}
class SignupViewModelFactory(private val iRetrofitApiCall: IRetrofitApiCall) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignupViewModel(iRetrofitApiCall) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}