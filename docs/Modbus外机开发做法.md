# Modbus RTU 外机开发做法

## 1. 实施范围与决策

目标是在管理员页增加“Modbus 外机”入口，完成：

- 配置外机通讯和业务参数；
- 手动读取状态、可选自动轮询和在线/离线显示；
- 按寄存器表设置可写参数，并在写后回读确认；
- 不影响当前低温增焓、光伏、升温除湿等自定义协议功能。

默认实施路径选择 **App 自定义帧 → 主控/网关 → Modbus RTU 外机**。若硬件确认存在独立的第二 UART，可把本文第 4 节的网关接口替换为 App 内的 `ModbusRtuTransport`；页面、数据模型和状态流程保持不变。

## 2. 建议的页面和数据模型

在 `manager_main.xml` 中增加按钮 `@id/li_modbus_outdoor`，文字为“Modbus 外机”，由 `ManagerFragment` 的 `@OnClick` 打开 `ModbusOutdoorFragment`。该 Fragment 已处于管理员密码遮罩之后，无需再复制认证逻辑。

页面分为四块：

1. **通讯配置**：启用开关、从站地址（1–247）、轮询周期（建议 2–5 秒）、通讯状态、最后成功时间。波特率、校验位通常属于硬件/主控配置；若确实可配置，写入独立网关配置而不是临时改变 App 原串口。
2. **参数设置**：仅展示已在寄存器表批准的业务参数，如启停、模式、目标温度、频率上限。输入控件应显示单位、范围、步进和“写入”按钮。
3. **状态信息**：运行模式、关键温度/压力/频率、电流/电压、阀开度、告警/故障码、原始更新时间。字段以厂家寄存器表为准，不预设业务含义。
4. **诊断区**：立即读取、重新连接/重置网关、最近错误（超时、CRC、异常码）、受控的十六进制报文日志导出。生产 UI 不应开放任意寄存器写入。

建议新增以下不可变模型（Java 版本适配现有工程即可，不强制 Kotlin）：

```text
ModbusOutdoorConfig
  enabled, slaveId, pollIntervalMs, transportMode, lastUpdated

ModbusOutdoorStatus
  online, running, mode, values(Map<StatusKey, Number>), alarms,
  rawRegisters, updatedAt, quality, lastError

ModbusWriteRequest
  parameterKey, requestedValue, source(UI/MQTT), requestId

ModbusResult
  requestId, success, modbusExceptionCode, message, completedAt
```

持久化建议先使用现有 `MySpUtil` 的 JSON 保存 `ModbusOutdoorConfig`；若后续需多设备、历史曲线或审计，再迁移为 GreenDAO 表。保存的值必须是业务配置，不能保存未经验证的任意寄存器地址。

## 3. 已确认的 Modbus RTU 实现规则

水机通讯资料已经确认以下规则，网关固件或 App 直连实现必须严格使用：

```text
串口：9600 bps，8 数据位，无校验，1 停止位（8N1）
角色：水机主控板 = Modbus 从机；线控器/App/网关 = Modbus 主机
设置区：900–1699，03H 读、06H 单写、10H 多写
状态区：2000–2199，04H 只读
```

对应本次 Excel：`P01–P169` 的地址落在设置区，`B01–B30`、`D01–D12`、`Y00–Y23` 的地址落在状态区。状态轮询建议按以下三段执行 `04H`，每段所有寄存器是否都是一个 16 位值仍须以数据类型表确认：

| 读取段 | 起止地址 | 数量 | 覆盖内容 |
| --- | --- | --- | --- |
| 状态段 1 | 2121–2150 | 30 | B01–B30 模拟量/运行状态 |
| 状态段 2 | 2151–2162 | 12 | D01–D12 保护/开关状态 |
| 状态段 3 | 2170–2193 | 24 | Y00–Y23 输出、水流量、风机信息 |

读取和写入都必须串行化：等待一笔响应（或超时/有限重试）后才能发送下一笔，避免 RS485 总线冲突。对一项设置写入优先用 `06H`；只有多个**连续**寄存器且厂家确认其编码和原子写入语义时才用 `10H`。

> 地址基准尚未确认。实现中使用 `RegisterAddressResolver` 集中转换表中地址至 PDU 地址，先以抓包/厂家样例确定是否 `pduAddress = tableAddress - 1`，禁止在 UI 或各个调用点分散减 1。

## 4. 协议边界和 App—网关契约

### 4.1 不复用现有外机类型

不要直接把 Modbus 状态强行塞进 `OutDoorStatusInfo`：它假定了现有自定义报文的数据偏移、单位和 MQTT 字段。新设备应使用独立的 `ModbusOutdoorStatus` 和独立 EventBus 事件；需要云端共享的数据，再由明确的映射层转换。

在 `FunctionObject` 中增加一个未占用的设备类型常量（示例 `MODBUS_OUTDOOR = 0x0A`；最终值须与主控固件协议负责人确认），并为其定义稳定功能号。建议：

| 功能号 | App 语义 | 网关实际动作 | 响应 |
| --- | --- | --- | --- |
| `0x01` | 获取配置 | 返回当前从站/轮询/启用状态 | 配置结构 |
| `0x02` | 设置配置 | 校验并持久化网关配置 | 成功/失败码 |
| `0x03` | 读取状态 | 网关按寄存器表读取并归一化 | 标准状态结构 |
| `0x04` | 写业务参数 | 映射到批准的 Modbus 写寄存器 | 写后回读结果 |
| `0x05` | 诊断 | 返回链路状态、最近 Modbus 异常 | 诊断结构 |

App 自定义帧的外层格式保持不变：

```text
AA 55 | 版本 | 请求序号 | 命令 | termType | 长度 | functionId | functionLength | data | 自定义 CRC16
```

这不是让 App 自己发送 Modbus，而是把 `data` 定义为 App 与网关之间的版本化业务载荷。该载荷必须在固件与 App 联调前冻结：字段长度、字节序、版本、状态位、错误码、兼容策略都要写入双方协议文档。

### 4.2 网关侧 Modbus RTU 的职责

网关作为唯一 Modbus 主站，完成：

1. 根据已确认规则生成 RTU 请求：设置区用 `0x03/0x06/0x10`，状态区用 `0x04`。
2. 串行执行请求：一个请求在途；超时后有限重试；收到响应后校验从站、功能码、长度、CRC16。
3. 处理异常响应（功能码最高位置位，下一字节为异常码），不要将异常误报为普通离线。
4. 处理 RTU 帧间隔、RS485 收发方向和物理端口；默认 9600 但采用外机实际 8N1/8E1 等参数。
5. 校验值范围、写后回读、掉电保存；将 Modbus 原始错误转换为稳定的 App 错误码。
6. 严禁把 App 任意输入拼成“任意从站/任意寄存器写入”。网关仅暴露批准的参数键到寄存器映射。

## 5. App 代码改动建议

以下路径均基于当前工程组织，名称为建议，可随现有包风格调整。

```text
app/src/main/java/com/hy/greenbuilding/
  protocol/
    command/ModbusOutdoorCommand.java       # 自定义外层命令的组包
    ResPonseInfo/ModbusOutdoorStatus.java   # 只解析网关归一化状态载荷
    ModbusOutdoorPayloadCodec.java          # data 载荷编码/解码和长度校验
  event/
    ModbusOutdoorStatusEvent.java
    ModbusOutdoorResultEvent.java
  model/
    ModbusOutdoorConfig.java
    ModbusOutdoorParameter.java
  repository/
    ModbusOutdoorRepository.java            # 配置、轮询、请求去重、超时状态
  ui/fragment/
    ModbusOutdoorFragment.java
  res/layout/
    modbus_outdoor_fragment.xml
```

具体改动顺序：

1. 在 `FunctionObject.java` 预留并注释新设备类型、功能号和错误码；与网关固件使用同一份常量表。
2. 新建 `ModbusOutdoorCommand extends SpCommand`。像现有 `LowTempCommand` 一样设置外层命令、功能号、功能长度和优先级，但数据体仅使用已定义的载荷编码器。
3. 在 `SpDataProcessor.processSpResponse()` 中增加 **一个独立 `termType` 分支**：验证最小长度后交给 `ModbusOutdoorPayloadCodec`，发布新的状态/结果事件。不要修改原有低温/PV/升温分支。
4. `ModbusOutdoorRepository` 维护“同一时刻一个读取/写入请求”的业务状态；写操作停止或跳过本轮读取，成功或超时后恢复轮询。UI 不直接调用 `SpDataProcessor.send()`。
5. `ModbusOutdoorFragment` 订阅新事件并只在主线程刷新 UI；在 `onStart/onStop` 或 `onHiddenChanged` 中启动/停止页面级轮询，避免隐藏页面仍持续发包。
6. 在 `manager_main.xml` 增加入口，在 `ManagerFragment` 增加 `@BindView`、`@OnClick` 并打开新 Fragment。若保留旧的 `ManagerActivity` 入口，也同步增加，避免两处管理员入口行为不一致。
7. 若接入 MQTT：为外机定义独立的 DTO/Topic 字段和权限校验。云端写入必须经过与 UI 相同的参数范围校验、确认和结果上报流程。

## 6. 直接第二串口方案的最小实现

仅当硬件已验证有独立端口、且现场线控器不会与 App 同时作为主站时执行。新建 `ModbusRtuTransport`，内部持有**第二个** `SerialHelper("/dev/ttyXXX", 9600)` 或等价底层对象；`SpDataProcessor` 和 `/dev/ttyAS3` 保持原样。

要求：

- 有独立接收缓存；按 Modbus 规则依据请求期待长度/功能码及空闲间隔组帧，不能沿用 `AA 55` 查头逻辑。
- 请求队列必须串行；每个请求关联从站地址、功能码、期望长度、超时和重试次数。
- CRC 使用 Modbus RTU 低字节在前的线序；对站号、功能码、字节数、异常码、CRC 都校验。
- 默认超时建议 500–1000 ms、最多 2 次重试、请求间隔不少于外机手册规定值；具体值现场调优。
- 读写均形成结构化结果；离线不能无限重试；UI 销毁时正确停止轮询，应用退出时关闭第二口。

示例请求（仅说明编码方式，地址和值不可直接用于真实设备）：

```text
读保持寄存器 PDU: [slave][03][startHi][startLo][countHi][countLo][crcLo][crcHi]
写单寄存器 PDU:   [slave][06][registerHi][registerLo][valueHi][valueLo][crcLo][crcHi]
异常响应:          [slave][function | 80][exceptionCode][crcLo][crcHi]
```

注意 Modbus 地址经常有两种表示：文档的 `40001` 是显示地址，而 PDU 可能是 `0x0000`。开发只在一个地方转换，测试用厂家报文确认，禁止在 UI、Repository、Transport 各自减 1。

## 7. 已提供的水机地址表与 UI 首期范围

已读取 `D:\桌面\水机新增协议表.xlsx`。这是水机的寄存器**业务地址**清单，不是完整的 Modbus RTU 报文定义。实现时将地址表固化为受控的 `ParameterDefinition` / `StatusDefinition`，严禁把 UI 输入直接解释为任意寄存器操作。

首期管理员页建议只放入高频且风险可控的业务项：

| 页面分组 | 已知表内地址 | 建议用途 |
| --- | --- | --- |
| 状态总览 | `2121/B01` 进水温度、`2122/B02` 出水温度、`2123/B03` 环境温度、`2130/B10` 排气温度、`2132/B12` 水箱温度 | 只读显示 |
| 运行状态 | `2141/B21` 目标频率、`2142/B22` 当前频率、`2143/B23` 驱动运行状态、`2145/B25` 直流母线电压 | 只读显示 |
| 保护/开关 | `2151/D01` 至 `2162/D12` | 状态、告警/联锁展示 |
| 执行器 | `2170/Y00` 至 `2182/Y12` | 只读运行指示；不能直接当作控制写点 |
| 水路/风机 | `2183/Y13` 当前流量、`2184/Y14` 变频泵目标频率、`2185/Y15` 当前频率、`2191/Y21` 至 `2193/Y23` 风机转速 | 只读显示 |
| 常用设定 | `1001/P01` 制热设定温度、`1002/P02` 制冷设定温度、`1119/P119` 回水设定温度 | 有范围校验、二次确认、写后回读 |

`1102/P102` 是水机自身 Modbus 从站地址设置项（范围 1–99）。它属于高风险安装参数：**不应**在常规管理员页开放修改；仅可在安装/维护模式、独立确认流程和写后按新地址重连的条件下开放。

其余 P01–P169 多为除霜、EEV、压缩机、压力保护、风机和水泵的出厂调试参数。首版应全部隐藏或只读，不开放批量写入，避免改变保护逻辑。

## 8. 参数映射表模板（联调前填完）

| 参数键 | UI 名称/单位 | 权限 | Modbus 功能码 | PDU 0 基地址 | 寄存器数 | 类型/字序 | 比例/范围 | 写后回读 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `run_state` | 运行状态 | 只读 | 待确认 | 待确认 | 待确认 | 待确认 | 枚举 | 不适用 |
| `target_temperature` | 目标温度（℃） | 可写 | 待确认 | 待确认 | 待确认 | int16/待确认 | 待确认 | 必须 |
| `operation_mode` | 运行模式 | 可写 | 待确认 | 待确认 | 待确认 | enum/待确认 | 厂家枚举 | 必须 |
| `fault_code` | 故障码 | 只读 | 待确认 | 待确认 | 待确认 | bitset/uint16 | 厂家定义 | 不适用 |

没有完整映射表时，只实现“读取原始寄存器用于诊断”，不开放写入控制。

除地址外，每一项仍必须补齐：Modbus 数据区/功能码、PDU 0 基地址、寄存器数、数据类型、字节序、比例和只读/可写权限。表内的显示范围（例如 `P01` 的 0–100）可用于 UI 输入校验，但不能替代原始寄存器编码规则。

## 9. 联调与测试计划

1. **协议单元测试**：CRC 已知向量、寄存器地址换算、int16/uint16/int32/float 的大小端和比例、异常帧、截断帧、CRC 错帧。
2. **模拟器测试**：使用 Modbus slave simulator，覆盖正常响应、延迟、无响应、异常码 `01/02/03/04/06`、连续粘包和分包。
3. **网关契约测试**：针对每个 App 自定义功能号，固件与 App 互相保存请求/响应十六进制样例；包括旧 App/新网关、新 App/旧网关的兼容响应。
4. **台架测试**：逐项读取、写入、写后回读；拔 A/B、反接 A/B、断电重上电、错误从站地址和总线繁忙。
5. **回归测试**：原 `SpDataProcessor` 的低温/PV/升温外机状态及管理员页原有入口正常；验证新增轮询不会挤占原自定义指令。
6. **现场稳定性测试**：连续 24 小时轮询，记录成功率、平均响应时间、重试次数、离线恢复时间；确认终端电磁干扰环境下无误写。

## 10. 交付物和接口冻结点

开发前必须冻结：外机寄存器表、RS485 参数、App—网关载荷版本、错误码表、UI 字段和权限、云端字段（如需要）。交付应包含：App 安装包、主控/网关固件或配置文件、双方协议文档、寄存器映射表、抓包样例、测试报告和现场接线图。

在这些资料未冻结前，可以完成页面骨架、数据模型、事件链路和模拟数据；不能安全地实现真实的参数写入。
