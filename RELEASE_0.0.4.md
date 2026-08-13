# 枳生天气 0.0.4 发布核对记录

核对日期：2026-08-12  
版本：`0.0.4`（`versionCode 20260815`）  
工作副本：`D:\codex3\ZhishengWeather-0.0.4`  
基线：`D:\ZhishengWeatherProject\ZhishengWeather-0.0.3 - test`

## 结论

0.0.4 的代码、30 项单元测试和 Lint 均已通过。根据内测决定，卫星云图功能已整体移除，主页不再保留入口或隐藏跳转。

满血版已覆盖安装到测试手机 `2405CPX3DC`，设备内版本确认为 `0.0.4 (20260815)`；城市、设置与已放置小组件均保留。

## 本版改动

### 1. 桌面小组件

- 2x2、4x2、4x4 三档统一为 22dp 圆角气象终端：斜向深色渐变、橙色状态轨、青色实时点和渐变分区线
- 当前温度字号为 44–48sp，主天气图标为 44–60dp
- 补充日期、体感温度、湿度、风、降水概率、数据来源和更新时间；2x2 主动舍弃更新时间以完整显示体感与湿度
- 4x2 保留未来四小时，4x4 增加未来四小时、三日天气与 AQI
- 三档均提供独立静态 `previewImage`，兼容忽略 `previewLayout` 的小米桌面组件选择器
- 未同步时隐藏逐时/逐日占位图标，不再出现没有文字的假数据
- XML 回归测试会阻止主温度低于 44sp、主图标低于 44dp、圆角低于 16dp，并检查预览图、日期和详情字段

## 自动化核对

| 项目 | 结果 |
|:--|:--|
| 单元测试 | 30 / 30 通过，0 failure，0 error，0 skipped |
| Android Lint | 0 Error，57 Warning；`SmallSp` 已清零 |
| Debug 构建 | 通过 |
| 公共 Release：`assembleRelease -PpublicBuild` | 通过 |
| 满血 Release：`assembleRelease` | 通过 |
| APK zipalign | 三包均通过 |
| APK 签名校验 | 三包均通过 APK Signature Scheme v2 校验 |
| 源码与公共版凭据扫描 | 本机 `qw.host`、project id、kid、private key 均未出现；无私钥头标记 |

Lint 的 57 个 Warning 为依赖版本、旧 targetSdk 提示、部分 XML 属性兼容性、嵌套权重和未使用资源等建议项；没有阻断错误，也没有本轮小组件的小字号告警。

## 最终 APK

| 构建 | 文件 | 字节数 | SHA-256 |
|:--|:--|--:|:--|
| 公共版 | `dist/zhisheng-weather-v0.0.4-public.apk` | 12,568,016 | `316A788BE99930A62F41B3097ADA2CFB3C46546B50CD60320DFD433B1C5B1D2F` |
| 满血版 | `dist/zhisheng-weather-v0.0.4-full.apk` | 12,568,016 | `894C32D3898B49C9C4947B9E7C8798582E45B3C43F87EAD2FE588865C926E738` |
| Debug | `dist/zhisheng-weather-v0.0.4-debug.apk` | 16,460,512 | `C33625706504BC2854F6C502AFF5E52F863A0ACAEB28957C2D0BA35F5AFAD6E1` |

签名兼容性：

- 公共版证书 SHA-256：`7fb23b07cbe6b89b38f35c3e3e1ef73cb7bb36b4ec98defb1b5b3cedfde7f54f`，与本机 0.0.2 公共版一致
- 满血版证书 SHA-256：`b6cac6a6d6fcdbea946e67e80b1517a975e30fb221292410e2c577ad36da65f4`，与 0.0.3 test Release 一致

满血 APK 会内嵌本机构建时配置的和风凭据，只适合个人使用，不应上传到公开 Release。公开发布只使用 `-PpublicBuild` 生成的公共版。

## 真机验收

1. 2x2 与 4x2 已在小米桌面实拍复核：圆角、渐变、主字号和图标正常，体感/湿度均完整显示，无“…”；4x4 已按更紧凑的垂直间距修正底部裁切风险。
2. 三张选择器静态预览已打入 APK 并由 Provider XML 引用，资源表可解析。
3. 主页顶栏实拍复核仅保留刷新和设置，无云图入口。
4. 覆盖安装成功，版本升到 `20260815`，未清除应用数据。
