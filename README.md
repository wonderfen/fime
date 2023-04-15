# Fime

Fime is a free and customizable input method app on Android.

# Fime 输入法

Fime 是一个在 Android 平台上，支持自定义输入方案、键盘和词库的输入法。

## 关于本项目

目前这是一个个人开发的项目，主要目标为：

- 方便在移动触屏设备(Android)上，单手输入
- 支持自定义键盘(全键盘、九宫格、合并键和动态按键标签)
- 支持自定义输入方案(汉语拼音、双拼和码表类方案)
- 使用自定义词库(典)

在“自定义”方面，主要参考了已有的输入法平台Rime，实现了Rime中的部分功能。由于个人水平有限，没有使用Rime的代码。是一个简单的、从零开始实现的输入法软件。

## 下载和安装

- 最新版 
  - [Github Releases](https://github.com/wonderfen/fime/releases/latest)
  - [QQ群](https://jq.qq.com/?_wv=1027&k=wntFOKGk)
- 最新代码
  - [gitee 仓库(国内推荐)](https://gitee.com/zelde/fime)
  - [github 仓库](https://github.com/wonderfen/fime)

## 致谢

- [在线网站](https://fime.fit)：Noor Ty
- [QQ群](https://jq.qq.com/?_wv=1027&k=wntFOKGk)中提供建议的朋友们

## 从源代码构建

### 开发环境

- Android Studio(建议 2020.3 +)
- git
- Flutter 3.3.10

### 克隆本项目 

```shell
git clone https://gitee.com/zelde/fime.git
# 或
# git clone https://gihub.com/wonderfen/fime.git
```

### 使用 Android Studio 导入项目

1. 安装 flutter 的依赖

   ```shell
   flutter pub get
   ```

2. 准备签名相关的文件

   - 生成一个 keystore 文件

   - 在 android 目录中，增加一个 key.properties 文件，文件内容如下：

     ```properties
     storePassword=your store password
     keyPassword=your key password
     keyAlias=your key alias
     storeFile=/path/to/your/keystore/file
     ```

### 打包和安装

1. 打包

   ```shell
   flutter build apk --release
   # 或
   cd android
   ./gradlew assembleRelease # linux or windows PowerShell
   # gradlew assembleRelease # windows cmd
   ```

2. 在模拟器或真机上安装(也可以使用 IDE 中的 Run 菜单运行)

   ```shell
   cd build/app/outputs/apk/release
   adb install fime-VERSION+COUNT.apk	# VERSION为版本号，COUNT为 git 提交的历史计数值
   ```

## 其他说明

- Flutter：[flutter.cn](https://flutter.cn)

- Fime 在线文档: [fime.fit](https://fime.fit)

- Github 仓库地址: [wonderfen/fime](https://github.com/wonderfen/fime)

- Gitee 仓库地址: [zelde/fime](https://gitee.com/zelde/fime)

- 开源许可协议：MIT

  Github 和 Gitee 仓库中的代码是自动同步的，同步的方向是 Gitee => Github，也就是说 Gitee 中的代码总是最新的。

