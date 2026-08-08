# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Faz 6: release build R8 full-mode ile "Failed to create an instance of
# androidx.work.impl.WorkDatabase" crash'i verdi (play-services-ads'in
# transitive WorkManager+Room bagimliligi, Kotlin metadata/annotation'lari
# kirpilinca reflection ile calisamadi). Standart, resmi olarak belgelenmis
# duzeltme:
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keep class kotlin.Metadata { *; }
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**
