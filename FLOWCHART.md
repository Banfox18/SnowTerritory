# SnowTerritory 插件逻辑流程图

本文档详细描述了 SnowTerritory 插件的完整逻辑判定流程。

## 目录

1. [插件启动流程](#1-插件启动流程)
2. [命令执行流程](#2-命令执行流程)
3. [GUI 交互流程](#3-gui-交互流程)
4. [强化判定核心流程](#4-强化判定核心流程)
5. [概率计算流程](#5-概率计算流程)

---

## 1. 插件启动流程

```mermaid
flowchart TD
    A[服务器启动] --> B[调用 onEnable]
    B --> C[初始化 NBTUtils]
    C --> D{检查依赖插件}
    D -->|MMOItems 不存在| E[记录严重错误]
    E --> F[禁用插件]
    F --> G[结束]
    D -->|MMOItems 存在| H[检查 Vault]
    H -->|Vault 不存在| I[记录警告]
    H -->|Vault 存在| J[检查 PlayerPoints]
    I --> J
    J -->|PlayerPoints 不存在| K[记录警告]
    J -->|PlayerPoints 存在| L[创建 PluginConfig]
    K --> L
    L --> M[加载配置文件]
    M --> N{配置文件存在?}
    N -->|否| O[创建默认配置]
    N -->|是| P[读取配置]
    O --> P
    P --> Q[注册命令 edititem]
    Q --> R[注册命令 reloadeditor]
    R --> S[注册 GUIListener]
    S --> T[注册 ItemEditListener]
    T --> U[输出启动信息]
    U --> V[插件启用完成]
```

---

## 2. 命令执行流程

### 2.1 /edititem 命令流程

```mermaid
flowchart TD
    A[玩家输入 /edititem] --> B{是否为玩家?}
    B -->|否| C[发送错误消息]
    C --> D[结束]
    B -->|是| E{检查权限}
    E -->|无权限且非OP| F[发送权限错误]
    F --> D
    E -->|有权限或OP| G[创建 ItemEditorGUI]
    G --> H[调用 openGUI]
    H --> I[创建 Inventory]
    I --> J[加载自定义槽位装饰]
    J --> K[创建确认按钮]
    K --> L[创建取消按钮]
    L --> M[打开GUI给玩家]
    M --> N[等待玩家操作]
```

### 2.2 /reloadeditor 命令流程

```mermaid
flowchart TD
    A[玩家输入 /reloadeditor] --> B{检查权限}
    B -->|无权限且非OP| C[发送权限错误]
    C --> D[结束]
    B -->|有权限或OP| E[调用 reloadConfig]
    E --> F[重新加载配置文件]
    F --> G[更新所有配置项]
    G --> H[发送成功消息]
    H --> D
```

---

## 3. GUI 交互流程

```mermaid
flowchart TD
    A[玩家在GUI中操作] --> B[触发 InventoryClickEvent]
    B --> C{是否为玩家?}
    C -->|否| D[忽略事件]
    C -->|是| E{检查GUI标题}
    E -->|不是本插件GUI| D
    E -->|是本插件GUI| F[获取点击槽位]
    F --> G{是否为可编辑槽位?}
    G -->|是| H[允许操作]
    G -->|否| I{槽位在GUI范围内?}
    I -->|是| J[取消事件]
    I -->|否| H
    H --> K{点击的物品}
    K -->|确认按钮| L[调用 applyReinforce]
    K -->|取消按钮| M[关闭GUI]
    K -->|其他| N[正常处理]
    M --> O[发送取消消息]
    L --> P[进入强化流程]
    J --> D
    N --> D
    O --> D
```

---

## 4. 强化判定核心流程

```mermaid
flowchart TD
    A[玩家点击确认按钮] --> B[调用 applyReinforce]
    B --> C[获取武器槽位物品]
    C --> D[获取保护符槽位物品]
    D --> E[获取强化符槽位物品]
    E --> F[获取6个材料槽位物品]
    F --> G{武器是否为MMOItem?}
    G -->|否| H[发送错误: 请放置有效MMO物品]
    H --> Z[结束]
    G -->|是| I{物品是否可强化?}
    I -->|否| J[发送错误: 此物品不可强化]
    J --> Z
    I -->|是| K{检查Vault金币}
    K -->|启用且余额不足| L[发送错误: 金币不足]
    L --> Z
    K -->|通过或未启用| M{检查PlayerPoints}
    M -->|启用且余额不足| N[发送错误: 点券不足]
    N --> Z
    M -->|通过或未启用| O{检查材料数量}
    O -->|材料不足| P[发送错误: 材料不足]
    P --> Z
    O -->|材料足够| Q[扣除所有消耗]
    Q --> R[扣除Vault金币]
    R --> S[扣除PlayerPoints点券]
    S --> T[消耗所有材料]
    T --> U[消耗保护符]
    U --> V[消耗强化符]
    V --> W[获取当前强化等级]
    W --> X[计算基础成功率]
    X --> Y{是否有强化符?}
    Y -->|是| AA[成功率 +0.1]
    Y -->|否| AB[保持基础成功率]
    AA --> AC{是否有保护符?}
    AB --> AC
    AC -->|是| AD[失败降级概率 = 0]
    AC -->|否| AE[使用配置的失败降级概率]
    AD --> AF[执行概率判定]
    AE --> AF
    AF --> AG[进入概率计算流程]
    AG --> AH{判定结果}
    AH -->|成功| AI[修改属性: 提升]
    AH -->|失败降级| AJ[修改属性: 降低]
    AH -->|维持| AK[不修改属性]
    AI --> AL[更新物品名称: 等级+1]
    AJ --> AM[更新物品名称: 等级-1]
    AK --> AN[保持原等级]
    AL --> AO[发送成功消息]
    AM --> AP[发送失败消息]
    AN --> AQ[发送维持消息]
    AO --> AR[更新GUI中的物品]
    AP --> AR
    AQ --> AR
    AR --> Z
```

---

## 5. 概率计算流程

```mermaid
flowchart TD
    A[调用 attemptReinforce] --> B[生成随机数 0.0-1.0]
    B --> C{随机数 <= 成功率?}
    C -->|是| D[返回 SUCCESS]
    C -->|否| E{随机数 <= 成功率+维持概率?}
    E -->|是| F[返回 MAINTAIN]
    E -->|否| G{随机数 <= 成功率+维持概率+失败降级概率?}
    G -->|是| H[返回 FAIL_DEGRADE]
    G -->|否| I[返回 MAINTAIN 默认]
    D --> J[强化成功分支]
    F --> K[强化维持分支]
    H --> L[强化失败降级分支]
    I --> K
```

---

## 6. 属性修改流程

```mermaid
flowchart TD
    A[调用 modifyMMOAttribute] --> B{MMOItem 是否为 null?}
    B -->|是| C[结束]
    B -->|否| D[创建 LiveMMOItem]
    D --> E{尝试使用新API}
    E -->|成功| F[检查是否有攻击伤害属性]
    E -->|失败| G[使用旧API方法]
    F -->|有| H[修改攻击伤害]
    F -->|无| I[检查攻击速度]
    H --> I
    I -->|有| J[修改攻击速度]
    I -->|无| K[检查最大生命值]
    J --> K
    K -->|有| L[修改最大生命值]
    K -->|无| M[检查防御力]
    L --> M
    M -->|有| N[修改防御力]
    M -->|无| O[更新物品]
    N --> O
    G --> O
    O --> P[完成]
```

---

## 7. 物品验证流程

```mermaid
flowchart TD
    A[检查物品是否可强化] --> B{物品是否为 null?}
    B -->|是| C[返回 false]
    B -->|否| D{物品是否有 ItemMeta?}
    D -->|否| C
    D -->|是| E{是否为 MMOItem?}
    E -->|否| C
    E -->|是| F{可强化物品列表是否为空?}
    F -->|是| G[返回 true 允许所有]
    F -->|否| H[获取物品显示名称]
    H --> I[获取物品Lore]
    I --> J[遍历可强化物品列表]
    J --> K{名称或Lore包含匹配字符串?}
    K -->|是| G
    K -->|否| L{还有更多匹配项?}
    L -->|是| J
    L -->|否| C
```

---

## 8. 完整强化流程时序图

```mermaid
sequenceDiagram
    participant P as 玩家
    participant C as EditCommand
    participant G as ItemEditorGUI
    participant L as GUIListener
    participant U as Utils
    participant M as MMOItems API
    participant E as Economy

    P->>C: /edititem
    C->>C: 检查权限
    C->>G: openGUI(player)
    G->>G: 创建Inventory
    G->>G: 添加按钮和装饰
    G->>P: 打开GUI
    
    P->>G: 放置物品到槽位
    P->>L: 点击确认按钮
    L->>G: applyReinforce(player, gui)
    G->>G: 获取槽位物品
    G->>U: isMMOItem(weapon)
    U->>M: 检查是否为MMOItem
    M-->>U: 返回结果
    U-->>G: true/false
    G->>U: isReinforceable(weapon)
    U-->>G: true/false
    G->>E: 检查金币余额
    E-->>G: 余额结果
    G->>G: 扣除消耗
    G->>U: getCurrentLevel(weapon)
    U-->>G: 当前等级
    G->>U: attemptReinforce(...)
    U->>U: 生成随机数
    U->>U: 概率判定
    U-->>G: 强化结果
    G->>M: 创建LiveMMOItem
    G->>U: modifyMMOAttribute(...)
    U->>M: 修改属性
    G->>U: updateItemName(...)
    U->>G: 更新完成
    G->>P: 发送结果消息
    G->>G: 更新GUI物品
```

---

## 9. 错误处理流程

```mermaid
flowchart TD
    A[执行操作] --> B{是否发生异常?}
    B -->|否| C[正常执行]
    B -->|是| D{异常类型}
    D -->|MMOItems API错误| E[捕获异常]
    D -->|经济系统错误| F[记录警告]
    D -->|配置错误| G[使用默认值]
    D -->|其他错误| H[记录错误日志]
    E --> I[尝试使用旧API]
    I --> J{旧API是否可用?}
    J -->|是| C
    J -->|否| K[发送错误消息给玩家]
    F --> L[继续执行 忽略经济]
    G --> C
    H --> K
    K --> M[结束]
    C --> N[完成操作]
    L --> N
```

---

## 关键判定点总结

### 1. 权限判定
- 玩家必须拥有 `mmoitemseditor.edit` 权限或是 OP
- 命令执行者必须是玩家（不能是控制台）

### 2. 物品判定
- 物品必须存在且不为空气
- 物品必须是有效的 MMOItems 物品
- 物品必须在可强化列表中（如果列表不为空）

### 3. 消耗判定
- Vault 金币：如果启用，必须足够
- PlayerPoints 点券：如果启用，必须足够
- 材料：必须达到配置的数量要求

### 4. 概率判定
- 成功率：根据当前等级从配置读取
- 强化符：增加 0.1 成功率
- 保护符：将失败降级概率设为 0
- 维持概率：从配置读取

### 5. 结果处理
- **成功**：属性提升，等级+1
- **失败降级**：属性降低，等级-1（有保护符则不降级）
- **维持**：属性不变，等级不变

---

## 配置影响点

1. **成功率配置**：影响 `attemptReinforce` 的判定
2. **消耗配置**：影响经济检查逻辑
3. **GUI配置**：影响界面布局和槽位
4. **可强化物品列表**：影响 `isReinforceable` 判定

---

**流程图说明**：
- 菱形：判定节点（if/else）
- 矩形：处理节点（方法调用）
- 圆角矩形：开始/结束节点
- 箭头：流程方向

