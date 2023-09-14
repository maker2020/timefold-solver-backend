# 求解器地图应用Demo设计

## 管理模块

### Task Manager

描述：记录用户操作进度，允许后台运算（一天或更久之后再查看结果）。

## 程序实现

### 功能描述

地图任选五个点，可传入的配置（计算时间，是否后台计算），后台计算程序的管理。

### 实现逻辑

1. 任选五个点（含坐标信息）。将点和一些配置传入后台。

    - 配置项：运算时间，是否后台计算。

2. 得出组合列表C(2,5)，为每种组合中的两点调用路径规划api（选最短路径的规划策略）。
3. 将问题事实和计划对象应用到optaplanner框架按指定配置进行计算。

### 类的设计
