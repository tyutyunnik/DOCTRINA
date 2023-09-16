package my.doctrina.app.domain.interactors

import android.util.Log

class UserCheckInteractor : IUserCheckInteractor {
    override fun test() {
        Log.d("test", "test")
    }

    override fun isUserTokenExpired(accessExpired: Int): Boolean {
        val currentTime = (System.currentTimeMillis() / 1000).toInt()
        return accessExpired < currentTime
    }
}