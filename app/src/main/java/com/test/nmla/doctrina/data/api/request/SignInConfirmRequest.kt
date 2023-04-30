package com.test.nmla.doctrina.data.api.request

import com.google.gson.annotations.SerializedName

data class SignInConfirmRequest(
    @SerializedName("email") var email: String? = null,
    @SerializedName("code") var code: String? = null
)
