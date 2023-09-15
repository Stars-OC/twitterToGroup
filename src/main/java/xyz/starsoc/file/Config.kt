package xyz.starsoc.file

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("config") {
    val bot : Long by value()
    @ValueDescription("最高权限")
    val master : Long by value()

    val proxyHost by value("127.0.0.1")
    val proxyPort by value(10809)
    val time by value(3000)

    val username by value("")
    val password by value("")
    val cookies by value("")
    val enableMirror by value(true)
    val mirrorList by value(mutableSetOf("https://nitter.unixfox.eu/"))
}