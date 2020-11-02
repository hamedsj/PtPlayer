package me.pitok.firebase.di.modules

import dagger.Binds
import dagger.Module
import me.pitok.firebase.di.scopes.FcmScope
import me.pitok.firebase.repository.FcmTokenRefreshable
import me.pitok.firebase.repository.FcmTokenRefresher

@Module
interface FcmRepositoryModule {

    @Binds
    @FcmScope
    fun provideFcmRefresher(fcmTokenRefresher: FcmTokenRefresher): FcmTokenRefreshable

}