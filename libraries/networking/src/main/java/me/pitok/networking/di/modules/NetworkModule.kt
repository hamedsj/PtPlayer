package me.pitok.networking.di.modules

import android.content.Context
import dagger.Lazy
import dagger.Module
import dagger.Provides
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.networking.di.scopes.NetworkScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    companion object {
        const val OKHTTP_MAX_CACHE_SIZE = (50 * 1024 * 1024).toLong()
        const val SERVER_BASE_URL = ""
    }

    @Provides
    @NetworkScope
    fun providePkHttpClientCache( @ApplicationContext
        context: Context
    ): Cache {
        return Cache(context.cacheDir,
            OKHTTP_MAX_CACHE_SIZE
        )
    }

    @Provides
    @NetworkScope
    fun provideOkHttpClient(
        cache: Cache?
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(requireNotNull(cache))
            .build()
    }

    @Provides
    @NetworkScope
    fun provideGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @NetworkScope
    fun getRetrofit(
        okHttpClient: Lazy<OkHttpClient>,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .callFactory { request ->
                okHttpClient.get().newCall(request)
            }
            .baseUrl(SERVER_BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }
}