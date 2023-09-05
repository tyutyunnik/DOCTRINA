package my.doctrina.app.domain.interactors

import android.util.Log

class UserCheckInteractor : IUserCheckInteractor {
    override fun test() {
        Log.d("test", "test")
    }
}