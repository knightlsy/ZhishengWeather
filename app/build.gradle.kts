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
// 官方分发包可在构建时注入社区群号；默认留空，公开源码不包含外部联系信息。
val communityQqGroup = providers.gradleProperty("communityQqGroup")
    .orElse("")
    .get()
    .filter(Char::isDigit)

android {
    namespace = "com.zhisheng.weather"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zhisheng.weather"
        minSdk = 26
        targetSdk = 34
        // 20260825：0.1.0 模块排序 / 城市卡组 / 终端腕表小组件
        versionCode = 20260826
        versionName = "0.1.0"

        buildConfigField("String", "QW_HOST", "\"${if (publicBuild) "" else lp("qw.host")}\"")
        buildConfigField("String", "QW_PROJECT_ID", "\"${if (publicBuild) "" else lp("qw.project_id")}\"")
        buildConfigField("String", "QW_KID", "\"${if (publicBuild) "" else lp("qw.kid")}\"")
        buildConfigField("String", "QW_PRIVATE_KEY", "\"${if (publicBuild) "" else lp("qw.private_key")}\"")
        buildConfigField("String", "COMMUNITY_QQ_GROUP", "\"$communityQqGroup\"")
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
        create("performance") {
            initWith(getByName("release"))
            // 真机性能验收：非调试构建，但沿用 debug 证书，可无损覆盖开发机上的 Debug 包。
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            matchingFallbacks += listOf("release")
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
