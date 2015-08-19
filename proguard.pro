#-dontoptimize
-dontobfuscate
#-dontpreverify
#-dontnote
-dontwarn javax.annotation.**,com.android.annotations.**,org.kxml2.**

# Keep - Applications. Keep all application classes, along with their 'main'
# methods.
-keep public class com.madvay.tools.android.perf.apat.Main {
    public static void main(java.lang.String[]);
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

#-optimizationpasses 3
#-overloadaggressively
