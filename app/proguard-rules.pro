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



### Models
-keep class com.github.anastr.data.models.** { *; }

### Default arge for navigation component
-keepnames class com.github.anastr.domain.entities.Semester
-keepnames class com.github.anastr.myscore.CourseMode

### MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

### RxJava
-keep class io.reactivex.rxjava3.disposables.RunnableDisposable { <init>(...); }