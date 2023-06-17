package my.doctrina.app.data.api.request

import com.google.gson.annotations.SerializedName

data class SignInRequest(
    @SerializedName("email") var email: String? = null
)
