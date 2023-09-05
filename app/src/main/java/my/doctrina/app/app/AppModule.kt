package my.doctrina.app.app

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.doctrina.app.domain.interactors.UserCheckInteractor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserCheckInteractor(): UserCheckInteractor {
        return UserCheckInteractor()
    }
}