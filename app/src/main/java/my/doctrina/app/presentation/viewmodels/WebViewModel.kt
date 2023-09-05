package my.doctrina.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import my.doctrina.app.domain.interactors.IUserCheckInteractor
import javax.inject.Inject

@HiltViewModel
class WebViewModel @Inject constructor(
    private val userCheck: IUserCheckInteractor
) : ViewModel() {

    fun test() = userCheck.test()

}