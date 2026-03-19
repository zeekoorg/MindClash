plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    // 🚀 الإضافة الجديدة الخاصة بـ Compose في Kotlin 2.0
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    // 🚀 إضافة خدمات جوجل
    id("com.google.gms.google-services") version "4.4.2" apply false
}

tasks.register("clean", Delete::class) {
    // تم تحديث هذا السطر ليتوافق مع أحدث إصدارات Gradle
    delete(rootProject.layout.buildDirectory)
}
