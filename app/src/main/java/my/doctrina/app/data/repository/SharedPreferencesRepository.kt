package my.doctrina.app.data.repository

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveImageUrl(imageUrl: String, url: String) {
        sharedPreferences.edit().putString(imageUrl, url).apply()
    }

    fun getImageUrl(imageUrl: String, defaultUrl: String): String {
        return sharedPreferences.getString(imageUrl, defaultUrl) ?: defaultUrl
    }

    fun saveEmail(email: String, emailValue: String) {
        sharedPreferences.edit().putString(email, emailValue).apply()
    }

    fun getEmail(email: String, defaultEmailValue: String): String {
        return sharedPreferences.getString(email, defaultEmailValue) ?: defaultEmailValue
    }

}