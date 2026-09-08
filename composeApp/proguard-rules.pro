-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keep class ir.cutte.nava.model.** { *; }
-keep class ir.cutte.nava.receiver.** { *; }
-keep class ir.cutte.nava.service.** { *; }

-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }
