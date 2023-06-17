package my.doctrina.app.data.api

import my.doctrina.app.data.api.request.SignInConfirmRequest
import my.doctrina.app.data.api.request.SignInRequest
import my.doctrina.app.data.api.response.SignInConfirmResponse
import my.doctrina.app.data.api.response.SignInResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DOCApi {
    @POST("sign-in")
    fun signIn(@Body signInRequest: SignInRequest) : Call<SignInResponse>

    @POST("sign-in/confirm")
    fun signInConfirm(@Body signInConfirmRequest: SignInConfirmRequest) : Call<SignInConfirmResponse>
}