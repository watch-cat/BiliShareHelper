package com.watchcats.bilisharehelper.url.restoration

import java.util.regex.Matcher
import java.util.regex.Pattern

object GetURLFromText {

    fun getURL(inputText: String): String? {
        val patternStr = "((www\\.|http://|https://)(www\\.)*.*?(?=(www\\.|http://|https://|\$)))"

        val pattern: Pattern = Pattern.compile(patternStr)
        val matcher: Matcher = pattern.matcher(inputText)

        return if (matcher.find()) matcher.group(0)
        else "Fail to find URL"
    }

    fun replaceBiliToB23(inputText: String): String{
        return inputText.replace("www.bilibili.com/video", "b23.tv", true)
    }

}