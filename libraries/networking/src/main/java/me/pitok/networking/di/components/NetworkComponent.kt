package me.pitok.networking.di.components

import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.networking.di.modules.NetworkModule
import me.pitok.networking.di.scopes.NetworkScope
import me.pitok.sharedpreferences.di.components.SharedPreferencesComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@NetworkScope
@Component(modules = [NetworkModule::class],
    dependencies = [
        SharedPreferencesComponent::class,
        AndroidCoreComponent::class
    ])
interface NetworkComponent{

    fun providePkHttpClientCache(): Cache

    fun provideOkHttpClient(): OkHttpClient

    fun provideGsonConverter(): GsonConverterFactory

    fun getRetrofit(): Retrofit

}