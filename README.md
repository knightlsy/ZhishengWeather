![枳生天气 · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>把天气摆在第一屏。</b><br/>
  一个磷光终端风的 Android 天气 App：没有广告，不用登录，打开就能看。<br/>
  <sub>MELCHIOR-1 · BALTHASAR-2 · CASPER-3</sub>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/latest">
    <img alt="下载公共版 APK" src="https://img.shields.io/badge/下载公共版_APK_·_v0.0.2_·_12_MB-FF6F1E?style=for-the-badge&labelColor=10151C"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml"><img alt="Build" src="https://github.com/ZhishengZZ/ZhishengWeather/actions/workflows/build.yml/badge.svg?branch=main&style=flat-square"/></a>
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-31C9DB?style=flat-square"/>
  <img alt="广告、账号、埋点均为零" src="https://img.shields.io/badge/广告·账号·埋点-零-31C9DB?style=flat-square"/>
  <img alt="MIT License" src="https://img.shields.io/badge/许可-MIT-31C9DB?style=flat-square"/>
</p>

<p align="center">
  <b>简体中文</b> · <a href="README.en.md">English</a>
</p>

---

## 先看界面

<table>
  <tr>
    <td align="center"><a href="assets/screenshot-home.jpg"><img src="assets/screenshot-home.jpg" width="160" alt="枳生天气主页：实况、预警、逐时、分钟降水"/></a></td>
    <td align="center"><a href="assets/screenshot-details.jpg"><img src="assets/screenshot-details.jpg" width="160" alt="遥测数据与空气质量"/></a></td>
    <td align="center"><a href="assets/screenshot-cities.jpg"><img src="assets/screenshot-cities.jpg" width="160" alt="城市列表"/></a></td>
    <td align="center"><a href="assets/screenshot-add-city.jpg"><img src="assets/screenshot-add-city.jpg" width="160" alt="城市搜索"/></a></td>
    <td align="center"><a href="assets/screenshot-settings.jpg"><img src="assets/screenshot-settings.jpg" width="160" alt="设置：数据源、定位、单位"/></a></td>
  </tr>
  <tr>
    <td align="center"><sub>主页</sub></td>
    <td align="center"><sub>遥测与空气</sub></td>
    <td align="center"><sub>城市</sub></td>
    <td align="center"><sub>搜索</sub></td>
    <td align="center"><sub>设置</sub></td>
  </tr>
</table>

## 为什么做它

每天出门前，我想知道的其实很具体：冷不冷，会不会下雨，空气怎么样。

但现在的天气 App 想让我先看五秒开屏广告，再弹一个会员窗，天气本身藏在第三屏。我不想为了看一眼天气注册账号，所以自己写了一个。打开枳生天气，实况、预警、逐时变化和未来几天的趋势都在第一屏；湿度、气压、空气质量和生活指数往下滑一层就到。

界面也是照自己的喜好做的：黑色底、细线、单色磷光，信息密度拉满，没有一张大图占掉半个屏幕。它不是所有人都会喜欢的样子，但喜欢终端界面的人，大概一眼就懂。

## 出门前能看什么

- **带不带伞** — 未来两小时的降水直接画成柱状图，雨什么时候来、下多久，一眼看完。和风源精确到分钟，Open-Meteo 公共源是 15 分钟一档。
- **今天穿多少** — 温度、体感、风和穿衣指数放在一起，不用凭一个数字猜。
- **哪天降温** — 24 小时温度曲线加 15 天高低温趋势，都留在主界面。
- **能不能开窗** — AQI 之外能展开 PM2.5 / PM10 / O₃ / NO₂ / SO₂ / CO 六项分测。
- **有没有危险天气** — 气象预警按等级着色，正文完整给出；数据源没给的字段就留空，不拿估算凑数。

再往下还有：多城市收藏、日出日落和月相、昨日天气对比、台风辅助数据，以及 2x2 / 4x2 / 4x4 三档桌面小组件。下雨飘数据雨、下雪飘点、雾天呼吸噪点、雷暴扫过扫描线——四组氛围效果都画在内容底下，不挡字，三档强度可调，嫌花可以全关。温度、风速、气压单位分别可选，用不到的页面模块也能单独关掉。

## 公共版和满血版

Release 里挂的是公共版。它不是试用包：实况、24 小时预报、15 天趋势、空气质量、短时降水、城市搜索、桌面小组件，全都直接能用。

| | 公共版 | 满血版 |
|:--|:--|:--|
| 怎么获得 | [Release](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) 直接下载 | 拉源码自己构建 |
| 数据链路 | Open-Meteo + 小米天气 | 和风主源，小米、Open-Meteo 补充 |
| 要不要配置 | 不用 | 需要自己的和风 Ed25519 凭据 |
| 区别 | 开箱即用，和风独占的项目留空 | 多官方预警、逐分钟降水和生活指数，取决于账号接口权限 |

只想装上用，下公共版就够。满血版没法预先打包——开发者凭据不能跟着 APK 一起公开。

## 数据从哪来

天气 App 最怕数据源挂了整屏空白，所以同时接了三家，谁掉线都还有画面：

| 数据源 | 要不要配置 | 主要负责 |
|:--|:--|:--|
| 和风天气 | 自备开发者凭据 | 实况、预警、逐时逐日、分钟降水、空气质量、生活指数 |
| 小米天气 | 不用 | 国内天气、城市搜索、昨日天气、台风等辅助数据 |
| Open-Meteo | 不用 | 全球实况、逐时逐日、空气质量、15 分钟降水和缺项补位 |

设置里可以在自动优选、和风、小米、Open-Meteo 之间手动切换。自动模式按可用性降级：主源逐时不足两条时，会按城市本地时区重建时间轴、用 Open-Meteo 补满 24 小时；逐日不满 15 天同样补尾，海外城市也凑得齐。各家能给的项目不完全一样，给不了的区域就老实留白，不伪造一个看似完整的结果。

和风接口用 Ed25519 签名 JWT 认证，公开构建会强制清空凭据——开发者密钥进不了 APK，仓库里也不含任何凭据。

## 图标也是自己做的

<p align="center"><img src="assets/icons_grid.png" width="560" alt="磷光终端风天气图标组"/></p>

这组图标不是从图标库里拼的：15 枚天气符号为这个项目专门定制，纯黑底、单色青，昼夜各一套，跟界面共用同一套轮廓和明暗关系。入库前统一过了一遍本地图像处理：

```text
1024² 原图 ─▶ 亮度转 Alpha 键控 ─▶ 边缘平滑 ─▶ 512 px 归一 ─▶ 入库
```

晴、多云、阴、雾、小雨、大雨、雷暴、雪、风、霰，都齐了。

## 安装

1. 下载 [`zhisheng-weather-v0.0.2.apk`](https://github.com/ZhishengZZ/ZhishengWeather/releases/download/v0.0.2/zhisheng-weather-v0.0.2.apk)（约 12 MB）。
2. 装到 Android 8.0 或更高版本的机器上。
3. 第一次打开默认是北京，搜索并保存自己的城市就行。

APK 只走 GitHub 发布，所以系统会提示"允许安装未知来源应用"——这是安装渠道的提示，不是 App 多申请了权限。介意的话可以照上面的步骤自己编一个，产物一样。

## 从源码构建

需要 JDK 17 和 Android SDK 34，Gradle Wrapper 仓库自带。

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
```

不填任何凭据直接构建，跑的就是公共版数据链路。想接和风主源，把 SDK 路径和自己的凭据写进根目录的 `local.properties`（已被 Git 忽略）：

```properties
sdk.dir=<Android SDK 路径>
qw.host=<API Host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行>
```

然后：

```bash
./gradlew assembleDebug                     # Windows 用 .\gradlew.bat assembleDebug
./gradlew assembleRelease                   # 满血发布包，需自备 keystore/zhisheng.jks
./gradlew assembleRelease -PpublicBuild     # 公开版：强制清空凭据，用随库公开证书签名
```

随库公开证书只用来保证公开构建之间能覆盖升级，不代表任何私密或可信身份。

技术栈：Kotlin 2.0.21 + Jetpack Compose + Material 3，MVVM（ViewModel / StateFlow），Retrofit + OkHttp + kotlinx-serialization，DataStore 存储，BouncyCastle 做 Ed25519。`minSdk 26`，`targetSdk 34`。代码结构和提交约定见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 权限和数据

整个 App 只声明三个权限：

| 权限 | 用途 |
|:--|:--|
| 网络访问 | 请求天气和城市搜索数据 |
| 网络状态 | 判断当前是否联网 |
| 粗略位置 | 可选；打开定位开关并点"定位当前城市"时才申请一次 |

没有广告 SDK，没有统计埋点，没有账号系统，也没有自建后端。城市列表和设置只存在本机。天气请求会把所选城市的坐标发给当前数据源；用定位时，坐标还会用于反查城市名。相关代码在 [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data)，可以直接查。

## 还不完美的地方

先摊在这，免得下载了才发现：

- 公共版不带和风凭据，官方预警和生活指数在公共版里可能缺位
- Open-Meteo 的短时降水是 15 分钟粒度，不是逐分钟的雷达临近预报
- 台风和昨日天气靠辅助源，接口不返回时对应区域留空
- 跨源预警按标题精确去重，两个源措辞不同时，同一条预警可能出现两遍
- 项目还在早期；涉及防灾决策，请以当地气象部门发布为准

## 更新记录

<details>
<summary><b>0.0.2 // FEED SELECT</b> — 数据源可选 · 桌面小组件 · 氛围层 · 可选定位</summary>

- 数据源四选一：自动优选 / 和风 / 小米 / Open-Meteo；Open-Meteo 升为独立主源，免密钥跑完整链路
- 2x2 / 4x2 / 4x4 三档桌面小组件，跟 App 同一套终端皮
- 雨、雪、雾、雷暴四组氛围效果，三档强度，可整体关闭
- 定位改为按需启用，默认关闭，只申请粗略位置，不引入 Google Play 服务
- 设置页重做：数据源 / 定位 / 单位 / 显示模块 / 界面效果 / 关于
- 修了一批观感问题：夜里显示太阳图标、月相恒为残月（朔望月序号基准差了 30 年）、逐日高低温倒挂、逐时曲线断成半段弧、返回键直接退出、转屏丢失当前页、预警卡片展开错位

**0.0.1 Preview // FIRST DROP** — 首个公开预览：磷光终端 UI、15 枚自制图标、三源融合、`-PpublicBuild` 公开版构建链路。

完整记录见 [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases)。

</details>

## 许可

- 代码以 [MIT](LICENSE) 开源，欢迎提 [Issue](https://github.com/ZhishengZZ/ZhishengWeather/issues) 和 PR（[贡献指南](CONTRIBUTING.md) · [行为准则](CODE_OF_CONDUCT.md) · [安全说明](SECURITY.md)）。
- 界面美学致敬 EVA / NERV 终端风格；个人作品，不作商业用途。
- 天气数据版权归 [和风天气](https://www.qweather.com/) · [Open-Meteo](https://open-meteo.com/) · 小米天气，数据仅供参考。
- 用和风主源请自备开发者凭据，也别把自己的 Key 提交到公开仓库。

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · 用磷光和 Kotlin 写的</sub></p>
