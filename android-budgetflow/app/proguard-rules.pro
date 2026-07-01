# Financial GPS ProGuard rules

# Keep serializable data classes
-keepclassmembers class com.rork.budgetflow.data.** {
    <fields>;
}

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rork.budgetflow.**$$serializer { *; }
-keepclassmembers class com.rork.budgetflow.** {
    *** Companion;
}
-keepclasseswithmembers class com.rork.budgetflow.** {
    kotlinx.serialization.KSerializer serializer(...);
}
