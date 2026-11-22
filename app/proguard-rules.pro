# Preserve Quran dataset models and reflection-compatible packages under R8
-keep class ws.quran.** { *; }
-dontwarn ws.quran.**

-keepclassmembers class com.example.domain.model.** { *; }
-keepclassmembers class com.example.domain.prayer.** { *; }
-keepclassmembers class com.example.domain.wird.** { *; }
-keepclassmembers class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.adhkar.** { *; }
-keepclassmembers class com.example.data.remote.** { *; }

# Keep @Keep annotated classes and members
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep Manifest-registered Android components and widget providers
-keep public class * extends android.appwidget.AppWidgetProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Service
-keep class androidx.core.content.FileProvider { *; }

# DataStore Preferences / Protobuf protection for R8 full mode
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Enum preservation for serialization and string mapping
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# OkHttp / Okio / Conscrypt / JSSE optional platform warnings
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Retain attributes required for generics, annotations, and readable stack traces
-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
