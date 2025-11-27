import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

// 应用公共配置文件
apply(from = "../script/configs.gradle.kts")

plugins {
    alias(libs.plugins.android.application)
}

// 辅助函数：获取当前时间戳
fun releaseTime(): String {
    return SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
}

// 辅助函数：获取当前日期
fun currentData(): String {
    return SimpleDateFormat("yy-MM-dd").format(Date())
}

android {
    namespace = "com.mhyyu.qyunn.testpackkotlin"
    compileSdk = 35

    bundle {
        language {
            enableSplit = false
        }
        density {
            // This property is set to true by default.
            enableSplit = false
        }
        abi {
            // This property is set to true by default.
            enableSplit = false
        }
    }

    defaultConfig {
        applicationId = "com.mhyyu.qyunn.testpackkotlin"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        ndk {
            //设置支持的SO库架构
            abiFilters += setOf("armeabi-v7a", "arm64-v8a")
        }

        androidResources {
            localeFilters += setOf("en", "zh")
        }
    }

    signingConfigs {

        create("test1") {
            storeFile = file("../jks/funny.jks")
            storePassword = "calculator123123126"
            keyAlias = "key0"
            keyPassword = "calculator123123126"
        }

//        create("test2") {
//            storeFile = file("../jks/test2.jks")
//            storePassword = "123456"
//            keyAlias = "key0"
//            keyPassword = "123456"
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true  // 开启资源压缩
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            // Debug版本也可以使用签名配置
            // signingConfig = signingConfigs.getByName("test2")
            signingConfig = null
        }
    }

    flavorDimensions += listOf("type", "version")

    productFlavors {

        create("guonei") {
            dimension = "type"
        }

//        create("guowai") {
//            dimension = "type"
//        }

        create("funny") {
            signingConfig = signingConfigs.getByName("test1")
            dimension = "version"
            resValue("string", "app_name", rootProject.extra.get("appName") as String)
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    /**
     * 自定义apk文件名和输出路径
     */
    applicationVariants.all {
        outputs.all {
            if (outputFile.name.endsWith(".apk")) {
                if (buildType.name == "release") {
                    // 配置值
                    val appName = rootProject.extra.get("appName") as String
                    val channel = rootProject.extra.get("channel") as String
                    val isDebug = false

                    // 生成文件名
                    val fileName = "${appName}_v${defaultConfig.versionName}_${defaultConfig.versionCode}_${releaseTime()}_${channel}_${if (isDebug) "Debug" else "Release"}.apk"

                    // 生成输出路径（相对于app/build/outputs/apk）
                    val dateFolder = SimpleDateFormat("yy-MM-dd").format(Date())
                    val outputDir = File("../../../../../../outputApk/$dateFolder")

                    // 设置输出文件名
                    (this as BaseVariantOutputImpl).outputFileName = File(outputDir, fileName).toString()

                    println("===========> Custom APK output: ${File(outputDir, fileName)}")
                }
            }
        }

        /**
         * APK的mapping文件重命名和复制
         */
        if (buildType.isMinifyEnabled) {
            assembleProvider.get().doLast {
                // 输出目录
                val desktopDir = File(rootProject.rootDir, "outputMapping")
                val dateFolder = SimpleDateFormat("yy-MM-dd").format(Date())
                val mappingDir = File(desktopDir, dateFolder)

                // 确保目录存在
                if (!mappingDir.exists()) {
                    mappingDir.mkdirs()
                }

                // 获取mapping文件（直接使用mappingFile属性）
                val variantMappingFile = mappingFile

                // 检查mapping文件是否存在
                if (variantMappingFile.exists()) {
                    val appName = rootProject.extra.get("appName") as String
                    val versionCode = defaultConfig.versionCode
                    val versionName = defaultConfig.versionName
                    val copyName = "${appName}_v${versionName}_${versionCode}_${releaseTime()}_apk.txt"

                    copy {
                        from(variantMappingFile)
                        into(mappingDir)
                        rename { copyName }
                    }

                    println("===========> APK Mapping file copied to: ${File(mappingDir, copyName)}")
                } else {
                    println("===========> Warning: APK Mapping file not found at: $variantMappingFile")
                }
            }
        }
    }
}


/**
 * AAB文件重命名和mapping文件复制
 */
tasks.whenTaskAdded {
    if (name.startsWith("bundle")) {
        println("=================================================> task.name: $name")

        // 从"bundleGuoneiFunnyRelease"提取"guoneiFunnyRelease"
        val flavor = name.substring("bundle".length).replaceFirstChar { it.lowercase() }
        println("=================================================> flavor: $flavor")

        // 配置变体名称
        val curVariantsName = "guoneiFunny"
        val variantNameRelease = "${curVariantsName}Release"

        if (flavor.equals(variantNameRelease, ignoreCase = true)) {
            println("==================================================================================================================================================> equalsIgnoreCase1")
            println("=================> equalsIgnoreCase2 flavor: $flavor")

            val renameTaskName = "rename${name.replaceFirstChar { it.uppercase() }}Aab"

            // 配置值
            val appName = rootProject.extra.get("appName") as String
            val group = "guonei"
            val currentFlavor = "funny"
            val channel = "test1"
            val isDebug = false

            // 获取版本信息
            val androidExt = project.extensions.getByType(com.android.build.gradle.AppExtension::class.java)
            val versionName = androidExt.defaultConfig.versionName ?: "1.0"
            val versionCode = androidExt.defaultConfig.versionCode ?: 1

            // 原始AAB文件路径和名称
            val fromPath = File(layout.buildDirectory.get().asFile, "outputs/bundle/$flavor/")
            val archive = "${project.name}-$group-$currentFlavor-release.aab"
            println("=================> equalsIgnoreCase3 fromPath: $fromPath")
            println("=================> equalsIgnoreCase3 archive: $archive")

            // 新的AAB文件名
            val myName = "${appName}_v${versionName}_${versionCode}_${releaseTime()}_${channel}_${if (isDebug) "Debug" else "Release"}.aab"
            val dest = File(rootProject.projectDir, "outputApk/${currentData()}/")
            println("=================> dest: $dest")

            // 创建重命名AAB的任务
            val renameTask = tasks.register(renameTaskName) {
                doLast {
                    copy {
                        println("=================> copy AAB file")
                        from(File(fromPath, archive))
                        println("=================> from: ${File(fromPath, archive)}")
                        into(dest)
                        println("=================> into: $dest")
                        rename { myName }
                    }
                }
            }

            finalizedBy(renameTask)

            // 创建复制mapping文件的任务
            val copyMappingTaskName = "copy${name.replaceFirstChar { it.uppercase() }}Mapping"
            val mappingFromPath = File(layout.buildDirectory.get().asFile, "outputs/mapping/$flavor/")
            val mappingDestDir = File(rootProject.projectDir, "outputMapping/${currentData()}/")

            // 确保目录存在
            if (!mappingDestDir.exists()) {
                mappingDestDir.mkdirs()
            }

            val mappingFileName = "${appName}_v${versionName}_${versionCode}_${releaseTime()}_aab.txt"

            val copyMappingTask = tasks.register(copyMappingTaskName) {
                doLast {
                    val mappingFile = File(mappingFromPath, "mapping.txt")
                    if (mappingFile.exists()) {
                        copy {
                            println("=================> copy mapping file")
                            println("=================> from: $mappingFile")
                            from(mappingFile)
                            into(mappingDestDir)
                            rename { mappingFileName }
                        }
                        println("=================> mapping file copied to: ${File(mappingDestDir, mappingFileName)}")
                    } else {
                        println("=================> Warning: mapping.txt not found at $mappingFile")
                        println("=================> Make sure minifyEnabled is set to true in release buildType")
                    }
                }
            }

            renameTask.configure {
                finalizedBy(copyMappingTask)
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}