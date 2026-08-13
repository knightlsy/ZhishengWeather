# 枳生天气 0.0.4 最终发布核对记录

核对日期：2026-08-13
版本：`0.0.4`（`versionCode 20260818`，可覆盖安装此前内测的 `20260815`）
工作副本：`D:\ZhishengWeatherProject\ZhishengWeather-0.0.4`
基线：`ZhishengWeather-0.0.3 - test`（7 项稳定性修复）+ 小组件重做 + 本轮四方向更新

## 结论

0.0.4 全部开发完成：41 项单元测试、Android Lint、Debug / 公共版 / 满血版构建均通过；公共版凭据扫描干净；真机（小米 2405CPX3DC）覆盖安装成功，主页 / 设置 / 2x2 与 4x2 小组件实拍复核正常，预警按国标黄档着色生效。

## 本版改动（相对 2026-08-12 的内测基线）

### 一、2026-08-12 已完成（原 RELEASE_0.0.4.md）

- 三档小组件重做：22dp 圆角气象终端、放大字号、静态预览图
- 卫星云图（彩云源）整体移除 + 旧凭据擦除
- 合入 0.0.3test 的 7 项稳定性修复

### 二、2026-08-13 新增（本轮）

**数据新鲜度与离线体验**

- `WeatherCache` 离线缓存：按城市持久化最近一次成功数据；断网 / 全源失败 / 超时展示缓存并标注「x 分钟前」（状态行橙色提示）
- 全局请求超时 25s（`WeatherViewModel.refresh`，TimeoutCancellationException 优先捕获）
- `SourceHealth` 数据源熔断：连续 2 次失败 down 5 分钟，仅作用于 AUTO 链；手动锁定源不受影响
- `WidgetSyncWorker` 小组件后台刷新：WorkManager 每小时拉取当前城市（约束联网），公共版自然走免 key 链路
- 小组件快照新鲜度：>3h 显示「x小时前」，>24h 显示「数据已过期」

**稳定性与埋雷清理**

- WMO 映射收敛单真源 `model/WmoMaps.kt`；修 OM `is_day == null` 恒判白天；逐时补齐按城市本地小时判昼夜
- MoonCalc 基准注释勘正（常量 12:14Z / 公式基值 14:20Z / 真实 18:14Z 三值差异说明），公式不动
- 城市数据双写备份 + 损坏自愈（主值坏读备份并回写；主备都坏重种北京）
- 定位整体 15s 上限 + accuracy >20km 拒绝；最坏 40s → ~15s
- `nearestXiaomiKey` 会话级缓存，去掉每次和风刷新附带的 searchCity 往返
- `backfillDaily` 按 dateMillis 去重；`backfillHourly` 补齐门槛改为「最后一条距今 <3h」
- `updateTime` 全源统一为本地抓取时刻（此前小米用服务器时间）
- QwAuth 签名失败 5 分钟负缓存；`enabled` 补 QW_KID 检查；401 重签失败不再重放旧 token

**新功能**

- 雨区距离（小米 `kmNum`）接入分钟降水卡
- AQI 健康建议（小米 `suggest`）接入 AQI 卡底部
- 逐时 AQI（小米独有字段）接入逐时格
- 预警等级归一：国标蓝/黄/橙/红四档着色（和风 severity + 小米 level 双映射），未识别档退回警报红

**视觉与无障碍**

- 小组件配色对齐主题：`values/widget_colors.xml` 单一色板，10 个布局 + 活点 drawable 统一引用
- 图标映射单真源 `model/ConditionIcons.kt`（when 穷尽，新增条件编译器兜底）
- TalkBack：SourceRow / SegmentRow 补 selected 语义 + selectableGroup；ToggleRow 去除与内部 Switch 重复的 role
- 小组件 Provider 兜底文案资源化（strings.xml）
- Ambience 雾效果：点阵预生成 Path 缓存，每帧 ~4000 drawCircle → 单次 drawPath
- 主题补 onError/errorContainer 派生色；移除 widget_small 恒 GONE 的 w_upd 死控件
- 清理：OpenMeteoApi 死参数、未使用的 okhttp-logging 依赖

**测试**

- 新增 11 项：MoonCalc 补 3（phaseKeyForDayStart 锚点 / enrich 补缺与保留 / 南半球与极区）、WmoMaps 3、SourceHealth 5
- `unitTests.isReturnDefaultValues = true`（SourceHealth 日志路径进入 JVM 单测）

## 自动化核对

| 项目 | 结果 |
|:--|:--|
| 单元测试 | 41 / 41 通过，0 failure，0 error，0 skipped |
| Android Lint | 0 Error，54 Warning（原 57，移除死依赖后略降）；无 SmallSp 类告警 |
| Debug 构建 | 通过 |
| 公共 Release：`assembleRelease -PpublicBuild` | 通过 |
| 满血 Release：`assembleRelease` | 通过 |
| APK zipalign | 三包均通过 |
| APK 签名校验 | 三包均通过 APK Signature Scheme v2 |
| 公共版凭据扫描 | qw.host / project id / kid / 私钥前缀在 APK 中均 0 命中 |

Lint 的 54 个 Warning 为依赖版本、旧 targetSdk 提示、部分 XML 属性兼容性、嵌套权重和未使用资源等建议项；没有阻断错误。

## 最终 APK

| 构建 | 文件 | 字节数 | SHA-256 |
|:--|:--|--:|:--|
| 公共版 | `dist/zhisheng-weather-v0.0.4-public.apk` | 12,883,816 | `CDEEDEA5D9A3566DEAB63B16F27EE829B70968252AE766B6235501FF908E4485` |
| 满血版 | `dist/zhisheng-weather-v0.0.4-full.apk` | 12,568,016 | `6E187FF485B011B76AA2141FFFDE6373B2763419F593855D6B5D7DF57DD2BB52` |
| Debug | `dist/zhisheng-weather-v0.0.4-debug.apk` | 16,890,992 | `5801E35388E129C5B1BE7DDE614C4C78A5D21F9B7AE8406751A6C71176FD5D14` |

签名兼容性：

- 公共版证书 SHA-256：`7fb23b07cbe6b89b38f35c3e3e1ef73cb7bb36b4ec98defb1b5b3cedfde7f54f`，与 0.0.2 公共版一致（覆盖安装链不断）
- 满血版证书 SHA-256：`b6cac6a6d6fcdbea946e67e80b1517a975e30fb221292410e2c577ad36da65f4`，与 0.0.3 test Release 一致

满血 APK 内嵌本机构建时配置的和风凭据，只适合个人使用，不应上传到公开 Release。公开发布只使用 `-PpublicBuild` 生成的公共版。

## 真机验收

设备：小米 2405CPX3DC（Android 16，serial 949f2d6e）

1. 覆盖安装成功：`20260815` → `20260818`，城市、设置与已放置小组件均保留。
2. 主页实拍：状态行坐标 / UPD / SRC 和风正常；**雷电黄色预警按黄档着色**（边框、斜纹、标题）；分钟降水、逐日温度条正常。
3. 设置页实拍：数据源状态机（自动优选 · 使用中 · 和风）、全部设置项与版本 0.0.4 正常。
4. 小组件实拍：4x2 显示四小时预报、来源与更新时间；2x2 无更新时间（设计如此），配色与 App 主题一致。

## 发布流程记录

- 分支：`agent/v0.0.4`（从 main `33e3284` 出发，59 files changed，+1645/−522）
- PR：#6「0.0.4: 小组件重做 + 离线缓存 + 后台刷新」，CI（test/lint/debug/public release）通过后合入 main
- 营销落地页：`D:\claudecodecli\output\zhisheng-weather-landing\index.html` 下载链接与文案已更新至 v0.0.4
  （`D:\ZhishengWeatherProject\zhisheng-weather-landing` 用 releases/latest 通用链接，无需改动）

## 遗留（记录在案，下版参考）

- 生活指数口径（B8）：小米 carWash/sports 的 value 语义待真机对比验证后再对齐，本版未改行为
- Compose 三屏文案全量资源化（本版只做小组件地基）
- OpenMeteoApi 硬编码 `Asia/Shanghai` 时区参数
- 三个 DataStore 可合并为单 DataStore 多 key
- 台风 lat/lon/typhoonCode 已解析未用（路径/详情页素材）
- targetSdk 34（上 Play 需 35）、shortcuts 未配自定义 icon
