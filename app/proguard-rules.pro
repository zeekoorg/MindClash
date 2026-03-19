-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class androidx.** { *; }
-keep class com.yandex.mobile.ads.** { *; }
-keep class com.zeeko.mindclash.data.models.** { *; }
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
