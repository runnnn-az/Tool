# Tool · 利率计算器

一个基于 Java Swing 的图形化利率计算工具，覆盖**贷款**与**存款**两大场景，提供完整的分期账单明细，方便快速估算资金成本与收益。

## 功能特性

### 🏦 贷款计算
- 支持三种还款方式：**等额本息**、**等额本金**、**先息后本**
- 一键计算月供、总利息、总还款额
- 展示完整逐月还款计划表：期数、月供、偿还本金、偿还利息、剩余本金

### 💰 存款计算
- 支持**单利**与**复利**两种计息方式
- 复利可选计息频率：按年 / 按六个月 / 按三个月 / 按月
- 计算到期本息合计、利息总额
- 展示逐期账单明细：期数、期初余额、本期利息、期末余额、累计利息

## 环境要求

- JDK 8 及以上

## 使用方法

方式一：编译后运行

```bash
javac -d out src/interest/*.java
java -cp out interest.InterestCalculatorApp
```

方式二：JDK 11+ 直接单文件运行

```bash
java src/interest/InterestCalculatorApp.java
```

启动后即可看到「贷款计算 | 存款计算」两个页签，填写金额、利率、期限等信息后点击「计算」。

## 项目结构

```
Tool/
├── src/interest/
│   ├── InterestCalculatorApp.java   # 主程序入口（窗口与页签）
│   ├── LoanPanel.java               # 贷款计算面板
│   └── DepositPanel.java            # 存款计算面板
├── out/                             # 编译输出目录（已加入 .gitignore）
├── .gitignore
├── LICENSE
└── README.md
```

## 许可证

[MIT](LICENSE) © 2026 runnnn-az
