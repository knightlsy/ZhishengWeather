![枳生天气 · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <img src="assets/app-icon.png" width="96" alt="枳生天气应用图标"/><br/>
  <b>打开就是天气。</b><br/>
  一款信息密度较高的 Android 天气应用，采用磷光终端风格。没有广告、账号和统计 SDK。
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/latest">
    <img alt="下载公共版 APK" src="https://img.shields.io/badge/下载公共版_APK_·_v0.0.5-FF6F1E?style=for-the-badge&labelColor=10151C"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml"><img alt="Build" src="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml/badge.svg?branch=main&style=flat-square"/></a>
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-31C9DB?style=flat-square"/>
  <img alt="当前版本 0.0.5" src="https://img.shields.io/badge/当前版本-0.0.5-31C9DB?style=flat-square"/>
  <img alt="无广告、账号和埋点" src="https://img.shields.io/badge/广告·账号·埋点-无-31C9DB?style=flat-square"/>
  <img alt="MIT License" src="https://img.shields.io/badge/许可-MIT-31C9DB?style=flat-square"/>
</p>

<p align="center">
  <b>简体中文</b> · <a href="README.en.md">English</a>
</p>

---

## 界面

<table>
  <tr>
    <td align="center"><a href="assets/screenshot-home.jpg"><img src="assets/screenshot-home.jpg" width="160" alt="枳生天气主页"/></a></td>
    <td align="center"><a href="assets/screenshot-details.jpg"><img src="assets/screenshot-details.jpg" width="160" alt="遥测数据与空气质量"/></a></td>
    <td align="center"><a href="assets/screenshot-cities.jpg"><img src="assets/screenshot-cities.jpg" width="160" alt="城市列表"/></a></td>
    <td align="center"><a href="assets/screenshot-add-city.jpg"><img src="assets/screenshot-add-city.jpg" width="160" alt="城市搜索"/></a></td>
    <td align="center"><a href="assets/screenshot-settings.jpg"><img src="assets/screenshot-settings.jpg" width="160" alt="设置"/></a></td>
  </tr>
  <tr>
    <td align="center"><sub>主页</sub></td>
    <td align="center"><sub>遥测与空气</sub></td>
    <td align="center"><sub>城市</sub></td>
    <td align="center"><sub>搜索</sub></td>
    <td align="center"><sub>设置</sub></td>
  </tr>
</table>

<p align="center"><sub>点击截图查看原图。</sub></p>

## 这个项目

我平时打开天气 App，只想尽快确认几件事：现在多少度，什么时候下雨，空气怎么样，明后天会不会突然降温。枳生天气把这些内容放在同一条纵向信息流里，不需要先看开屏广告，也不用登录账号。

界面走的是磷光终端风：黑色背景、细线分区、青色主信号和少量橙色提示。信息排得比较紧，但主次是固定的。实况、预警、逐时和短时降水在前，空气质量、生活指数、月相等内容继续往下滑就能看到。

## 能看什么

- 当前位置或已保存城市的实况天气、体感温度、风向风速和气压
- 24 小时逐时预报、15 天高低温趋势，以及未来两小时降水
- 气象预警、空气质量六项分测和常用生活指数
- 日出日落、月相、月出月落和昨日天气对比
- 多城市收藏，2x2 / 4x2 / 4x4 三种桌面小组件
- 长按应用图标可刷新天气、搜索城市或打开设置

雨、雪、雾和雷暴各有一套背景氛围效果，强度可调，也可以完全关闭。温度、风速和气压单位分别设置；不用的页面模块可以单独隐藏。

数据源没有提供的字段会留空，不用估算值补齐。

## 公共版和满血版

GitHub Release 提供公共版 APK，安装后就能使用。满血版需要自己从源码构建，并填写个人的和风天气开发者凭据。

| | 公共版 | 满血版 |
|:--|:--|:--|
| 获取方式 | [Release](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) 下载 | 拉取源码自行构建 |
| 默认数据链路 | 小米天气 + Open-Meteo | 和风天气为主，小米和 Open-Meteo 补充 |
| 额外配置 | 不需要 | 和风 Ed25519 凭据 |
| 主要差别 | 常用天气功能可直接使用 | 可使用和风提供的预警、逐分钟降水和生活指数，具体范围取决于账号权限 |

普通使用直接下载公共版即可。满血版不能预先打包，因为开发者凭据不应该放进公开 APK。

## 数据源怎么工作

项目接入了和风天气、小米天气和 Open-Meteo。设置页可以选择自动优选，也可以固定使用某个数据源，并会显示当前城市实际返回数据的来源。

| 数据源 | 配置 | 主要内容 |
|:--|:--|:--|
| 和风天气 | 需要个人开发者凭据 | 实况、预警、逐时逐日、分钟降水、空气质量、生活指数 |
| 小米天气 | 不需要 | 国内天气、城市搜索、昨日天气和台风辅助数据 |
| Open-Meteo | 不需要 | 全球实况、逐时逐日、空气质量、15 分钟降水和缺项补充 |

自动模式会按可用性依次尝试和风（满血版）、小米和 Open-Meteo。主源逐时或逐日数据不足时，会用 Open-Meteo 补足。

月相在本机按城市日期计算。数据源没有月出、月落时，应用会根据日期和城市坐标补算，不再发起额外请求。

和风接口使用 Ed25519 签名 JWT。`-PpublicBuild` 会在构建阶段清空和风配置，公开 APK 和仓库都不包含开发者凭据。

## 图标

<p align="center">
  <img src="assets/app-icon.png" width="144" alt="枳生天气应用图标"/>
</p>

启动图标使用太阳、云和降水三个直接的天气元素。深色底板和青橙配色与应用界面保持一致。

<p align="center"><img src="assets/icons_grid.png" width="560" alt="枳生天气图标组"/></p>

应用内还有 15 枚天气图标，覆盖晴、多云、阴、雾、雨、雷暴、雪、风和霰等状态。它们单独绘制，没有从通用图标库拼接。

## 安装

1. 从 [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) 下载最新公共版 APK。
2. 安装到 Android 8.0 或更高版本的设备。
3. 首次打开默认显示北京，可通过搜索保存自己的城市。

APK 只在 GitHub 发布。Android 可能提示允许当前应用安装未知来源文件，这是安装渠道提示，不是枳生天气申请了额外系统权限。

## 0.0.5 更新

0.0.5 的重头戏是主题。深色之外新增了浅色「清冷翡翠」——冷灰纸面打底、翡冷翠做数据色，桌面小组件跟着一起换；顺手修了两个藏得比较深的毛病。

- 主题三档：深色 / 浅色 / 跟随系统，切换即时生效，小组件同步换肤
- 修复「跟随系统」方向反了的问题：系统深色时 App 反而切浅色
- 修复和风天气源预警不按国标四档着色的老问题（此前和风的预警清一色红）
- 设置页新增 GitHub 仓库入口，欢迎顺手点个 star
- 文字、图标、逐日温度条等浅色细节整体盘过一遍

当前回归检查包括 46 项单元测试、Android Lint、Debug / 公共版 / 满血版构建。详细记录见 [RELEASE_0.0.5.md](RELEASE_0.0.5.md)。

## 从源码构建

需要 JDK 17 和 Android SDK 34。仓库包含 Gradle Wrapper。

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
```

不填写凭据时，构建结果使用公共版数据链路。需要和风主源时，在根目录 `local.properties` 中写入 SDK 路径和个人凭据；该文件已被 Git 忽略。

```properties
sdk.dir=<Android SDK 路径>
qw.host=<API Host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行>
```

```bash
./gradlew assembleDebug                     # Windows：.\gradlew.bat assembleDebug
./gradlew assembleRelease                   # 满血版，需配置自己的签名文件
./gradlew assembleRelease -PpublicBuild     # 公共版，清空凭据并使用随库公开证书
```

随库的 `keystore/public.jks` 只用于保持公共版之间可以覆盖安装，不是私有签名身份。

主要技术栈：Kotlin 2.0.21、Jetpack Compose、Material 3、ViewModel / StateFlow、Retrofit、OkHttp、kotlinx-serialization、DataStore 和 BouncyCastle。`minSdk 26`，`targetSdk 34`。代码结构与提交约定见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 权限和数据

应用声明三个权限：

| 权限 | 用途 |
|:--|:--|
| 网络访问 | 请求天气和城市搜索数据 |
| 网络状态 | 判断设备是否联网 |
| 粗略位置 | 可选；仅在开启定位并主动重新定位时申请 |

应用没有广告 SDK、统计埋点、账号系统或自建后端。城市列表和设置保存在本机。天气请求会把所选城市坐标发送给当前数据源；使用定位时，坐标还用于反查城市名。

定位开启并授权后，应用回到前台会按间隔复核所在城市，不在后台持续获取位置。相关代码位于 [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data)。

## 已知限制

- 公共版不含和风凭据，官方预警和生活指数可能缺失
- Open-Meteo 的短时降水为 15 分钟粒度，不是逐分钟雷达临近预报
- 台风和昨日天气依赖辅助数据源，接口无返回时对应区域为空
- 跨数据源预警按标题去重；标题不同的同一条预警可能重复出现
- 项目仍处于早期版本，涉及防灾决策时请以当地气象部门信息为准

## 更新记录

<details open>
<summary><b>0.0.4 // WIDGET OVERHAUL</b></summary>

- 重做三档桌面小组件的圆角、字号、图标和信息层级
- 合入 0.0.3test 稳定性补丁；41 项单元测试通过，Lint 0 Error
- 离线缓存兜底、小组件后台刷新、数据源熔断、预警分级着色

</details>

<details>
<summary><b>0.0.3 // STABILITY PASS</b></summary>

- 修正月相、月出月落、小组件、快捷操作、定位换城和数据源状态
- 调整“明显”天气氛围档，保持“克制”档不变
- 统一应用内终端名称，重做启动图标
- 15 项单元测试通过，Lint 0 Error，公共版与满血版均可构建

</details>

<details>
<summary><b>0.0.2 // FEED SELECT</b></summary>

- 增加数据源选择、三种桌面小组件、天气氛围层和可选定位
- Open-Meteo 成为可独立使用的数据源
- 修复夜间图标、逐时曲线、返回与转屏等问题

</details>

**0.0.1 Preview**：首次公开预览，包含磷光终端界面、15 枚天气图标、三源数据链路和公共版构建方式。

完整版本记录见 [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases)。

## 许可

- 代码使用 [MIT License](LICENSE)。欢迎提交 [Issue](https://github.com/ZhishengZZ/ZhishengWeather/issues) 和 PR。
- 界面、启动图标、天气图标和终端文案为枳生天气项目素材，引用时请保留来源。
- 天气数据版权归 [和风天气](https://www.qweather.com/)、[Open-Meteo](https://open-meteo.com/) 和小米天气，数据仅供参考。
- 使用和风主源时请保管好个人凭据，不要提交到公开仓库。

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · Kotlin / Android</sub></p>
