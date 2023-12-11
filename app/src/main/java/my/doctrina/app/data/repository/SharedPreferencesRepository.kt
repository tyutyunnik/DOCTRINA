package my.doctrina.app.data.repository

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveImageUrl(imageUrlKey: String, urlValue: String) {
        sharedPreferences.edit().putString(imageUrlKey, urlValue).apply()
    }

    fun getImageUrl(imageUrlKey: String, defaultUrlValue: String): String {
        return sharedPreferences.getString(imageUrlKey, defaultUrlValue) ?: defaultUrlValue
    }

    fun saveEmail(emailKey: String, emailValue: String) {
        sharedPreferences.edit().putString(emailKey, emailValue).apply()
    }

    fun getEmail(emailKey: String, defaultEmailValue: String): String {
        return sharedPreferences.getString(emailKey, defaultEmailValue) ?: defaultEmailValue
    }

    fun saveUserData(
        accessExpired: Int,
        accessToken: String,
        refreshExpired: Int,
        refreshToken: String,
        success: Boolean?
    ) {
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

    fun getAccessExpirationUserData(accessExpiredKey: String, defaultAccessExpiredValue: Int): Int {
        return sharedPreferences.getInt(accessExpiredKey, defaultAccessExpiredValue)
    }

    fun saveAccessUserToken(tokenKey: String, tokenValue: String) {
        sharedPreferences.edit().putString(tokenKey, tokenValue).apply()
    }

    fun getAccessUserToken(accessTokenKey: String, defaultAccessTokenValue: String): String {
        return sharedPreferences.getString(accessTokenKey, defaultAccessTokenValue)
            ?: defaultAccessTokenValue
    }

    fun getRefreshUserToken(refreshTokenKey: String, defaultRefreshTokenValue: String): String {
        return sharedPreferences.getString(refreshTokenKey, defaultRefreshTokenValue)
            ?: defaultRefreshTokenValue
    }

    fun getRefreshExpirationUserData(
        refreshExpiredKey: String,
        defaultRefreshExpiredValue: Int
    ): Int {
        return sharedPreferences.getInt(refreshExpiredKey, defaultRefreshExpiredValue)
    }

    fun getStatusUserData(successKey: String, defaultSuccessValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(successKey, defaultSuccessValue)
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