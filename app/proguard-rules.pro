# ProGuard & R8 Optimization Rules for UP Sonod (ইউপি সনদ)
# Ensures Room, Retrofit, Moshi, Coroutines, WorkManager, and PDF generation work properly in Release.

# 1. General Attributes & Debugging Information
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# 2. Application Model & Domain Classes (Room Entities, DTOs, Serialized Objects)
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.data.remote.** { *; }
-keep class com.example.util.** { *; }
-keep class com.example.sync.** { *; }

# 3. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase$Callback {
    <init>(...);
}

# 4. WorkManager & Background Sync
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# 5. Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 6. Moshi (JSON Serialization / Deserialization)
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
    @com.squareup.moshi.* <fields>;
}
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter

# 7. Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 8. Supabase & Ktor & Kotlinx Serialization (Rules if added or used)
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class * implements kotlinx.serialization.KSerializer {
    <init>(...);
}

# 9. Native PDF Generator, Typography & Printing
-keep class com.example.util.PdfGenerator { *; }
-keep class com.example.util.PdfPrintHelper { *; }
-keep class com.example.util.BanglaHelper { *; }
-keep class com.example.util.TemplateEngine { *; }
