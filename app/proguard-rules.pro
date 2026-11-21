# Keep reflection-based models and quran-text dataset classes intact when R8 is enabled
-keep class ws.quran.** { *; }
-dontwarn ws.quran.**

-keepclassmembers class com.example.domain.model.** { *; }
-keepclassmembers class com.example.data.remote.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature,InnerClasses,EnclosingMethod
