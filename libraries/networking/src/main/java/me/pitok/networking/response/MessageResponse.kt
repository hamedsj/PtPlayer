package me.pitok.networking.response

import com.google.gson.annotations.SerializedName

data class MessageResponse(@SerializedName("message") val message: String)
