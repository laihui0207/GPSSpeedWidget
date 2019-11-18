# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/Cellar/android-sdk/24.4.1_1/tools/proguard/proguard-android.txt
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
#-------------------------------------------基本不用动区域--------------------------------------------
#---------------------------------基本指令区----------------------------------
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-printmapping proguardMapping.txt
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------

#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}
-keep class com.autonavi.** {*;}
-keep class com.baidu.** {*;}
-dontwarn okhttp3.**
-dontwarn okio.**
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepnames class * implements com.amap.api.navi.AMapNaviListener {
    *;
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
#----------------------------------------------------------------------------
-libraryjars libs/AMap3DMap_7.1.0_AMapNavi_7.1.0_AMapSearch_7.1.0_AMapTrack_1.1.0_AMapLocation_4.7.2_20191030.jar
-libraryjars libs/com.baidu.tts_2.3.2.20180713_6101c2a.jar
-libraryjars libs/kwmusic-autosdk-v2.0.2.jar
#---------------------------------webview------------------------------------
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}
#----------------------------------------------------------------------------
#aMap
-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.**{*;}
-keep   class com.amap.api.trace.**{*;}
-keep class com.amap.api.location.**{*;}
-keep   class com.amap.api.services.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}
# baidu tts
-keep class com.baidu.speech.**{*;}
-keep class com.baidu.tts.**{*;}
-keep class com.baidu.speechsynthesizer.**{*;}
# ify
-keep class com.iflytek.**{*;}
#keep Util
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep public class com.aispeech.common.Util{public *;}
-keep public class com.aispeech.common.WavFileWriter{public *;}
-keep public class com.aispeech.common.AITimer{public *;}
-keep public class com.aispeech.common.JSONResultParser{public *;}
-keep public class com.aispeech.common.AIConstant{public *;}
-keep public class com.aispeech.common.FileUtil{public *;}
-keep public class com.aispeech.DUILiteSDK{public *;}
-keep public class com.aispeech.fdm.**{public *;}
-keep public class com.aispeech.echo.**{public *;}

-keep class com.aispeech.upload.**{*;}

-keepclassmembers class com.aispeech.kernel.**{
	public static native <methods>;
}

-keep interface com.aispeech.kernel.**$*{
	public *;
}

-keep class com.aispeech.kernel.**$*{
	public *;
}

-keep interface com.aispeech.DUILiteSDK$*{
    public *;
}
#---------------------------------------------------------------------------------------------------
#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class org.apache.** { *;}
-keep public class com.huivip.gpsspeedwidget.GPSUtil.AutoNaviListener.** { *;}
-keep public class devlight.io.** { *;}
-keep public class com.github.bumptech.glide.** { *;}
-keep public class com.jakewharton.** { *;}
-keep public class io.reactivex.** { *;}
-keep public class com.github.pluscubed.** { *;}
-keep class com.zlm.hp.lyrics.** { *; }
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
##---------------EventBus-------------------------------
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}