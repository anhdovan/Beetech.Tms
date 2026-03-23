# Keep class names intact for reflection
-keepnames class beetech.app.readers.**

# Ensure constructors remain for reflection-based instantiation
-keepclassmembers class beetech.app.readers.** {
    public <init>(...);
}

# Prevent ProGuard from removing these classes
-keep class beetech.app.readers.** { *; }

# Preserve attributes related to reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keep,allowobfuscation class beetech.app.readers.**
-dontshrink
