package my.doctrina.app.presentation.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import my.doctrina.app.data.shared_preferences.ISharedPreferencesStorage
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sharedPrefsStorage: ISharedPreferencesStorage
) {



}