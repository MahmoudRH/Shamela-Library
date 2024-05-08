# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keep class org.readium.r2.streamer.** {
    <init>(...);
    *;
}
#ParametrizedType ClassCastException error fix
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.** { *; }

-dontwarn org.joda.convert.**
-keep class com.shamela.library.data.** {*;}
-keep class com.shamela.library.domain.** {*;}
-keep class com.shamela.library.presentation.** { *; }
-keep class com.shamela.apptheme.** {*;}
-keep class com.folioreader.mediaoverlay.** {*;}
-keep class com.folioreader.model.** {*;}
-keep class com.folioreader.network.** {*;}
-keep class com.folioreader.ui.** {*;}
-keep class com.folioreader.util.** {*;}
-keep class com.folioreader.viewmodels.** {*;}
-keep class com.folioreader.** {*;}


