# 枳生天气 · 0.0.3test 工作副本交接

> 建立：2026-08-11
> 用途：0.0.3 稳定性周期内专门复制的测试副本，**已合入 7 个 bug 修复 + 版本号显式标记为 0.0.3test**
> 定位：下一版从这里直接构建，不要再回到正式 0.0.3 目录（那里没有这 7 步修复）

---

## 一、这个目录是什么

- 是 `ZhishengWeather-0.0.3` 的工作副本，**可独立构建**
- 基于 0.0.3 正式版的 git 分支 `agent/v0.0.3-stability`（HEAD `33e3284`）
- 在正式版基础上叠加了 **7 个 bug 修复**（详见 `测试报告_0.0.3核对结果.md`）
- `versionName` 由 `0.0.3` 改为 `0.0.3test`，`versionCode` 仍是 `20260809`（同签名可 `-r` 覆盖安装，无需升码）
- 已通过真机部署验证（小米 2405CPX3DC，Android 16）

## 二、未提交的工作区改动（git status）

```
 M app/build.gradle.kts                            # versionName → "0.0.3test"
 M app/src/main/kotlin/.../data/OpenMeteoApi.kt    # Bug B
 M app/src/main/kotlin/.../data/OpenMeteoSource.kt  # Bug E
 M app/src/main/kotlin/.../data/WeatherRepository.kt # Bug F
 M app/src/main/kotlin/.../ui/WeatherViewModel.kt  # Bug C、G
 M app/src/main/kotlin/.../ui/home/HomeScreen.kt    # Bug A、D、G
?? app/src/test/kotlin/.../ui/home/                # 新增 TempBarParamsTest.kt（12 项）
?? 测试报告_0.0.3核对结果.md                       # 详细测试报告
```

当前 HEAD：`33e3284 Merge pull request #5 from ZhishengZZ/agent/icon-remove-corners`

> 这些改动**没有 commit**。下一版要发的时候再决定是 squash 进一个 patch commit 还是用正式 commit 推上 main。

## 三、7 个修复（对 vs 应不应该改的结论）

详细见 `测试报告_0.0.3核对结果.md`，这里只放结论表：

| # | Bug | 提取/修复 | 必要度 |
|---|---|---|---|
| A | 逐日温度条 `coerceIn(0.03f, 1f-lo)` 在 lo>0.97 时崩溃 | 提 `tempBarParams` 纯函数 | **必修** |
| B | Open-Meteo 三处 `resp.body!!` | `body?.string() ?: return@withContext null` | 防御性 |
| C | 切城市取消任务时 loading 卡死 | try/finally + `fetchJob === job` 仅当前任务清 | **必修** |
| D | 华氏度模式下昨日 ΔT 与读数不一致 | 提 `tempDelta` 按显示单位算 | **应改** |
| E | 分钟降水 `take(9)` 与文案 120min 不符 | 改 `take(8)` | 可改可不改 |
| F | 小米能见度未按 unit 换算 | `when(unit) { "m" -> /1000 }` | **应改** |
| G | "开机自检动画"开关无人读取 | DisplayPrefs 加 bootAnim + BootState 参数 | **必修** |

改动范围严格限定在各自显示/状态逻辑内，正常路径行为等价，不影响其他功能（27 项测试全绿、lint 0 Error）。

## 四、怎么构建（含本机踩平的两个坑）

> 重要：直接在 PowerShell 里跑 `gradlew.bat` 会被命令工具杀掉 daemon 子进程；中文 JAVA_HOME 传给 `gradlew.bat` 会编码失效被判为 invalid directory。所以本副本根目录放了一个 `build.ps1`，下次直接用它。

### 推荐：用本目录自带的 build.ps1

```powershell
cd 'D:\ZhishengWeatherProject\ZhishengWeather-0.0.3 - test'
.\build.ps1        # 默认 assembleRelease，输出 app\build\outputs\apk\release\app-release.apk
```

`build.ps1` 关键点：
- `JAVA_HOME` 强制设为**纯英文路径** `D:\android-build-tools\jdk-17.0.13+11`（这套工具是 2026-08-11 从 `D:\金川党建数据大屏项目\tools` 复制出来的副本，专治中文路径让 gradlew.bat 报 invalid directory / aapt2 乱码）
- `--no-daemon`：单次 daemon，避免被宿主工具误杀 daemon 子进程
- 输出写日志 `build.log`，便于本身有 nohup/background 语义的工具直接异步取走

> SDK 路径在 `local.properties` 的 `sdk.dir` 里还是原中文路径（`D:\金川党建数据大屏项目\tools\android-sdk`），gradle 读取字符串值没问题，不用动；只有 JAVA_HOME 经 bat 环境变量传递才会被中文坑。若将来也想把 SDK 切到英文路径，把 local.properties 改成 `sdk.dir=D\:\\android-build-tools\\android-sdk` 即可。

### 手敲版（如果 build.ps1 不可用）

```powershell
cd 'D:\ZhishengWeatherProject\ZhishengWeather-0.0.3 - test'
$env:JAVA_HOME = 'D:\android-build-tools\jdk-17.0.13+11'
.\gradlew.bat assembleRelease --no-daemon --console=plain
```

### 各构建类型

```powershell
.\build.ps1 -Task assembleDebug                   # 调试包
.\build.ps1 -Task assembleRelease                 # 满血版（私人签名，本机自用，默认）
.\build.ps1 -Task 'assembleRelease -PpublicBuild' # 公共版（清空和风凭据 + 随库公开证书）
.\build.ps1 -Task 'testDebugUnitTest lintDebug'   # 跑测试 + lint（27 项 / 0 Error）
```

## 五、装机（小米真机）

```powershell
$adb = 'D:\金川党建数据大屏项目\tools\android-sdk\platform-tools\adb.exe'
& $adb devices -l                                    # 确认手机连上
& $adb install -r 'app\build\outputs\apk\release\app-release.apk'
& $adb shell am start -n com.zhisheng.weather/.MainActivity
```

- `-r` 覆盖安装，**同签名才覆盖得了**（私人 `keystore/zhisheng.jks`，口令 `zhisheng123` 在 local.properties）
- 当前手机上就是这套签名，下版继续用同 keystore 即可
- MIUI 拦 INJECT_EVENTS，无法 adb 滑屏/点亮，验收靠手滑 + `adb shell screencap`

## 六、已知遗留（不要顺手"修"）

### 测试报告里的 6 个低优先级问题（不影响功能，记录在案）

1. `LocationSource.xiaomiReverse` 归属地 `reversed()`，与 `CityRepository.search` 的格式/顺序可能不一致
2. `QWeatherApi.lat()` 实为通用坐标格式化却用来格式化 lon，命名误导（功能正确，建议加 `lon()` 别名）
3. `WeatherViewModel.lastFetchedKey` / `lastFetchKey` 双变量职责不同但总同步，维护隐患
4. 逐时/逐日用系统时区而非城市时区，海外城市逐时标签会偏移（主 App 与小组件一致，属**设计选择**）
5. `pct` 的 `<=1.0` 边界：和风返回 0-100 整数不会触发，低风险
6. `OpenMeteoSource.us_aqi` value 和 level 各算一次 `Math.round`，轻微性能

### 0.0.3 稳定性周期遗留

- 37 个 Lint Warning（SmallSp 10 / 小组件属性 9 / 图标 7 / Monochrome 2 / 其他），清单见 `STABILITY_0.0.3.md`
- API 26 / API 27 真机未测（本机无这两档系统镜像，发布前必测项）
- "明显"氛围档 VIVID 的雷暴扫描频率 / 粒子密度已调，长时间运行真机表现待 verify

## 七、下一版从这里开始的建议操作

### 路线 A：作为 0.0.3 的 patch 发布

1. 决定版本号：`versionName` 改 `0.0.3-patch`、`versionCode` 升到 `20260811`（保证与已发布的 0.0.3 公共版能升级）
2. commit 这 6 个改动 + 1 个新测试，message：`fix(0.0.3-patch): merge 7 bug fixes from test copy`
3. 在 main 分支走 PR 合入，打 tag `v0.0.3-patch`
4. 公共版构建 `.\build.ps1 -Task 'assembleRelease -PpublicBuild'`，发 Release
5. 满血版 Release 跑一次本机完整凭据构建，自留自用

### 路线 B：直接开 0.0.4

1. `versionName=0.0.4`，`versionCode=20260812`
2. 把本副本改名为 `ZhishengWeather-0.0.4` 或在 0.0.4 分支上延续这 7 步修复
3. 顺手处理上面 6 个低优里的 #2（加 `lon()` 别名）和 #3（合并双变量）

### 不管哪条路线

- 先在本地凭据完整的环境跑一次满血版构建验证
- 9 个区块主屏滑一遍、设温度单位切 ℉ 看昨日 ΔT、关 bootAnim 冷启
- grep 全项目确认无凭据残留（`QW_PRIVATE_KEY` 为空 / 无 ed25519 私钥文本）再发公共版

## 八、关键路径速查

| 项 | 路径 |
|---|---|
| 工程根 | `D:\ZhishengWeatherProject\ZhishengWeather-0.0.3 - test` |
| 构建脚本 | `.\build.ps1`（本目录新建） |
| 构建日志 | `build.log`（本目录） |
| 私人 keystore | `keystore\zhisheng.jks`（口令在 `local.properties`） |
| 公共 keystore | `keystore\public.jks`（public123，随库公开） |
| 测试报告 | `测试报告_0.0.3核对结果.md` |
| 稳定性记录 | `STABILITY_0.0.3.md` |
| JDK（推荐英文路径） | `D:\android-build-tools\jdk-17.0.13+11` |
| android-sdk | `D:\金川党建数据大屏项目\tools\android-sdk`（也可换 D:\android-build-tools\android-sdk） |
| adb | `D:\金川党建数据大屏项目\tools\android-sdk\platform-tools\adb.exe` |

---

<sub>ZHISHENG WEATHER TERMINAL // 0.0.3test housekeeping · 2026-08-11</sub>