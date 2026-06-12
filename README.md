# TVBoxOSC 自定义增强版

基于 [q215613905/TVBoxOS](https://github.com/q215613905/TVBoxOS) 的 Android TV 视频播放器，集成多项稳定性与体验优化。

## 功能特性

### 📦 稳定性增强
- **全局崩溃捕获** — 闪退时自动记录堆栈日志 + 友好提示 + 自动重启
- **播放加载超时** — 45 秒无响应自动切换播放器/线路
- **配置刷新容错** — 配置加载失败自动尝试历史配置，支持国内代理镜像

### 🎬 播放体验
- **倍速记忆** — 播放速度跨视频保留，无需每次调整
- **续播确认** — 打开有进度的视频时弹窗询问"从断点继续？"
- **字幕搜索** — 无字幕时自动弹出 assrt.net 搜索（国内可访问）

### 🎨 UI 视觉
- **深色主题** — 纯黑背景 `#0A0A0C`，深灰卡片 `#1F2026`
- **焦点动效** — 获焦放大 10% + 白边描边 + 平滑插值器
- **卡片间距** — 24dp 间隔，12dp 圆角，文字透明悬浮
- **功能按钮缩放** — 按钮高度缩 30%，更紧凑

### 🗄️ 数据管理
- **收藏导出/导入** — JSON 格式，方便换设备迁移
- **自动缓存清理** — 7天前的旧缓存自动删除
- **源健康检测** — 设置内一键检测所有片源状态

### ⚡ 启动优化
- 非关键初始化延迟 300ms 执行
- 配置缓存优先加载，冷启动秒开
- HTTP 服务器端口扫描从 21 次缩短至 3 次

## 精简配置

默认使用 [jsm_lite.json](https://github.com/LeeCQiang/tvbox/blob/master/jsm_lite.json) — 从原 164 源精简至 **81 个可用源**：

| 保留 | 移除 |
|---|---|
| 直连影视源（量子/非凡/索尼等） | 磁力源（电视无法播放） |
| APP 源（顾我/热播/三秋等） | 云盘源（需登录授权） |
| 4K 源（玩偶/木偶） | 教育/听书/音乐/DJ/体育 |
| 动漫源 | 官源 js 爬虫（GitHub 被墙） |
| 解析线路×5 | 29 个解析→5 个 |

## 下载

APK 在 [Releases](https://github.com/LeeCQiang/tvbox-osc-custom/releases) 页面。

| 版本 | 架构 | 说明 |
|---|---|---|
| `TVBox_*-java32.apk` | ARM 32-bit | 适用于大部分电视盒子 |
| `TVBox_*-java64.apk` | ARM 64-bit | 适用于新设备 |

## 使用

### 首次启动
1. 安装 APK
2. 打开 APP，自动加载精简配置
3. 首页默认源为 **顾我**（速度较快）
4. 如需切换源，在设置 → 首页站源中选择

### 配置地址
默认配置已内置，如需更换：`设置 → 配置地址 → 填入新地址`

## 构建

```bash
git clone https://github.com/LeeCQiang/tvbox-osc-custom.git
cd tvbox-osc-custom
./gradlew assembleNormalDebug
```

APK 输出路径：`app/build/outputs/apk/normal/debug/`

## 致谢

- [q215613905/TVBoxOS](https://github.com/q215613905/TVBoxOS) — 原始源码
- [j4Uq/TVBoxOSC](https://github.com/j4Uq/TVBoxOSC) — CI 构建脚本
- [qist/tvbox](https://github.com/qist/tvbox) — 配置源整理
