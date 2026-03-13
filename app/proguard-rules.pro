# FitLog ProGuard Rules
#
# These rules are applied when minification is enabled in the release build type.
# Currently minification is disabled (isMinifyEnabled = false), but these rules
# are prepared for when it's enabled.
#
# TODO: Enable minification and test thoroughly before release

# Room database — keep entity classes and their fields
-keep class com.fitlog.app.data.local.** { *; }

# Keep domain models that may be used with reflection
-keep class com.fitlog.app.domain.model.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
