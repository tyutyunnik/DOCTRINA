package my.doctrina.app.data.api.response

import com.google.gson.annotations.SerializedName

data class SignInResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("error") var error: String? = null
)
