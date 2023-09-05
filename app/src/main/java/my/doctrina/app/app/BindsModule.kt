package my.doctrina.app.app

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.doctrina.app.domain.interactors.IUserCheckInteractor
import my.doctrina.app.domain.interactors.UserCheckInteractor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BindsModule {

    @Binds
    @Singleton
    fun bindUserCheckInteractor(userCheck: UserCheckInteractor) : IUserCheckInteractor
}