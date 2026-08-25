# --------------------------------------------------------
# 1. 基本指令
# --------------------------------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,EnclosingMethod

# --------------------------------------------------------
# 2. Android 系统组件与核心
# --------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Fragment 保护
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment

# View 保护
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 序列化（防止跨页面数据丢失）
-keep class * implements java.io.Serializable { *; }
-keep class * implements android.os.Parcelable { *; }
-keep class * implements android.os.Parcelable$Creator { *; }

# R 文件
-keep class **.R$* {*;}

# --------------------------------------------------------
# 3. 项目核心包（完全匹配你当前目录结构）
# --------------------------------------------------------
-keep class com.hy.greenbuilding.adapter.** { *; }
-keep class com.hy.greenbuilding.config.** { *; }
-keep class com.hy.greenbuilding.event.** { *; }
-keep class com.hy.greenbuilding.model.** { *; }
-keep class com.hy.greenbuilding.mqtt.** { *; }
-keep class com.hy.greenbuilding.presenter.** { *; }
-keep class com.hy.greenbuilding.protocol.** { *; }
-keep class com.hy.greenbuilding.receiver.** { *; }
-keep class com.hy.greenbuilding.service.** { *; }
-keep class com.hy.greenbuilding.ui.** { *; }
-keep class com.hy.greenbuilding.utils.** { *; }
-keep class com.hy.greenbuilding.HyApplication { *; }

# --------------------------------------------------------
# 4. 第三方库（保持你原有的配置）
# --------------------------------------------------------
# Gson
-keep class com.google.gson.** { *; }

# GreenDao
-keep class org.greenrobot.greendao.** {*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties { *; }

# EventBus
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **_ViewBinding { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Retrofit / OkHttp / RxJava
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }
-dontwarn rx.**
-keep class rx.** { *; }
-keep class io.reactivex.** { *; }

# MQTT & 串口
-dontwarn org.eclipse.paho.**
-keep class org.eclipse.paho.** { *; }
-keep class com.hwellyi.smarthome.** { *; }
-keep class com.github.f1reking.serialport.** { *; }

# Glide / Zxing / Material Dialogs / Logger
-keep public class * implements com.bumptech.glide.module.GlideModule
-dontwarn com.bumptech.glide.**
-keep class com.google.zxing.** {*;}
-keep class com.afollestad.materialdialogs.** { *; }
-keep class com.orhanobut.logger.** { *; }