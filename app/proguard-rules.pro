-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-ignorewarnings
-dontwarn
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.support.v4.app.Fragment

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    void set*(***);
    *** get*();
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-dontwarn android.support.**
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}

-keep class com.tencent.mm.sdk.** {
   *;
}

-keep class cn.sharesdk.**{*;}
-keep class cn.smssdk.**{*;}
-keep class com.mob.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-dontwarn cn.sharesdk.**
-dontwarn **.R$*

##---------------Begin: proguard configuration for Gson  ----------
	-keepattributes Signature
	-keepattributes *Annotation*
	-keep class sun.misc.Unsafe { *; }
##---------------End: proguard configuration for Gson  ----------

-keep class com.sharedream.wifiguard.sd.SD {}

-keep class * implements com.sharedream.wifiguard.sd.SD {
	*;
}

-keep class com.sharedream.wifi.sdk.** {
 	*;
}
-keep class com.sharedream.wlan.sdk.** {
 	*;
}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keep class com.sharedream.wifi.**{
    *;
}

