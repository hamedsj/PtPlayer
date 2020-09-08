package me.pitok.sdkextentions

fun String.en2pr(): String {
    val persianNumbers = listOf("۰","۱","۲","۳","۴","۵","۶","۷","۸","۹")
    var result = this
    for (i in persianNumbers.indices){
        result = result.replace("$i",persianNumbers[i])
    }
    return result
}