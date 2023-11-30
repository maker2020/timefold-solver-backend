# 求解器应用Demo设计

## 子模块

1. TSP问题
2. CFLP问题

## 1. CFLP问题

### CFLP-功能描述

Capacity Facility Location Problem: 有容量的设施位置问题。

然而，在实际实现中，我们可以：

地图上可以自己标记服务站、客户。这两个对象有自己的属性，页面中，用户可以自己定义约束规则，来实现对特定问题场景的优化/分配。

**实现概要：**

在不确认的业务背景下，某端的数量是不确定的。因此，程序根据输入的服务站数量m、客户数量n，得出最大关系数量m*n。产生mn条关系对象，求解过程主要通过设置约束来影响求解器分配倾向。

真正的规划变量(Genuine Planning Var)：客户、服务站、分配量

[影子变量](https://timefold.ai/docs/timefold-solver/latest/configuration/configuration#shadowVariable)：客户、服务站、分配关系这三个均有自己的自定义影子变量。

## 2. TSP问题

### TSP-功能描述

travelling seller problem: 旅行商问题

地图上有仓库和访问者（卡车）这两个角色，程序为每个卡车从它们各自的起点，规划路线来访问所有仓库。（点与点之间的规划采用地图API，两点规划策略可以配置）

**实现概要：**

这个问题求解前会先进行一个预处理（组合点与点间调用API来得到两点的优化值，这个优化值的含义根据API策略配置决定），简单示例：

1. 任选五个点（含坐标信息、接受的规划策略）。将点和一些配置传入后台。

    - 配置项：运算时间，是否后台计算。

2. 得出组合列表C(2,5)，为每种组合中的两点调用路径规划api（选最短路径或其他可选的规划策略）。
3. 开始求解。

真正的规划变量(Genuine Planning Var)：访问者中的客户List。

[链式影子变量](https://timefold.ai/docs/timefold-solver/latest/configuration/configuration#chainedPlanningVariable)：客户中记录访问前后客户的链式结构的变量。

## 管理模块

主要实现以下功能：

1. 用户求解每个问题是后台异步单独进行的，每个求解都可以实时轮询获取结果，以及每个结果都实时同步到数据库，有单独页面可以管理问题的增删改查，重新计算。

2. 可以实时计算（变更参数、问题事实输入后重新计算——区分为冷/热启动）。

## Solver配置及算法说明

求解器内置了几个通用智能优化算法（模拟退火SA、禁忌搜索、）解决了几乎大多数场景。因此，通常只需要在resources以构建启发式+局部搜索（包含以上所说的智能优化算法）来配置Solver，就能满足基本需求。

> 备注：不能忽略顺序问题。

```XML
<solver>
    ......

    <!-- 构建启发式 -->
    <constructionHeuristic>
    </constructionHeuristic>
    <!-- 局部搜索 -->
    <localSearch></localSearch>

    ......
</solver>
```

此外，可以通过官网的benchmark（基准测试）方案来进一步确立更好的算法及算法配置结构。

## 求解器相关说明

> 只写本demo案例用到的关注点。其余详细看官网文档。

### 规划变量

1. ```@PlanningVariable```和```@PlanningListVariable```。

    a. 前者不支持标记到List上，后者可以。

    b. ```@PlanningListVar```标记List为规划变量，求解器可能对列表进行切割、排列组合等，但不管怎样给出的不同实体的规划List不会重复。

    c. ```@PlanningVariable```标记单个对象为规划变量。求解器可能对其排列组合等。不同实体的规划变量彼此会重复，需要自己来控制（用约束）。

2. ```@PlanningId```，通常标记实际的ID属性，后端自己根据前端问题，赋上ID。如果被求解器使用于规划的变量没有ID是不被允许的。

### 影子变量

1. 触发顺序(Trigger Order)要了解清楚。否则如果这些变量参与分数计算，则会导致分数偏差。

2. 含影子变量的类必须标记为PlanningEntity。（任何规划实体PlanningEntity都要写到Solver配置中去）

### Solver配置

1. ```<Forager>```标签可以控制结果，例如数量或其他限制等。例如：如果页面需要展示更多差的解决方案，可以调整。

## Git仓库地址

<http://cloud.keyvalues.cn:18081/hufs/opta-demo.git>

## optaplanner变更为timefold

API使用上的唯一区别: 少了移动邻里选择器、多线程求解。

优点是：更轻量、更高效、社区持续维护。官网：<https://timefold.ai/>

## 自定义约束设计说明

求解器约束定义的JavaAPI配置类为```ScoreDirectorFactoryConfig```，方法对应```setConstraintProviderClass()```
只接受Class类型参数，设置好后，求解器会通过反射调用默认无参构造方法。**因此实现一个Wrapper类，无参构造时对程序开发中定义的约束类XXConstraintProvider生成代理对象**

内置约束（开发定义的）所有权重和分数等级都定义在一个配置类中，可单独配置修改。而自定义约束的权重、分数等都是定义时一并设置好的。

自定义约束定义后，把结构存到数据库，该demo目前在切面中读取数据库，再在代理生成约束时从request对象拿去。

生成约束的关键类为```ConstraintGenerator```，其中```generate()```通过针对每种操作对象的每种操作方法做了构造方法，最后返回一个Constraint链式结构的对象。

**补充说明**：

1. 这个自定义还没有进一步抽象到每个Solution求解的层级，需要进一步抽象。
2. 数据结构采取**表达式树**可能更直观。

## Solution建模有关设计模式说明

官方说明：<https://timefold.ai/docs/timefold-solver/latest/design-patterns/design-patterns>

DEMO中的两个Solution，TSP和官方的原则一致即1-*。但是，CFLP并没有遵循它的建议，不过demo的设计也能达到效果，如果按官方1-1来表示多对多，必须确定一边的数量（在业务层面定下来），所以没有采用。

此外还有一个关于求解器关于ShadowVariable的issue没有得到解决。

## 动态求解（热重启）说明

意思是，在已开始求解后，还没结束时，变更求解问题的客观事实，此时可以采用冷启动（目前采用的）和热启动。热启动的接口方法签名示例：
```cn.keyvalues.optaplanner.solution.cflp.service.CFLPService.addStationRealTime(UUID problemID, ServerStation newStation)```
但有一些因素没实现完成，具体说明在方法实现的注释中。

## SolutionHelper类

为了省去对全局对象的操作、重复步骤及定时清理。从而避免每个solution都要管理队列和轮询方法。

**循环引用说明**：
大多数问题模型都存在循环引用关系，所以每个@PlanningSolution类统一实现CircularRefRelease接口以便solutionhelper去操作。
