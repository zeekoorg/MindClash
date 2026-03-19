pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // 🚀 إضافة مستودع هواوي هنا أيضاً تحسباً لأي إضافات مستقبلية
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 🚀 مستودع هواوي (ضروري جداً لتحميل مكتبة المعرف الإعلاني)
        maven { url = uri("https://developer.huawei.com/repo/") }
        
        // 🚀 مستودع ironSource (ضروري جداً لتحميل وساطة الإعلانات المربحة)
        maven { url = uri("https://android-sdk.is.com/") }
    }
}

rootProject.name = "MindClash"
include(":app")
