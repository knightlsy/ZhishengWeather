import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// 和风天气凭据（不入库，仅存 local.properties）
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.canRead()) load(f.inputStream())
}
fun lp(key: String, def: String = ""): String = localProps.getProperty(key, def)

// 公开版构建开关：./gradlew assembleRelease -PpublicBuild
// 强制和风凭据为空 + 换用随库公开证书，保证 Release 分发的 APK 不含个人凭据
val publicBuild = providers.gradleProperty("publicBuild").isPresent

android {
    namespace = "com.zhisheng.weather"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zhisheng.weather"
        minSdk = 26
        targetSdk = 34
        // 20260821：0.0.5.1（小组件字号放大/预览图上海/主题只跟系统）——> 20260820 可覆盖安装 0.0.5
        versionCode = 20260821
        versionName = "0.0.5.1"

        buildConfigField("String", "QW_HOST", "\"${if (publicBuild) "" else lp("qw.host")}\"")
        buildConfigField("String", "QW_PROJECT_ID", "\"${if (publicBuild) "" else lp("qw.project_id")}\"")
        buildConfigField("String", "QW_KID", "\"${if (publicBuild) "" else lp("qw.kid")}\"")
        buildConfigField("String", "QW_PRIVATE_KEY", "\"${if (publicBuild) "" else lp("qw.private_key")}\"")
    }

    signingConfigs {
        create("release") {
            val props = Properties()
            val f = rootProject.file("local.properties")
            if (f.canRead()) props.load(f.inputStream())
            if (publicBuild) {
                // 公开版：随库公开证书（密码公开即其设计，仅保证安装/升级签名一致）
                storeFile = project.rootProject.file("keystore/public.jks")
                storePassword = "public123"
                keyAlias = "public"
                keyPassword = "public123"
            } else {
                storeFile = project.rootProject.file("keystore/zhisheng.jks")
                storePassword = props.getProperty("keystore.store_password") ?: "zhisheng123"
                keyAlias = "zhisheng"
                keyPassword = props.getProperty("keystore.key_password") ?: "zhisheng123"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        // android.util.Log 等在纯 JVM 单测中返回默认值而非抛「not mocked」
        //（SourceHealth 熔断等逻辑的日志路径进入单测，v0.0.4）
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.tooling.preview)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.bouncycastle)
    implementation(libs.work.runtime.ktx)
    testImplementation(libs.junit)
    debugImplementation(libs.compose.tooling)
}
