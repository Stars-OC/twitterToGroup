package xyz.starsoc.file

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object TwitterInfo : AutoSavePluginData("TwitterInfo"){
    val twitterUser : Map<String,Long> by value()
    val lastTweetId : Map<Long,Long> by value()
}