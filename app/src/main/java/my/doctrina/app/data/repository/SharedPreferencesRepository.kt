package my.doctrina.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import my.doctrina.app.data.api.response.SignInConfirmResponse

private const val EMAIL_KEY = "email"

class SharedPreferencesRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // TODO: imageUrl and email change to get|set sting and to do constants with key values
    fun saveImageUrl(imageUrlKey: String, urlValue: String) {
        sharedPreferences.edit().putString(imageUrlKey, urlValue).apply()
    }

    fun getImageUrl(imageUrlKey: String, defaultUrlValue: String): String {
        return sharedPreferences.getString(imageUrlKey, defaultUrlValue) ?: defaultUrlValue
    }

    fun saveEmail(emailValue: String) {
        sharedPreferences.edit().putString(EMAIL_KEY, emailValue).apply()
    }

    fun getEmail(): String {
        return sharedPreferences.getString(EMAIL_KEY, "") ?: ""
    }

    private fun saveUserData(
        accessExpired: Int,
        accessToken: String,
        refreshExpired: Int,
        refreshToken: String,
        success: Boolean?
    ) {

        // TODO: constants with more than one keys
        val editor = sharedPreferences.edit()
        editor.putInt("access_expired", accessExpired)
        editor.putString("access_token", accessToken)
        editor.putInt("refresh_expired", refreshExpired)
        editor.putString("refresh_token", refreshToken)
        if (success != null) {
            editor.putBoolean("success", success)
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
        return sharedPreferences.getInt("access_expired", 0)
    }

    // TODO: save method gets token value. fix it
    fun saveAccessUserToken() {
        sharedPreferences.edit().putString("access_token", "").apply()
    }

    fun getAccessUserToken(): String {
        return sharedPreferences.getString("access_token", "") ?: ""
    }

    fun getRefreshUserToken(): String {
        return sharedPreferences.getString("refresh_token", "") ?: ""
    }

    fun getRefreshExpirationUserData(): Int {
        return sharedPreferences.getInt("refresh_expired", 0)
    }

    fun getStatusUserData(): Boolean {
        return sharedPreferences.getBoolean("success", false)
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