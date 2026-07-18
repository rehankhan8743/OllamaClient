-keepattributes *Annotation*
-keep class com.rehan.ollamaclient.data.remote.model.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn org.jetbrains.kotlinx.**
