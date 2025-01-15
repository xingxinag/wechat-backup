-keep class com.example.myxposedmodule.MainHook {
    public void handleLoadPackage(...);
    public void initZygote(...);
}
-keepclassmembers class com.example.myxposedmodule.** {
    *;
}
-keepattributes *Annotation* 