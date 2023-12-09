package my.doctrina.app.data.shared_preferences

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferencesStorage @Inject constructor(
    private val sp: SharedPreferences
) : ISharedPreferencesStorage {

    //    override fun setFirstStart(firstStart: Boolean) {
//        sp.edit().putBoolean("ROYAL", firstStart).apply()
//    }
    override fun setImageUrl(imageUrl: String) {
        sp.edit().putString("imageUrl", imageUrl).apply()
    }

}