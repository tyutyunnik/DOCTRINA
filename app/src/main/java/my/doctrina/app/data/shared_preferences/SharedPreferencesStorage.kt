package my.doctrina.app.data.shared_preferences

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferencesStorage @Inject constructor(
    private val sp: SharedPreferences
) : ISharedPreferencesStorage {



}