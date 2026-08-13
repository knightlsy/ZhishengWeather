# 枳生天气 0.0.5.1 发布核对记录

核对日期：2026-08-13
版本：`0.0.5.1`（`versionCode 20260821`，可覆盖安装 0.0.5 的 `20260820`）
工作副本：`D:\ZhishengWeatherProject\ZhishengWeather-0.0.5`（git 分支 `agent/v0.0.5.1`，从 main 出发 cherry-pick 修复）
基线：v0.0.5（已发布）+ 用户反馈驱动的小组件修复

## 结论

0.0.5.1 小组件修复完成：46 项单元测试、Android Lint、Debug / 公共版 / 满血版构建均通过；公共版凭据扫描 0 命中；真机覆盖安装成功，小组件主题只跟系统策略生效。

## 本版改动（用户反馈驱动）

### 一、小组件字号放大

三套布局里全部 11sp 小字放大一档，消除「字体过小」反馈：

| 控件 | 原 | 新 |
|:--|:--|:--|
| 日期 w_date | 11sp（小/中）/ 12sp（大） | 13sp / 14sp |
| 更新时间 w_upd | 11sp | 12sp |
| 高低温 w_range | 13sp（小/中）/ 14sp（大） | 14sp / 15sp |
| 体感湿度 w_details | 11sp（小/中）/ 12sp（大） | 12sp / 13sp |
| AQI 行 w_aqi | 11sp | 12sp |
| 小时时间标签 | 11sp | 12sp |
| 小时温度 | 14sp | 15sp |
| 逐日行 | 13sp | 14sp |

### 二、小组件选择器预览图重绘（上海）

`scripts/generate_widget_previews.ps1` 重写：

- 与真实布局 1:1 对齐：字号 = 布局 sp × 2（画布按 2x 密度）、颜色取自 widget_colors.xml / widget_bg.xml 同值
- 示范城市默认**上海**（可传参），不再用开发机城市金川区
- 日期取生成当天（zh-CN 格式），不再写死
- 修左角弧线裁切城市名的绘制问题；三张 PNG（360 / 720×360 / 720×720）重新生成并通过视觉检查

### 三、小组件主题策略：只跟系统

- `ZhishengWidgetProvider` 不再读 App 的 ThemeMode，只看系统夜间态（`uiMode & UI_MODE_NIGHT_MASK`）
- App 内切换浅色不再改变桌面小组件（此前会跟着换肤，与选择器深色预览不一致，用户反馈后调整）
- 设置页切主题不再触发小组件重渲

## 自动化核对

| 项目 | 结果 |
|:--|:--|
| 单元测试 | 46 / 46 通过，0 failure |
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
| 公共版 | `dist/zhisheng-weather-v0.0.5.1-public.apk` | 12,883,804 | `8DF9F35E1D71C367B5F6A006949ED80CE678CFEA80C3F8B24B01454BE4194032` |
| 满血版 | `dist/zhisheng-weather-v0.0.5.1-full.apk` | 12,883,804 | `954DF7362D8E78424ABBE7970BD6F8E571A03103D11459E2474E63C0FD32EFD9` |
| Debug | `dist/zhisheng-weather-v0.0.5.1-debug.apk` | 16,847,388 | `F8E70BA182EF426AD0382A427FE21CCD7032DC5D2919148B3128ADFE660CAB28` |

签名兼容性：

- 公共版证书 SHA-256：`7fb23b07cbe6b89b38f35c3e3e1ef73cb7bb36b4ec98defb1b5b3cedfde7f54f`，与 0.0.2 / 0.0.4 / 0.0.5 公共版一致（覆盖安装链不断）
- 满血版证书 SHA-256：`b6cac6a6d6fcdbea946e67e80b1517a975e30fb221292410e2c577ad36da65f4`，与 0.0.3 test 起一致

满血 APK 内嵌本机构建时配置的和风凭据，只适合个人使用，不应上传到公开 Release。公开发布只使用 `-PpublicBuild` 生成的公共版。

## 真机验收

设备：小米 2405CPX3DC（Android 16，serial 949f2d6e）

1. 覆盖安装成功：`20260820` → `20260821`。
2. 主题联动验证：App 切浅色后桌面小组件保持深色（只跟系统）；系统深色下小组件深色。
3. 选择器预览图：重绘后三档预览均为上海示范数据，字号与真实布局一致（HyperOS 缓存旧预览图时重启桌面生效）。

## 发布流程记录

- 分支：`agent/v0.0.5.1`（从 main `5527f42` 出发，cherry-pick 99eed35 + 版本号/README/发布记录）
- PR：#10「v0.0.5.1: 小组件字号放大 + 预览图重绘 + 主题只跟系统」
- 营销落地页：`D:\claudecodecli\output\zhisheng-weather-landing\index.html` 下载链接与文案更新至 v0.0.5.1

## 遗留（记录在案，下版参考）

与 0.0.5 相同，另加：

- 小组件天气图标 PNG 为深底设计，系统浅色时小组件换肤但图标仍为原色（App 内走 SrcIn 染色）
