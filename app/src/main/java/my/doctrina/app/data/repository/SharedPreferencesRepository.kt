package my.doctrina.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import my.doctrina.app.data.api.response.SignInConfirmResponse

private const val EMAIL_KEY = "email"
private const val IMAGE_URL_KEY = "imageUrl"

object USER {
    const val ACCESS_EXPIRED_KEY = "access_expired"
    const val ACCESS_TOKEN_KEY = "access_token"
    const val REFRESH_EXPIRED_KEY = "refresh_expired"
    const val REFRESH_TOKEN_KEY = "refresh_token"
    const val SUCCESS_KEY = "success"
}

class SharedPreferencesRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // TODO: imageUrl and email change to get|set sting and to do constants with key values

    fun saveString(value: String) {
        with(sharedPreferences.edit()) {
            if (value.contains("https://")) {
                putString(IMAGE_URL_KEY, value).apply()
            } else {
                putString(EMAIL_KEY, value).apply()
            }
        }
    }

    fun getString(value: String): String {
        with(sharedPreferences) {
            return if (value.contains("url")) {
                getString(IMAGE_URL_KEY, "") ?: ""
            } else {
                getString(EMAIL_KEY, "") ?: ""
            }
        }
    }

//    fun saveImageUrl(urlValue: String) {
//        sharedPreferences.edit().putString(IMAGE_URL_KEY, urlValue).apply()
//    }

//    fun getImageUrl(): String {
//        return sharedPreferences.getString(IMAGE_URL_KEY, "") ?: ""
//    }

//    fun saveEmail(emailValue: String) {
//        sharedPreferences.edit().putString(EMAIL_KEY, emailValue).apply()
//    }

//    fun getEmail(): String {
//        return sharedPreferences.getString(EMAIL_KEY, "") ?: ""
//    }

    private fun saveUserData(
        accessExpired: Int,
        accessToken: String,
        refreshExpired: Int,
        refreshToken: String,
        success: Boolean?
    ) {

        // TODO: constants with more than one keys
        val editor = sharedPreferences.edit()
        editor.putInt(USER.ACCESS_EXPIRED_KEY, accessExpired)
        editor.putString(USER.ACCESS_TOKEN_KEY, accessToken)
        editor.putInt(USER.REFRESH_EXPIRED_KEY, refreshExpired)
        editor.putString(USER.REFRESH_TOKEN_KEY, refreshToken)
        if (success != null) {
            editor.putBoolean(USER.SUCCESS_KEY, success)
        }
        editor.apply()
    }

    fun getUserData(body: SignInConfirmResponse?) {
        if (body != null) {
            with(body) {
                val accessExpired = accessExpired
                val accessToken = accessToken.toString()
                val refreshExpired = refreshExpired
                val refreshToken = refreshToken.toString()
                val success = success

                if (accessExpired != null && refreshExpired != null) {
                    saveUserData(
                        accessExpired,
                        accessToken,
                        refreshExpired,
                        refreshToken,
                        success
                    )
                }
            }
        }
    }

    fun getAccessExpirationUserData(): Int {
        return sharedPreferences.getInt(USER.ACCESS_EXPIRED_KEY, 0)
    }

    // TODO: save method gets token value. fix it
    fun saveAccessUserToken() {
        sharedPreferences.edit().putString(USER.ACCESS_TOKEN_KEY, "").apply()
    }

    fun getAccessUserToken(): String {
        return sharedPreferences.getString(USER.ACCESS_TOKEN_KEY, "") ?: ""
    }

    fun getRefreshUserToken(): String {
        return sharedPreferences.getString(USER.REFRESH_TOKEN_KEY, "") ?: ""
    }

    fun getRefreshExpirationUserData(): Int {
        return sharedPreferences.getInt(USER.REFRESH_EXPIRED_KEY, 0)
    }

    fun getStatusUserData(): Boolean {
        return sharedPreferences.getBoolean(USER.SUCCESS_KEY, false)
    }

//    fun saveUserLoginBody(
//        accessExpired: Int,
//        accessToken: String,
//        refreshExpired: Int,
//        refreshToken: String,
//        success: Boolean
//    ) {
//        val userObject = JsonObject()
//        userObject.apply {
//            addProperty("access_expired", accessExpired)
//            addProperty("access_token", accessToken)
//            addProperty("refresh_expired", refreshExpired)
//            addProperty("refresh_token", refreshToken)
//            addProperty("success", success)
//        }
//        val authJson = JsonObject()
//        authJson.add("authenticated", userObject)
//    }

}