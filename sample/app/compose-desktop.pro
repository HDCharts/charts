# PostHog persists events and API models through Gson reflection.
-keepattributes Signature,*Annotation*
-keep class com.posthog.** { *; }
-keep class com.google.gson.** { *; }

# Preserve Okio bridge methods and OkHttp's public suffix resource in release builds.
-keep class okio.** { *; }
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

-dontwarn android.**
-dontwarn dalvik.**
-dontwarn javax.annotation.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn com.jetbrains.SharedTextures

# OkHttp's GraalVM native-image hooks are optional in a JVM distribution.
-dontwarn okhttp3.internal.graal.**
