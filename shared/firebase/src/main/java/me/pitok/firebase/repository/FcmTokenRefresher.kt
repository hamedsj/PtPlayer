package me.pitok.firebase.repository

import me.pitok.datasource.Writable
import me.pitok.firebase.repository.apis.FcmApiInterface
import me.pitok.networking.*
import java.io.IOException
import javax.inject.Inject


class FcmTokenRefresher @Inject constructor(private val fcmApiInterface: FcmApiInterface) : FcmTokenRefreshable{
    override suspend fun write(input: String) {
        try {
            fcmApiInterface.refreshToken(fcm_token =  input)
        }catch (t: Throwable){
            if (BuildConfig.DEBUG){
                t.printStackTrace()
            }
        }
    }
}

typealias FcmTokenRefreshable = Writable.Suspendable<String>