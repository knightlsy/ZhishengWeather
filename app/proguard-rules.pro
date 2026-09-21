# TianQi Weather ProGuard rules
-keep class com.tianqi.weather.model.** { *; }
-keep class com.tianqi.weather.data.SecretStore { *; }
-keep class com.tianqi.weather.data.WeatherCache { *; }
-keepclassmembers class com.tianqi.weather.model.** { *; }
-keepclassmembers class com.tianqi.weather.data.** { *; }
