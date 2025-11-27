/**
 * 项目公共配置文件
 * 统一管理版本号
 */

// 版本配置
val versionCode = 100
val versionName = "1.0.0"
val appName = "banana"
val channel = "test001"

// 设置到rootProject.extra，供app模块使用
rootProject.extra.apply {
    set("versionCode", versionCode)
    set("versionName", versionName)
    set("appName", appName)
    set("channel", channel)
}

println("=================> Configs loaded!")
println("=================> Version: v$versionName ($versionCode)")