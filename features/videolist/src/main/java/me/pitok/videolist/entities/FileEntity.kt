package me.pitok.videolist.entities

data class FileEntity(
    val path: String,
    val type: Int,
    val name: String,
    val details: String = ""
){
    companion object{
        const val FILE_TYPE = 0
        const val FOLDER_TYPE = 1
    }
}