package xyz.starsoc.file

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object TwitterInfo : AutoSavePluginData("TwitterInfo"){
    val twitterToGroup : Map<Long,Set<String>> by value()
    val groupPermission : Map<Long,Set<Long>> by value()
    val twitterUser : Map<String,Long> by value()
    val lastTweetId : Map<String,Long> by value()
}