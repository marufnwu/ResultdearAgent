package com.skithub.resultdear.agent.utils.api

import com.skithub.resultdear.agent.application.MyApplication
import com.skithub.resultdear.agent.model.UserLogin
import com.skithub.resultdear.agent.model.response.ReferUserResponse
import com.skithub.resultdear.agent.model.response.ServerResponse
import io.reactivex.rxjava3.core.Single

class ApiRepo(private val api: IRetrofitApiCall) {

    fun isUserExits() : Single<UserLogin> = api.isUserExits()

    fun signUp(name:String, email:String, age:String, gender:Int, photoUrl:String, phone:String) :Single<ServerResponse>{
        return api.signUp(name, email, age, gender, photoUrl, phone)
    }

    fun getAllReferUser() : Single<ReferUserResponse> = api.getAllReferUser()

}