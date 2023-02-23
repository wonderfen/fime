-keepnames class top.someapp.fimesdk.* {
  public *;
}

-keepnames class top.someapp.fimesdk.Fime*{
  *;
}

-keepnames class org.h2.api.* {
  *;
}

-keep public class org.h2.**

-keepclasseswithmembernames class * {
  native <methods>;
}
