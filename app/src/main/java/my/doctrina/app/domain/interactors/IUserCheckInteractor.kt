package my.doctrina.app.domain.interactors

interface IUserCheckInteractor {
    fun test()

    fun isUserTokenExpired(accessExpired: Int): Boolean
}