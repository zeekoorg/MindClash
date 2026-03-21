plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.zeeko.mindclash"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zeeko.mindclash"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    // 🚀 واجهات Compose الحديثة
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // 🧭 التنقل بين الشاشات
    implementation("androidx.navigation:navigation-compose:2.8.0")
    
    // 🗄️ قاعدة بيانات Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // 💉 حقن الاعتمادات (Hilt)
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // 💰 إعلانات ياندكس + وساطة Unity و IronSource
    implementation("com.yandex.android:mobileads:7.18.2")
    implementation("com.yandex.ads.mediation:mobileads-unityads:4.16.4.0")
    implementation("com.yandex.ads.mediation:mobileads-ironsource:9.0.0.0")
    
    // 🆔 معرفات الإعلانات (لجوجل وهواوي معاً!)
    implementation("com.google.android.gms:play-services-ads-identifier:18.1.0")
    implementation("com.huawei.hms:ads-identifier:3.4.62.300")
    
    // 🛠️ أدوات التصميم السحري (Lottie & Glassmorphism)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation("dev.chrisbanes.haze:haze:0.7.1")
}
