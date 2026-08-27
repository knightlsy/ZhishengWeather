# 安全政策 // SECURITY POLICY

## 01// 受支持版本 SUPPORTED VERSIONS

| 版本 / Version | 安全支持 / Supported |
|:--|:--|
| 0.1.3 | ✅ |
| < 0.1.3 | ❌ |

## 02// 漏洞上报 REPORTING A VULNERABILITY

- **不要**通过公开 Issue 报告安全问题
  *Do **not** report security issues in public issues.*
- 首选通道：通过 GitHub 私信维护者，或开仅维护者可见的安全相关 Issue
  *Preferred channel: GitHub private message to the maintainer, or a security issue visible only to maintainers.*
- 备选通道：私信维护者
  *Fallback: message the maintainer directly.*
- 报告应包含：受影响版本、复现步骤、影响面评估、修复建议（如有）
  *Include the affected version, repro steps, an impact assessment, and a fix suggestion if you have one.*
- **报告中请勿附带真实凭据、私钥或个人数据**
  *Never attach real credentials, private keys or personal data to a report.*

## 03// 响应承诺 RESPONSE COMMITMENT

- 48 小时内确认收到
  *Acknowledgement within 48 hours.*
- 7 天内给出评估结论与修复排期
  *Assessment and a fix timeline within 7 days.*
- 修复发布后在 Release Notes 中致谢（如报告者愿意署名）
  *Credit in the release notes once the fix ships — if the reporter wants it.*

## 04// 特别说明 NOTES

- 和风天气凭据仅存于各人本地 `local.properties`，或运行时写入本机 `no_backup`，不随仓库分发
  *QWeather credentials live in each person's `local.properties`, or in on-device `no_backup` storage at runtime; they never ship with the repo.*
- 构建产物（APK）会内嵌编译期凭据，**请勿公开发布你带凭据构建的 APK**。实验室填写的 Token / 密钥不进系统备份
  *Builds embed whatever credentials you compile with — **never publish an APK you built with real credentials**. Tokens entered in the lab are excluded from system backup.*
- Release 附带的公共版 APK 由 `assemblePublicRelease` 构建。该独立变体会强制清空天气服务凭据，并使用随库公开证书 `keystore/public.jks` 签名
  *Public APKs are built with `assemblePublicRelease`, a dedicated variant that clears compile-time QWeather credentials and signs with the in-repo public key `keystore/public.jks`.*
- 若在仓库历史中发现任何凭据痕迹，请按安全漏洞上报，勿公开扩散
  *If you ever spot credential traces in the repository history, report them as a vulnerability — don't amplify them publicly.*

<!-- ZHISHENG WEATHER TERMINAL // security policy -->
