package com.skithub.resultdear.agent.utils.api

import com.skithub.resultdear.agent.model.UserLogin
import com.skithub.resultdear.agent.model.response.PlanListResponse
import com.skithub.resultdear.agent.model.response.ReferUserResponse
import com.skithub.resultdear.agent.model.response.ServerResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


interface IRetrofitApiCall {

    @GET("account.isUserExits.php")
    fun isUserExits(): Single<UserLogin>

    @FormUrlEncoded
    @POST("account.signup.php")
    fun signUp(
        @Field("name") name:String,
        @Field("email") email:String,
        @Field("age") age:String,
        @Field("gender") gender:Int,
        @Field("profile_url") photoUrl:String,
        @Field("phone") phone:String,
    ): Single<ServerResponse>

    @GET("user.getAllReferUser.php")
    fun getAllReferUser(): Single<ReferUserResponse>

    @FormUrlEncoded
    @POST("user.getUserPlan.php")
    fun getUserPlan(
        @Field("userId") name:String
    ): Single<PlanListResponse>

}