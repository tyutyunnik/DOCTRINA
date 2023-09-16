package my.doctrina.app.data.api.response

import com.google.gson.annotations.SerializedName

data class TokenExpirationResponse(
    @SerializedName("expiration") val expiration: Long? = null
)
