# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Jason_Fang/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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


################################################################################
#
# OpenCV
#
-keep class org.opencv.** {*;}
#
# OpenCV
#
################################################################################


################################################################################
#
# AndroidMenifest.xml
#
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.support.v4.widget

#
# AndroidMenifest.xml
#
################################################################################



-keepclasseswithmembernames class * {
    native <methods>;
}


-keep class com.opencv.common.widget.camera.CameraViewImpl {
    public <methods>;
    public <fields>;
}

-keep class com.opencv.common.widget.IFrameCallback {
    public <methods>;
    public <fields>;
}

-keep class com.opencv.common.widget.DecorationView {
    public <methods>;
    public <fields>;
}

-keep class com.opencv.common.utility.LogHelper {
    public <methods>;
    public <fields>;
}

-keep class com.opencv.common.utility.Utilities {
    public <methods>;
    public <fields>;
}

-keep class com.opencv.common.utility.AnimationHelper {
    public <methods>;
    public <fields>;
}

-keep class com.opencv.common.utility.AnimationHelper$AnimateCallback {
    public <methods>;
    public <fields>;
}
