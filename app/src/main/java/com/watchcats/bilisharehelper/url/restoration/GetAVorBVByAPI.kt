package com.watchcats.bilisharehelper.url.restoration

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object GetAVorBVByAPI {

    val avToBvUrlString = "https://api.bilibili.com/x/web-interface/view?aid="
    val bvToAvUrlString = "https://api.bilibili.com/x/web-interface/view?bvid="

    fun getAV(bvNumber: String): String{
        val biliUrl = URL(bvToAvUrlString + bvNumber)
        val textResult = getAVorBVRequest(biliUrl)

        val patternStr = "(?<=aid\":)[^\\\\s]+(?=,\"videos)"
        val pattern = Pattern.compile(patternStr)
        val matcher = pattern.matcher(textResult)

        return if (matcher.find()) matcher.group(0) as String
        else "No video found"
    }

    fun getBV(avNumber: String): String{
        val biliUrl = URL(avToBvUrlString + avNumber)
        val textResult = getAVorBVRequest(biliUrl)

        val patternStr = "(?<=bvid\":\")[^\\\\s]+(?=\",\"aid)"
        val pattern = Pattern.compile(patternStr)
        val matcher = pattern.matcher(textResult)

        return if (matcher.find()) matcher.group(0).substring(3)
        else "No video found"
    }

    private fun getAVorBVRequest(biliUrl: URL): String {
        val connection = biliUrl.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val bufferReader =
                BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))
            val stringBuffer = StringBuffer()
            var temp: String? = null
            do {
                temp = bufferReader.readLine()
                if (temp != null) {
                    stringBuffer.append(temp)
                    stringBuffer.append("\r\n")
                } else {
                    break
                }
            } while (true)

            return stringBuffer.toString()
        }else return "Connect Error"
    }

}