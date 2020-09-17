package me.pitok.logger

import android.util.Log

class Logger {

    companion object{
        private const val TAG = "PTPLAYER_DEV_LOG"
        fun e(message: String?){
            if (BuildConfig.DEBUG && message != null){
                Log.e(TAG,message)
            }
        }
        fun i(message: String?){
            if (BuildConfig.DEBUG && message != null){
                Log.i(TAG,message)
            }
        }
        fun d(message: String?){
            if (BuildConfig.DEBUG && message != null){
                Log.d(TAG,message)
            }
        }
        fun v(message: String?){
            if (BuildConfig.DEBUG && message != null){
                Log.v(TAG,message)
            }
        }
        fun w(message: String?){
            if (BuildConfig.DEBUG && message != null){
                Log.w(TAG,message)
            }
        }
    }

}