package me.pitok.firebase.di.modules

import dagger.Module
import dagger.Provides
import me.pitok.firebase.di.scopes.FcmScope
import me.pitok.firebase.repository.apis.FcmApiInterface
import retrofit2.Retrofit

@Module
class FcmApiModule {

    @Provides
    @FcmScope
    fun provideNeewsApiInterface(retrofit: Retrofit): FcmApiInterface{
        return retrofit.create(FcmApiInterface::class.java)
    }

}