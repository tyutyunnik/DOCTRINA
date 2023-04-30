package com.test.nmla.doctrina.data.api

import com.test.nmla.doctrina.data.api.request.SignInConfirmRequest
import com.test.nmla.doctrina.data.api.request.SignInRequest
import com.test.nmla.doctrina.data.api.response.SignInConfirmResponse
import com.test.nmla.doctrina.data.api.response.SignInResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DOCApi {
    @POST("sign-in")
    fun signIn(@Body signInRequest: SignInRequest) : Call<SignInResponse>

    @POST("sign-in/confirm")
    fun signInConfirm(@Body signInConfirmRequest: SignInConfirmRequest) : Call<SignInConfirmResponse>
}