# 枳生天气 0.0.5 最终发布核对记录

核对日期：2026-08-13
版本：`0.0.5`（`versionCode 20260820`，可覆盖安装 0.0.4 的 `20260818`）
工作副本：`D:\ZhishengWeatherProject\ZhishengWeather-0.0.5`（git 分支 `agent/v0.0.5`）
基线：0.0.4 工作副本 + 清冷翡翠浅色主题 + 两个历史遗留修复

## 结论

0.0.5 开发完成：46 项单元测试、Android Lint、Debug / 公共版 / 满血版构建均通过；公共版凭据扫描 0 命中；真机（小米 2405CPX3DC）覆盖安装成功，「跟随系统」深/浅双向切换实拍验证通过，浅色主页 / 设置页实拍正常。

## 本版改动

### 一、主题系统（本版核心）

- 浅色主题「清冷翡翠」：冷灰纸面（`#F2F4F5` 底 → `#F7F9FA` 面板 → 纯白卡 + `#E3E6EA` 发丝边）、翡冷翠数据色 `#0F7F68`、钢青线框 `#16697A`、琥珀标签 `#B45309` 小面积；文字石墨蓝灰三档，全部 ≥ 4.5:1
- 浅色天气图标：SrcIn 染钢青单色线（此前染墨黑，观感太硬）
- 逐日温度条：轨道加深可见；填充改两段插值 钢青→翡冷翠→琥珀（单段青→橙中点发灰发脏）
- 预警黄收进色板 `warning` 字段（浅色油墨黄 `#8F6F0A` / 深色荧光黄 `#FFD24A`），消灭 UI 层 `if(isLight)` 硬编码
- 主题三档：深色 / 浅色 / 跟随系统（ThemeMode 存 DataStore），切换即时生效，状态栏图标同步

### 二、小组件双主题

- `ZhishengWidgetProvider` 渲染时读 ThemeMode：LIGHT 恒浅 / DARK 恒深 / SYSTEM 按系统夜间态
- 浅色换肤资源：`widget_bg_light`（冷灰渐变面板）、`widget_accent_light`、`widget_rule_light`、`widget_live_dot_light` + `widget_colors.xml` 浅色色表
- 三个布局补装饰条 id（accent bar / rule bar / live dot），RemoteViews 按 id 整体换肤，缺失 id 静默跳过
- 设置页切主题后立即 `refreshAll` 重渲桌面小组件

### 三、修复

- **「跟随系统」方向反了**：`SYSTEM` 分支把 `systemDark` 直接当 `isLight`，系统深色时反而切浅色；改为 `!systemDark`
- **manifest 去 `uiMode`**：`configChanges` 保留 `uiMode` 时系统切换主题不重建 Activity，部分 ROM 下拿不到新配置；去掉后切换即时生效（真机双向验证）
- **和风源预警不分级**：和风新版 `weatheralert/v1` 的 `severity` 是英文枚举（minor/moderate/severe/extreme），旧映射只认颜色词，全部落 UNKNOWN 统一红色；现解析官方 `color.code`（blue/yellow/orange/red）优先、severity 枚举兜底（minor→蓝 / moderate→黄 / severe→橙 / extreme→红，major→橙 / standard→蓝 就近归入）
- 新增 `AlertLevelTest` 5 用例 20 断言（小米中文 / 英文色名 / 和风枚举 / 大小写 / trim / 未知值）

### 四、其他

- 设置页 06 关于新增「GitHub 仓库」外链行（引流入口）
- README 中英同步 0.0.5；`RELEASE_0.0.4.md` 按 main 的文档整理删除（记录只保留 FINAL 版）

## 自动化核对

| 项目 | 结果 |
|:--|:--|
| 单元测试 | 46 / 46 通过（0.0.4 的 41 项 + AlertLevelTest 5 项），0 failure |
| Android Lint | 通过，无阻断错误 |
| Debug 构建 | 通过 |
| 公共 Release：`assembleRelease -PpublicBuild` | 通过 |
| 满血 Release：`assembleRelease` | 通过 |
| APK zipalign | 公共版通过 |
| APK 签名校验 | 公共/满血均通过 APK Signature Scheme v2 |
| 公共版凭据扫描 | 按 zip 条目解压扫描：qw.host / project id / kid / 私钥前缀均 0 命中（满血与 Debug 各 4 命中，仅本地） |

## 最终 APK

| 构建 | 文件 | 字节数 | SHA-256 |
|:--|:--|--:|:--|
| 公共版 | `dist/zhisheng-weather-v0.0.5-public.apk` | 12,902,636 | `E4E35C9B5FC782B17CE808AE2A9F8D11FDD8599325D200435DBBF4270D1D864B` |
| 满血版 | `dist/zhisheng-weather-v0.0.5-full.apk` | 12,902,636 | `7B8DC9DBC838AD5DC92D8721CB3EA6BBF1F8FD26F63AE85739575667EBFBE01E` |
| Debug | `dist/zhisheng-weather-v0.0.5-debug.apk` | 17,066,476 | `C00B465B825558C557A3C5EDCBD5E668027A256CDD44333965642F1BBAE18E5B` |

签名兼容性：

- 公共版证书 SHA-256：`7fb23b07cbe6b89b38f35c3e3e1ef73cb7bb36b4ec98defb1b5b3cedfde7f54f`，与 0.0.2 / 0.0.4 公共版一致（覆盖安装链不断）
- 满血版证书 SHA-256：`b6cac6a6d6fcdbea946e67e80b1517a975e30fb221292410e2c577ad36da65f4`，与 0.0.3 test / 0.0.4 满血一致

满血 APK 内嵌本机构建时配置的和风凭据，只适合个人使用，不应上传到公开 Release。公开发布只使用 `-PpublicBuild` 生成的公共版。

## 真机验收

设备：小米 2405CPX3DC（Android 16，serial 949f2d6e）

1. 覆盖安装成功：`20260818` → `20260820`，城市、设置均保留。
2. 「跟随系统」双向验证（App 停在跟随系统档）：系统深色 → App 深色；`cmd uimode night no` → App 即时变浅；恢复深色 → App 即时变深，全程无需重启。
3. 浅色主页实拍：冷灰纸面、钢青图标、翡冷翠大温度、逐日温度条三段渐变正常。
4. 浅色设置页实拍：三档主题选择器、开关翡冷翠着色、06 关于「GitHub 仓库 ↗」外链行正常。

## 发布流程记录

- 分支：`agent/v0.0.5`（从 main `33e3284` 出发，75 files changed；并入 main 后按冲突决议保留本分支代码、删除已整合的 `RELEASE_0.0.4.md`）
- PR：#9「v0.0.5: 清冷翡翠浅色主题 + 跟随系统修复」
- 营销落地页：`D:\claudecodecli\output\zhisheng-weather-landing\index.html` 下载链接与文案已更新至 v0.0.5（`D:\ZhishengWeatherProject\zhisheng-weather-landing` 用 releases/latest 通用链接，无需改动）

## 遗留（记录在案，下版参考）

- 生活指数口径（B8）：小米 carWash/sports 的 value 语义待真机对比验证后再对齐
- Compose 三屏文案全量资源化
- OpenMeteoApi 硬编码 `Asia/Shanghai` 时区参数
- 三个 DataStore 可合并为单 DataStore 多 key
- 台风 lat/lon/typhoonCode 已解析未用（路径/详情页素材）
- targetSdk 34（上 Play 需 35）、shortcuts 未配自定义 icon
- 小组件天气图标 PNG 为深底设计，浅色下未单独出浅色图标（App 内走 SrcIn 钢青染色）
- 浅色启动瞬间 windowBackground 仍为黑色（manifest 主题单值，默认深色品牌优先）
