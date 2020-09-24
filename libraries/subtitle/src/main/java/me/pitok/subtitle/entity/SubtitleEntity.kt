package me.pitok.subtitle.entity

data class SubtitleEntity(val content: String,
                          val fromMs: Long,
                          val toMs: Long,
                          val _id: Int){
    override fun toString(): String {
        return "SubtitleEntity[ id: $_id \n content: $content \n from: $fromMs \n to: $toMs ]"
    }
}