# 0.0.3 稳定性周期

状态：开发中

基线版本：0.0.2

版本原则：不增加新功能，只测试、修 Bug、提高兼容性和运行稳定性，并打磨已有视觉效果。

## 范围锁定

0.0.3 可以做：

- 修复崩溃、错误数据、状态丢失和交互异常
- 修复 Android 8.0 及以上版本的兼容问题
- 补单元测试、静态检查和构建检查
- 降低不必要的重组、分配和后台工作
- 调整已有动画或天气氛围层的性能、清晰度和一致性
- 修正文案、无障碍描述和明显的显示问题

0.0.3 不做：

- 不增加天气模块、数据源或新的页面
- 不增加权限、账号、统计、广告或后台服务
- 不大改视觉方向，不重做现有信息架构
- 不借修复之名改变公共版与满血版的功能边界

## 自动检查

每次准备合并时必须执行：

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleRelease -PpublicBuild
```

发布前还要在本地凭据完整的环境中执行一次满血版构建：

```bash
./gradlew assembleRelease
```

## 手工测试矩阵

### 安装与生命周期

- Android 8.0（API 26）冷启动、返回前台和锁屏唤醒
- Android 8.1（API 27）及较新系统冷启动
- 横竖屏切换后仍停留在当前页面
- 系统返回键在搜索页、设置页和首页行为正确
- 前后台切换不会重复发起失控的网络请求

### 数据与异常

- 公共版：Open-Meteo、小米天气、自动优选分别检查
- 满血版：和风天气和自动优选分别检查
- 无网络、超时、单一数据源失败和全部数据源失败
- 国内、海外、同名城市和无搜索结果
- 跨日、跨时区、白天/夜间图标和月相日期
- 缺少预警、生活指数、台风或昨日数据时不崩溃

### 界面与效果

- 2x2、4x2、4x4 小组件首次添加、刷新和城市切换
- 雨、雪、雾、雷暴效果的关闭、三档强度和长时间运行
- 低端设备滚动、逐时曲线和启动打字动画无明显卡顿
- 字体放大、深色状态栏和导航栏遮挡情况

## 发布门槛

- 自动检查全部通过，Lint 不能有 Error
- 公共版和满血版都能成功构建
- API 26、API 27 和至少一个较新 Android 版本完成手工冒烟测试
- 没有已知崩溃、数据错位、密钥泄露或升级安装阻断
- 0.0.2 公共版可以正常覆盖升级到 0.0.3 公共版
- README、Release 说明和 APK 版本号一致

## 2026-08-09 首轮记录

- 建立独立的 0.0.3 稳定性开发线，`versionName` 升至 `0.0.3`，`versionCode` 升至 `20260809`
- 基线项目没有单元测试，`testDebugUnitTest` 为 `NO-SOURCE`
- 首次 Lint 发现 2 个 Error、52 个 Warning
- 两个 Error 均来自 API 26 直接调用 API 27 的锁屏/亮屏接口；已增加 Android 8.0 兼容分支
- 移除清单中 API 26 会忽略的重复 `turnScreenOn` 属性
- 启动和空状态打字动画改用 `mutableIntStateOf`，减少高频整数状态的装箱
- 新增单位换算、蒲福风级边界、气压格式和月相日期回归测试
- 风向值先归一化再映射方位；数据源偶发返回负角度、超范围值或非有限数时不再有数组越界风险
- CI 增加单元测试、Lint 和公共 Release 构建

首轮自动复测：

- `testDebugUnitTest`：11 项通过，0 失败
- `lintDebug`：0 Error、51 Warning
- `assembleDebug`：通过
- `assembleRelease -PpublicBuild`：通过；APK 内版本为 `0.0.3 (20260809)`，四项和风配置均为空，使用公开签名
- `assembleRelease`：通过；使用私人签名

真机冒烟（小米 2405CPX3DC，Android 16 / API 36）：

- 从已安装的满血版 `0.0.2 (20260808)` 覆盖升级到 `0.0.3 (20260809)`：成功，签名一致，应用数据保留
- 灭屏 Dozing 状态冷启动：381 ms；启动后系统进入 Awake，确认厂商 ROM 兼容回退有效
- 连续 3 次前后台恢复：59 / 47 / 56 ms
- 进程保持运行；Logcat 未发现 `FATAL EXCEPTION` 或应用 ANR
- 未绕过设备锁屏，也未截取、保存或公开包含城市等私人信息的界面

剩余 51 个 Lint Warning 分类：

| 类型 | 数量 | 当前处理意见 |
|:--|--:|:--|
| HardcodedText | 15 | 小组件占位文案，后续统一提取资源 |
| SmallSp | 10 | 终端式小组件密排设计，结合真机可读性再定 |
| UnusedAttribute | 9 | API 31 小组件属性在旧系统被忽略，需分版本资源评估 |
| IconDuplicates / IconDuplicatesConfig / IconLocation | 7 | 启动图标资源专项检查 |
| MonochromeLauncherIcon | 2 | Android 13 主题图标专项检查 |
| UnusedResources | 2 | 确认无运行时引用后再删除 |
| UseCompoundDrawables / NestedWeights | 4 | 小组件布局性能项，需保持当前像素效果 |
| OldTargetApi | 1 | 升级 compile/target SDK 时统一处理 |
| ObsoleteSdkInt | 1 | 启动图标资源目录整理时处理 |

当前阶段：自动基线和一台较新 Android 真机冒烟已通过。当前环境未安装 API 26 / 27 系统镜像，这两档兼容性仍是发布前必测项；其余 Warning 按上表继续专项处理。
