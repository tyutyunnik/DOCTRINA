package com.test.nmla.doctrina.data.api.response

import com.google.gson.annotations.SerializedName

data class SignInConfirmResponse(
    @SerializedName("access_expired") var accessExpired: Int? = null,
    @SerializedName("access_token") var accessToken: String? = null,
    @SerializedName("refresh_expired") var refreshExpired: Int? = null,
    @SerializedName("refresh_token") var refreshToken: String? = null,
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("error") var error: String? = null
)
