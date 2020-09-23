package me.pitok.subtitle

import me.pitok.datasource.Failure
import me.pitok.datasource.Readable
import me.pitok.datasource.Response
import me.pitok.datasource.Success
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

class SubtitleReader @Inject constructor() : SubtitleReaderType {

    private var lineNumberPattern = "(\\d+\\s)"
    private var timeStampPattern = "([\\d:,]+)"
    private val regex = "$lineNumberPattern$timeStampPattern( --> )$timeStampPattern"
    private var pattern=
        Pattern
            .compile(regex)

    override suspend fun read(input: SubtitleRequest): Response<List<SubtitleEntity>, SubtitleError> {
        val file = File(input.subtitleFilePath)
        if (!file.exists()) return Failure(SubtitleError.SubtitleFileNotFound)
        val resultList = mutableListOf<SubtitleEntity>()
        val fileContent = StringBuilder()
        var totalMatchedFound = 0
        try {
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                if (totalMatchedFound == (input.page+1)*100 && input.page != -1) break
                fileContent.append("$line\n")
                val matcher = pattern.matcher(fileContent)
                while (matcher.find()) {
                    if (input.page == -1 || totalMatchedFound+1 > input.page*100){
                        var content = ""
                        var contentLine = ""
                        while(bufferedReader.readLine().also {contentLine = it.trim()}.trim().isNotEmpty()){
                            content += "$contentLine\n"
                        }
                        resultList.add(
                            SubtitleEntity(
                                content,
                                subtitleTimeToMiliSecond(matcher.group(2).trim()),
                                subtitleTimeToMiliSecond(matcher.group(4).trim()),
                                matcher.group(1).trim().toInt()
                                )
                            )
                    }
                    fileContent.clear()
                    totalMatchedFound += 1
                }
            }
            bufferedReader.close()
            return Success(resultList)
        }catch (ioException: IOException){
            return Failure(SubtitleError.ReadingSubtitleFileError)
        }

    }
}

private fun subtitleTimeToMiliSecond(subTime: String): Long {
    var result = 0L
    val subTimeSplited = subTime.split(",")
    val nonMiliSecondSplited = subTimeSplited[0].split(":")
    result += nonMiliSecondSplited[0].toLong()*60*60*1000
    result += nonMiliSecondSplited[1].toLong()*60*1000
    result += nonMiliSecondSplited[2].toLong()*1000
    return result + subTimeSplited[1].toLong()

}

typealias SubtitleReaderType =
        Readable.Suspendable.IO<SubtitleRequest,
                @JvmSuppressWildcards Response<List<SubtitleEntity>, SubtitleError>>