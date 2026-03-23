# Preserve core interfaces and base classes
-keep public interface beetech.app.core.** { *; }
-keep public class beetech.app.core.** { *; }

# Keep DTOs and models for serialization
#-keep class beetech.app.core.dto.** { *; }
#-keep class beetech.app.core.models.** { *; }

# Protect WebSocketConnector class
#-keep class beetech.app.core.network.WebSocketConnector { *; }

# Prevent warnings for unused utility classes
-dontwarn beetech.app.core.utils.**

# Remove logging for security
-assumenosideeffects class android.util.Log { *; }

# Hide source file information
-renamesourcefileattribute SourceFile
-keepattributes InnerClasses, Signature
