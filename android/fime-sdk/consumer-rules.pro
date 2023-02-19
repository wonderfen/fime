-keepnames class top.someapp.fimesdk.* {
  public *;
}

-keepnames class top.someapp.fimesdk.Fime*{
  *;
}

-keepclasseswithmembernames class * {
  native <methods>;
}