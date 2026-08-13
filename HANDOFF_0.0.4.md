# 枳生天气 · 0.0.4 完结交接（下一个 session 先读这里）

> 建立：2026-08-13 · 状态：**0.0.4 已发布上线**，0.0.5 方向讨论中
> 本文件放工作副本根目录，新 session 接手项目时第一步读它。

## 一、当前状态速览

- 0.0.4 已发布：GitHub Release `v0.0.4 // WIDGET OVERHAUL`（Latest），附件 `zhisheng-weather-v0.0.4.apk`（公共版 12.9MB），tag v0.0.4。
- 线上 main：每版一份发布记录（`STABILITY_0.0.3.md` + `RELEASE_0.0.4_FINAL.md`）；只剩 main 一个分支。
- 四个 Release 已统一形式：**「v0.0.x // 英文代号」标题 + 中英双语 notes**（简介→公开版APK说明→本次更新→检查结果→满血版段）。
- 本工作副本（`ZhishengWeather-0.0.4`）**不是 git 仓库**，改代码在这里，发版走「临时克隆+新分支+PR」。
- 真机（小米 2405CPX3DC）已装 0.0.4 满血版（versionCode 20260818）。
- 安全核查已完成：线上历史无私钥/凭据；仅 kid 值作为旧版 build 脚本默认值存在于 0.0.1-0.0.3 历史（用户知情，未清历史）。

## 二、0.0.5 候选方向（2026-08-13 讨论，用户尚未拍板）

用户要「用户能感知到的更新」。已给三梯队清单：

1. **第一梯队（推荐）**：① 天气预警本地通知 ② 降雨提醒通知（「X 分钟后开始下雨」）③ Hero 下的一句话天气摘要——全部基于现有数据与 WorkManager 链路，零新依赖
2. **第二梯队**：④ 「35 分钟后开始下雨」提示 ⑤ 逐日详情展开 ⑥ 防 MIUI 强制深色反转
3. **第三梯队**：⑦ 配色主题 ⑧ 搜索历史 ⑨ 小组件分区点击

用户未选。若用户说「接着做 0.0.5」未指明方向，默认推荐 1+2+3。

## 三、工作方式约定（用户明确要求过）

- **核查/看一看类任务：先汇报发现，用户说改再改**——尤其线上门面（Release/tag/分支/历史）。
- 发布纪律：公共版必须 `-PpublicBuild` 构建 + 上传前用真实凭据值 grep 扫描 + zipalign/签名校验；满血版绝不上传。
- 小米源相关问题用户明确说过「先不管，觉得小米没啥大问题」（生活指数口径 B8 因此保持原样）。

## 四、关键路径与命令速查

| 项 | 路径/命令 |
|---|---|
| 工作副本 | `D:\ZhishengWeatherProject\ZhishengWeather-0.0.4` |
| 构建 | `build.ps1`（JAVA_HOME 固定 `D:\android-build-tools\jdk-17.0.13+11`，--no-daemon，日志 build.log） |
| 测试+lint | `build.ps1 -Task 'testDebugUnitTest lintDebug assembleDebug'`（当前 41 项全绿） |
| 真机 | adb `D:\金川党建数据大屏项目\tools\android-sdk\platform-tools\adb.exe`；screencap 用 `//sdcard/` 双斜杠；INJECT_EVENTS 被拦只能人工滑 |
| 发布记录 | `RELEASE_0.0.4_FINAL.md`（最终版） |
| 落地页 | `D:\claudecodecli\output\zhisheng-weather-landing\`（已指 v0.0.4）；`ZhishengWeatherProject\zhisheng-weather-landing` 用 releases/latest 无需改 |

## 五、坑（踩过的）

- `combine` 超过 5 个 Flow 没有具名 lambda 重载，用 Array 风格（`{ arr -> arr[0] as ... }`）
- onSizeChanged 给的是 `IntSize` 不是 `geometry.Size`
- 单测碰 `android.util.Log` 需要 `unitTests.isReturnDefaultValues = true`
- README 中英两份必须同步改（版本号/更新记录/结构树）
- 构建期间不要同时改源码（gradle 会编译到中间状态）
- 临时 clone 目录被后台 shell cwd 占用时删不掉，换目录再删

## 六、遗留清单（下版可捡）

- 生活指数口径对齐（B8，需真机对比小米/和风后再动）
- Compose 三屏文案全量资源化（本版只做了小组件地基）
- OpenMeteoApi 硬编码 `Asia/Shanghai` 时区参数
- 三个 DataStore 可合并；台风 lat/lon/typhoonCode 已解析未用（路径/详情页素材）
- targetSdk 34（上 Play 需 35）；shortcuts 未配自定义 icon
- kid 值在旧历史中（用户知情；清历史需用户决定，用 git filter-repo）
