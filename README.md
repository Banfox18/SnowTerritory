# SnowTerritory - MMOItems 强化编辑插件

一个功能完整的 Minecraft Paper 服务器插件，用于强化和编辑 MMOItems 物品。

## 功能特性

- ✅ **物品强化系统**：支持成功/失败/维持三种结果
- ✅ **GUI 界面**：直观的图形化强化界面
- ✅ **概率系统**：可配置不同等级的成功率
- ✅ **保护机制**：保护符防止失败降级
- ✅ **增强机制**：强化符提升成功率
- ✅ **经济集成**：支持 Vault 和 PlayerPoints
- ✅ **属性修改**：自动修改物品的攻击力、防御力等属性
- ✅ **等级显示**：物品名称显示强化等级（+1, +2...）

## 依赖要求

### 必需依赖
- **Paper/Spigot 1.21.1**
- **MMOItems 6.9.5+**

### 可选依赖
- **Vault** - 用于金币消耗功能
- **PlayerPoints** - 用于点券消耗功能

## 安装方法

1. 将编译好的 `SnowTerritory-1.0-SNAPSHOT.jar` 放入服务器的 `plugins` 文件夹
2. 确保已安装 MMOItems 插件
3. 启动服务器，插件会自动生成配置文件
4. 根据需要修改 `plugins/SnowTerritory/config.yml`

## 使用方法

### 命令

- `/edititem` - 打开物品强化界面（需要权限 `mmoitemseditor.edit`）
- `/reloadeditor` - 重载插件配置（需要权限 `mmoitemseditor.reload`）

### 强化流程

1. 手持或背包中有 MMOItems 物品
2. 执行 `/edititem` 打开强化界面
3. 在武器槽位放置要强化的物品
4. （可选）放置保护符防止降级
5. （可选）放置强化符提升成功率
6. 在材料槽位放置所需材料
7. 点击确认按钮进行强化

### 强化结果

- **成功**：物品等级 +1，属性提升
- **失败降级**：物品等级 -1，属性降低（使用保护符可避免）
- **维持不变**：等级和属性不变

## 配置说明

配置文件位于 `plugins/SnowTerritory/config.yml`

### 主要配置项

```yaml
reinforce:
  # 失败降级概率
  fail-degrade-chance: 0.3
  
  # 维持概率
  maintain-chance: 0.2
  
  # 成功时属性提升百分比
  attribute-boost-percent: 1.1  # 1.1 = +10%
  
  # 失败时属性降低百分比
  attribute-reduce-percent: 0.9  # 0.9 = -10%
  
  # 不同等级的成功率
  success-rates:
    level-0: 0.9   # 0级升1级: 90%
    level-1: 0.8   # 1级升2级: 80%
    # ...
  
  # 消耗配置
  cost:
    vault-gold: 1000      # 金币消耗
    player-points: 50     # 点券消耗
    materials: 6          # 材料数量
```

详细配置说明请查看生成的 `config.yml` 文件中的注释。

## 逻辑流程图

详细的插件逻辑判定流程图请查看 [FLOWCHART.md](FLOWCHART.md)，包含：
- 插件启动流程
- 命令执行流程
- GUI 交互流程
- 强化判定核心流程
- 概率计算流程
- 属性修改流程
- 完整时序图

## 开发信息

### 编译

```bash
mvn clean package
```

编译后的文件位于 `target/SnowTerritory-1.0-SNAPSHOT.jar`

### 项目结构

```
src/main/java/top/arctain/snowTerritory/
├── Main.java                    # 主类
├── commands/                    # 命令处理
│   ├── EditCommand.java
│   └── ReloadCommand.java
├── config/                      # 配置管理
│   └── PluginConfig.java
├── core/                        # 核心逻辑
│   └── ItemEditor.java
├── data/                        # 数据类
│   └── MMOItemData.java
├── gui/                         # GUI界面
│   └── ItemEditorGUI.java
├── listeners/                   # 事件监听
│   ├── GUIListener.java
│   └── ItemEditListener.java
└── utils/                       # 工具类
    ├── Utils.java
    ├── ConfigUtils.java
    └── NBTUtils.java
```

## 权限

- `mmoitemseditor.edit` - 使用物品编辑功能（默认：OP）
- `mmoitemseditor.reload` - 重载插件配置（默认：OP）

## 常见问题

### Q: 插件无法启用？
A: 请确保已安装 MMOItems 插件，并检查控制台错误信息。

### Q: 强化后物品属性没有变化？
A: 请确保物品是有效的 MMOItems 物品，并且物品有可修改的属性。

### Q: 经济系统不工作？
A: 请确保已安装 Vault 和对应的经济插件（如 EssentialsX），或 PlayerPoints 插件。

### Q: 如何自定义 GUI 布局？
A: 修改 `config.yml` 中的 `gui.slots` 配置项，调整各个槽位的位置。

## 更新日志

### v1.0-SNAPSHOT
- 初始版本
- 实现基本的物品强化功能
- 支持 GUI 界面
- 集成经济系统

## 作者

**Arctain**

## 许可证

本项目为私有项目，未经授权不得使用。

---

**注意**：本插件需要 MMOItems 插件才能正常工作。请确保服务器已正确安装并配置 MMOItems。

