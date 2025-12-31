   # Stellar Colonizer 项目类和方法详细说明

## 概述

Stellar Colonizer 是一款基于 JavaFX 的 4X 策略游戏，玩家在广阔的宇宙中建立和发展自己的星际文明。本文档详细说明项目中每个类的作用和方法。

## 1. 核心类 (Core Classes)

### 1.1 GameEngine

**作用**: 游戏引擎，管理整个游戏的运行状态、初始化、回合处理等核心功能。

**方法说明**:

- `initialize()`: 初始化游戏引擎，包括生成银河系、创建玩家和AI派系、设置起始位置、分配初始殖民地等
- `nextTurn()`: 进入下一回合，处理所有派系的回合逻辑
- `update(deltaTime)`: 更新游戏状态，处理AI决策和胜利条件检查
- `pause()`: 暂停游戏
- `resume()`: 恢复游戏
- `isPaused()`: 检查游戏是否暂停
- `getGameState()`: 获取游戏状态对象
- `getGalaxy()`: 获取银河系对象
- `getFactions()`: 获取所有派系列表
- `getPlayerFaction()`: 获取玩家派系
- `getEventBus()`: 获取事件总线
- `getUniversalResourceMarket()`: 获取宇宙资源市场

### 1.2 GameState

**作用**: 管理游戏状态，包括当前回合、游戏速度等。

**方法说明**:

- `update(deltaTime)`: 更新游戏状态
- `nextTurn()`: 进入下一回合
- `endGame(faction, reason)`: 结束游戏
- `getCurrentTurn()`: 获取当前回合数
- `getGameSpeed()`: 获取游戏速度
- `setGameSpeed(speed)`: 设置游戏速度

### 1.3 GameSpeed

**作用**: 定义游戏速度枚举。

**方法说明**:

- `getMultiplier()`: 获取速度倍数
- `getDisplayName()`: 获取显示名称

## 2. 模型类 (Model Classes)

### 2.1 银河系相关类 (Galaxy Classes)

#### 2.1.1 Galaxy

**作用**: 表示整个银河系，管理星系、六边形网格、派系等。

**方法说明**:

- `addStarSystem(system)`: 添加星系
- `removeStarSystem(system)`: 移除星系
- `findStarSystem(name)`: 根据名称查找星系
- `getStarSystemsInRange(center, range)`: 获取指定范围内的星系
- `getColonizedPlanetCount()`: 获取已殖民行星数量
- `getTotalPlanetCount()`: 获取总行星数量
- `generateStarSystemConnections()`: 生成星系间的连接
- `getHexForStarSystem(system)`: 获取星系所在的六边形
- `getConnectedSystems(system)`: 获取与指定星系连接的星系
- `areSystemsConnected(from, to)`: 检查两个星系是否连接

#### 2.1.2 GalaxyGenerator

**作用**: 银河系生成器，负责生成星系、行星、星云等。

**方法说明**:

- `generateGalaxy(starCount)`: 生成包含指定数量星系的银河系
- `generateStarSystem()`: 生成星系
- `generatePlanet(system, orbitIndex)`: 生成行星
- `getRandomStarType()`: 随机获取恒星类型（加权）
- `selectPlanetType(starType, orbitDistance)`: 根据恒星类型和轨道距离选择行星类型
- `calculateTemperature(starType, orbitDistance)`: 计算行星温度
- `generateNebulas(galaxy, count)`: 生成星云
- `generateAsteroidFields(galaxy, count)`: 生成小行星带
- `calculateGridRadius(starCount)`: 计算网格半径

#### 2.1.3 HexGrid

**作用**: 六边形网格系统，管理六边形坐标和操作。

**方法说明**:

- `getHex(coord)`: 根据坐标获取六边形
- `getHexAt(q, r)`: 根据q、r坐标获取六边形
- `getNeighbors(hex)`: 获取六边形的邻居
- `getHexesInRange(center, range)`: 获取指定范围内的六边形
- `cubeToPixel(coord)`: 将立方坐标转换为像素坐标
- `pixelToCube(x, y)`: 将像素坐标转换为立方坐标
- `getAllHexes()`: 获取所有六边形
- `getRadius()`: 获取网格半径
- `getHexSize()`: 获取六边形大小

#### 2.1.4 CubeCoord

**作用**: 立方坐标系统，用于六边形网格的坐标表示。

**方法说明**:

- `add(other)`: 坐标相加
- `subtract(other)`: 坐标相减
- `distance(other)`: 计算到另一个坐标的距离
- `length()`: 计算坐标到原点的距离
- `equals(o)`: 比较坐标是否相等
- `hashCode()`: 获取哈希码
- `toString()`: 返回坐标字符串表示

#### 2.1.5 Hex

**作用**: 表示六边形网格中的一个单元格。

**方法说明**:

- `getCoord()`: 获取坐标
- `getType()`: 获取六边形类型
- `setType(type)`: 设置六边形类型
- `getStarSystem()`: 获取六边形中的星系
- `setStarSystem(starSystem)`: 设置六边形中的星系
- `getEntities()`: 获取六边形中的实体（舰队等）
- `getVisibility()`: 获取可见度
- `setVisibility(visibility)`: 设置可见度
- `getExploredBy()`: 获取探索该六边形的派系
- `setExploredBy(exploredBy)`: 设置探索该六边形的派系
- `isExplored()`: 检查是否已探索
- `hasStarSystem()`: 检查是否包含星系
- `getFleets()`: 获取六边形中的舰队列表
- `addEntity(entity)`: 添加实体
- `removeEntity(entity)`: 移除实体
- `containsFleet(faction)`: 检查是否包含指定派系的舰队

#### 2.1.6 StarSystem

**作用**: 表示一个星系，包含恒星、行星等。

**方法说明**:

- `getName()`: 获取星系名称
- `getStarType()`: 获取恒星类型
- `getPlanets()`: 获取行星列表
- `addPlanet(planet)`: 添加行星
- `removePlanet(planet)`: 移除行星
- `getPosition()`: 获取位置
- `setPosition(position)`: 设置位置
- `getControllingFaction()`: 获取控制派系
- `setControllingFaction(faction)`: 设置控制派系
- `getPlanetCount()`: 获取行星数量

#### 2.1.7 Planet

**作用**: 表示一个行星，包含类型、大小、轨道等信息。

**方法说明**:

- `getName()`: 获取行星名称
- `getType()`: 获取行星类型
- `getSize()`: 获取行星大小
- `getOrbitDistance()`: 获取轨道距离
- `getHabitability()`: 获取宜居度
- `setHabitability(habitability)`: 设置宜居度
- `canColonize(faction)`: 检查是否可以被指定派系殖民
- `getColony()`: 获取殖民地
- `setColony(colony)`: 设置殖民地
- `getStarSystem()`: 获取所属星系
- `ensureMinimumHabitability(minHabitability)`: 确保最低宜居度
- `getResource(type)`: 获取特定类型的资源

### 2.2 派系相关类 (Faction Classes)

#### 2.2.1 Faction

**作用**: 表示一个派系（玩家或AI），管理资源、殖民地、科技等。

**方法说明**:

- `getName()`: 获取派系名称
- `isAI()`: 检查是否为AI派系
- `getResourceStockpile()`: 获取资源库存
- `getColonies()`: 获取殖民地列表
- `addColony(colony)`: 添加殖民地
- `removeColony(colony)`: 移除殖民地
- `getTechTree()`: 获取科技树
- `getAIController()`: 获取AI控制器
- `setAIController(aiController)`: 设置AI控制器
- `processTurn()`: 处理派系回合
- `getDiplomacyManager()`: 获取外交管理器
- `getRelationshipWith(otherFaction)`: 获取与其他派系的关系
- `getColor()`: 获取派系颜色
- `setColor(color)`: 设置派系颜色
- `getGalaxy()`: 获取银河系引用
- `setGalaxy(galaxy)`: 设置银河系引用

#### 2.2.2 PlayerFaction

**作用**: 表示玩家派系，继承自Faction。

**方法说明**:

- 继承Faction的所有方法
- 无额外方法（作为玩家派系的标识）

#### 2.2.3 AIController

**作用**: AI派系控制器，负责AI决策。

**方法说明**:

- `makeDecision()`: AI决策逻辑
- `makeDecision(gameState)`: 基于游戏状态的AI决策

### 2.3 殖民地相关类 (Colony Classes)

#### 2.3.1 Colony

**作用**: 表示一个殖民地，管理行星开发、建筑、资源产出等。

**方法说明**:

- `getName()`: 获取殖民地名称
- `getPlanet()`: 获取所属行星
- `getFaction()`: 获取所属派系
- `getBuildings()`: 获取建筑列表
- `addBuilding(building)`: 添加建筑
- `removeBuilding(building)`: 移除建筑
- `hasBuilding(buildingType)`: 检查是否拥有指定类型的建筑
- `processTurn()`: 处理殖民地回合
- `calculateResourceProduction()`: 计算资源产出
- `calculateResourceConsumption()`: 计算资源消耗
- `getPopulation()`: 获取人口
- `getGrowthRate()`: 获取增长率
- `getHappiness()`: 获取幸福度
- `getDefenses()`: 获取防御
- `getGovernor()`: 获取总督
- `setGovernor(governor)`: 设置总督
- `getGrowthFocus()`: 获取发展重点
- `setGrowthFocus(focus)`: 设置发展重点
- `getResourceStockpile()`: 获取资源库存
- `getUnlockedBuildings()`: 获取已解锁的建筑
- `canBuildBuilding(buildingType)`: 检查是否可以建造指定建筑
- `buildBuilding(buildingType)`: 建造建筑
- `upgradeBuilding(building)`: 升级建筑
- `getBuildingUpgrades()`: 获取建筑升级列表
- `getAvailableUpgrades()`: 获取可用升级
- `getTotalResourceProduction()`: 获取总资源产出
- `getTotalResourceConsumption()`: 获取总资源消耗

#### 2.3.2 BasicBuilding

**作用**: 基础建筑类，定义建筑的基本属性和行为。

**方法说明**:

- `getName()`: 获取建筑名称
- `getType()`: 获取建筑类型
- `getConstructionCost()`: 获取建造成本
- `getUpkeepCost()`: 获取维护成本
- `getConstructionTime()`: 获取建造时间
- `getEffects()`: 获取建筑效果
- `getRequiredTech()`: 获取需要的科技
- `canBeBuilt(colony)`: 检查是否可以在指定殖民地建造
- `applyEffects(colony)`: 应用建筑效果
- `removeEffects(colony)`: 移除建筑效果
- `getLevel()`: 获取建筑等级
- `getMaxLevel()`: 获取最大等级
- `isUpgradeable()`: 检查是否可升级
- `upgrade()`: 升级建筑

#### 2.3.3 Building

**作用**: 建筑类，继承自BasicBuilding。

**方法说明**:

- 继承BasicBuilding的所有方法
- `getConstructionProgress()`: 获取建造进度
- `setConstructionProgress(progress)`: 设置建造进度
- `isUnderConstruction()`: 检查是否在建造中
- `setUnderConstruction(underConstruction)`: 设置建造状态

#### 2.3.4 ColonyGovernor

**作用**: 殖民地总督，管理殖民地的治理。

**方法说明**:

- `getName()`: 获取总督名称
- `getLevel()`: 获取总督等级
- `getExperience()`: 获取总督经验
- `getPrimaryTrait()`: 获取主要特质
- `getSecondaryTrait()`: 获取次要特质
- `getAdministration()`: 获取行政能力
- `getLogistics()`: 获取后勤能力
- `getDiplomacy()`: 获取外交能力
- `getMilitary()`: 获取军事能力
- `getResearch()`: 获取科研能力
- `applyEffect(colony)`: 对殖民地应用效果
- `isAssigned()`: 检查是否已分配
- `setAssigned(assigned)`: 设置分配状态

#### 2.3.5 GrowthFocus

**作用**: 殖民地发展重点枚举。

**方法说明**:

- `getDisplayName()`: 获取显示名称
- `getDescription()`: 获取描述
- `getProductionBonus()`: 获取产出加成
- `getGrowthBonus()`: 获取增长加成

### 2.4 舰队相关类 (Fleet Classes)

#### 2.4.1 Fleet

**作用**: 表示一个舰队，管理舰船、任务、移动等。

**方法说明**:

- `getName()`: 获取舰队名称
- `getFaction()`: 获取所属派系
- `getCurrentHex()`: 获取当前六边形
- `getShips()`: 获取舰船列表
- `getTotalCombatPower()`: 获取总战斗力
- `getAverageSpeed()`: 获取平均速度
- `getDetectionRange()`: 获取探测范围
- `getFuelConsumption()`: 获取燃料消耗
- `isMoving()`: 检查是否在移动
- `getCurrentMission()`: 获取当前任务
- `getDestination()`: 获取目的地
- `getCommander()`: 获取指挥官
- `getSupplies()`: 获取补给
- `getSupplyEfficiency()`: 获取补给效率
- `addShip(ship)`: 添加舰船
- `removeShip(ship)`: 移除舰船
- `transferShip(ship, targetFleet)`: 转移舰船到其他舰队
- `moveTo(destination)`: 移动到目的地
- `canMove()`: 检查是否可以移动
- `setMission(mission, target)`: 设置任务
- `processTurn()`: 处理舰队回合
- `resupply(type, amount)`: 补给资源
- `resupplyAll()`: 补给所有资源
- `mergeFleet(otherFleet)`: 合并舰队
- `splitFleet(newFleetName, shipsToTransfer)`: 分离舰队
- `calculateTotalHealth()`: 计算总健康度
- `getShipCountByClass()`: 按类别获取舰船数量
- `getCompositionSummary()`: 获取组成摘要
- `hasMovedThisTurn()`: 检查本回合是否已移动
- `moveTowardsDestination()`: 向目的地移动
- `generateUniqueShipName(design)`: 生成唯一舰船名称

#### 2.4.2 Ship

**作用**: 表示一艘舰船，包含设计、状态、模块等。

**方法说明**:

- `getName()`: 获取舰船名称
- `getDesign()`: 获取舰船设计
- `getFaction()`: 获取所属派系
- `getHitPoints()`: 获取生命值
- `getMaxHitPoints()`: 获取最大生命值
- `getArmor()`: 获取护甲值
- `getShield()`: 获取护盾值
- `getCrew()`: 获取船员数量
- `getCurrentCrew()`: 获取当前船员数量
- `getFuel()`: 获取燃料
- `getMaxFuel()`: 获取最大燃料
- `getMorale()`: 获取士气
- `getCombatReadiness()`: 获取战斗准备度
- `getActiveModules()`: 获取激活的模块
- `takeDamage(damage)`: 承受伤害
- `repair(amount)`: 修理舰船
- `refuel(amount)`: 补充燃料
- `processTurn()`: 处理舰船回合
- `canMove()`: 检查是否可以移动
- `attackTarget(target)`: 攻击目标
- `calculateEvasion()`: 计算回避率
- `calculateDefense()`: 计算防御力
- `isOperational()`: 检查是否可操作
- `isDestroyed()`: 检查是否被摧毁

#### 2.4.3 ShipDesign

**作用**: 舰船设计，定义舰船的配置和属性。

**方法说明**:

- `getName()`: 获取设计名称
- `getShipClass()`: 获取舰船等级
- `getModules()`: 获取模块列表
- `getHullModule()`: 获取船体模块
- `getEngineModule()`: 获取引擎模块
- `getPowerModule()`: 获取能源模块
- `getWeaponModules()`: 获取武器模块列表
- `getDefenseModules()`: 获取防御模块列表
- `getUtilityModules()`: 获取功能模块列表
- `getTotalHitPoints()`: 获取总生命值
- `getTotalArmor()`: 获取总护甲
- `getTotalShield()`: 获取总护盾
- `getTotalCrewRequirement()`: 获取总船员需求
- `getTotalCargoCapacity()`: 获取总货舱容量
- `getTotalFuelCapacity()`: 获取总燃料容量
- `getWarpSpeed()`: 获取跃迁速度
- `getManeuverability()`: 获取机动性
- `getEnginePower()`: 获取引擎功率
- `getPowerOutput()`: 获取能源输出
- `getTotalDamage()`: 获取总伤害
- `addModule(module)`: 添加模块
- `removeModule(module)`: 移除模块
- `canAddModule(module)`: 检查是否可以添加模块
- `calculateCombatPower()`: 计算战斗力
- `calculateEvasion()`: 计算回避率
- `calculateDefense()`: 计算防御力
- `createCopy(newName)`: 创建设计副本
- `isDesignUnlocked(researchedTechs)`: 检查设计是否已解锁
- `updateDesign()`: 更新设计
- `getVersion()`: 获取设计版本
- `setVersion(version)`: 设置设计版本

#### 2.4.4 ShipModule

**作用**: 舰船模块基类，定义模块的基本属性和行为。

**方法说明**:

- `getName()`: 获取模块名称
- `getType()`: 获取模块类型
- `getConstructionCost()`: 获取建造成本
- `getMaintenanceCost()`: 获取维护成本
- `getHitPointBonus()`: 获取生命值加成
- `getArmorBonus()`: 获取护甲加成
- `getShieldBonus()`: 获取护盾加成
- `getEvasionBonus()`: 获取回避加成
- `getEnginePowerBonus()`: 获取引擎功率加成
- `getWarpSpeedBonus()`: 获取跃迁速度加成
- `getManeuverabilityBonus()`: 获取机动性加成
- `getCrewBonus()`: 获取船员加成
- `getCargoBonus()`: 获取货舱加成
- `getFuelBonus()`: 获取燃料加成
- `getSpecialAbilities()`: 获取特殊能力
- `damage(amount)`: 模块受损
- `repair(amount)`: 修理模块
- `getEffectiveness()`: 获取有效性
- `isActive()`: 检查是否激活
- `canBeUnlocked(researchedTechs)`: 检查是否可以解锁
- `createCopy()`: 创建模块副本

#### 2.4.5 WeaponModule

**作用**: 武器模块，继承自ShipModule。

**方法说明**:

- 继承ShipModule的所有方法
- `getWeaponType()`: 获取武器类型
- `getDamage()`: 获取伤害
- `getRange()`: 获取射程
- `getAccuracy()`: 获取精度
- `getFireRate()`: 获取射速
- `getPenetration()`: 获取穿透力
- `getAmmoCapacity()`: 获取弹药容量
- `getAmmoType()`: 获取弹药类型
- `getAmmoConsumption()`: 获取弹药消耗
- `getSpecialEffects()`: 获取特殊效果
- `getCriticalChance()`: 获取暴击率
- `getCriticalMultiplier()`: 获取暴击倍数

#### 2.4.6 DefenseModule

**作用**: 防御模块，继承自ShipModule。

**方法说明**:

- 继承ShipModule的所有方法
- `getDefenseType()`: 获取防御类型
- `getDefenseValue()`: 获取防御值
- `getRechargeRate()`: 获取充能速度
- `getCoverage()`: 获取覆盖范围
- `getSpecialAbilities()`: 获取特殊能力

#### 2.4.7 UtilityModule

**作用**: 功能模块，继承自ShipModule。

**方法说明**:

- 继承ShipModule的所有方法
- `getUtilityType()`: 获取功能类型
- `getUtilityValue()`: 获取功能值
- `getSpecialAbilities()`: 获取特殊能力

#### 2.4.8 EngineModule

**作用**: 引擎模块，继承自ShipModule。

**方法说明**:

- 继承ShipModule的所有方法
- `getEngineType()`: 获取引擎类型
- `getEnginePower()`: 获取引擎功率
- `getWarpSpeed()`: 获取跃迁速度
- `getFuelEfficiency()`: 获取燃料效率

#### 2.4.9 PowerModule

**作用**: 能源模块，继承自ShipModule。

**方法说明**:

- 继承ShipModule的所有方法
- `getPowerType()`: 获取能源类型
- `getPowerOutput()`: 获取能源输出
- `getPowerEfficiency()`: 获取能源效率
- `getFuelConsumption()`: 获取燃料消耗

#### 2.4.10 HullModule

**作用**: 船体模块，继承自ShipModule。

**方法说明**:

- 继承ShipModule的所有方法
- `getHullType()`: 获取船体类型
- `getBaseHitPoints()`: 获取基础生命值
- `getBaseArmor()`: 获取基础护甲
- `getCargoCapacity()`: 获取货舱容量
- `getCrewCapacity()`: 获取船员容量

### 2.5 经济系统类 (Economy Classes)

#### 2.5.1 ResourceStockpile

**作用**: 资源库存管理，管理各种资源的存储和交易。

**方法说明**:

- `getResource(type)`: 获取指定类型的资源数量
- `addResource(type, amount)`: 添加资源
- `consumeResource(type, amount)`: 消耗资源
- `transferTo(target, type, amount)`: 转移资源到目标库存
- `getCapacity(type)`: 获取资源容量
- `setCapacity(type, capacity)`: 设置资源容量
- `getUsagePercentage(type)`: 获取资源使用百分比
- `isFull(type)`: 检查资源是否已满
- `isEmpty(type)`: 检查资源是否为空
- `getAllResources()`: 获取所有资源
- `getTotalValue()`: 获取总价值
- `clear()`: 清空资源

#### 2.5.2 UniversalResourceMarket

**作用**: 宇宙资源市场，管理资源交易和价格。

**方法说明**:

- `getPrice(resourceType)`: 获取资源价格
- `buyResource(faction, resourceType, amount)`: 购买资源
- `sellResource(faction, resourceType, amount)`: 出售资源
- `getTransactionHistory()`: 获取交易历史
- `updatePrices()`: 更新价格
- `getAvailableResources()`: 获取可用资源

### 2.6 科技系统类 (Technology Classes)

#### 2.6.1 TechTree

**作用**: 科技树，管理所有科技及其研究状态。

**方法说明**:

- `addTechnology(technology)`: 添加科技
- `getTechnology(id)`: 获取科技
- `isTechnologyResearched(id)`: 检查科技是否已研究
- `canResearch(technology)`: 检查是否可以研究科技
- `startResearch(technology)`: 开始研究科技
- `processResearch(baseResearchPoints)`: 处理研究进度
- `addToQueue(technology)`: 添加到研究队列
- `removeFromQueue(project)`: 从队列中移除
- `getAvailableTechnologies()`: 获取可研究的科技
- `getResearchedTechnologies()`: 获取已研究的科技
- `getTechnologiesByCategory(category)`: 按类别获取科技
- `getResearchProgressPercentage()`: 获取研究进度百分比
- `getCurrentResearch()`: 获取当前研究
- `getResearchStatus()`: 获取研究状态
- `getRemainingRoundsForCurrentResearch()`: 获取当前研究剩余回合
- `getRemainingRoundsForTechnology(technology)`: 获取指定科技剩余回合
- `addResearchCompletedListener(listener)`: 添加研究完成监听器
- `removeResearchCompletedListener(listener)`: 移除研究完成监听器

#### 2.6.2 Technology

**作用**: 科技类，定义单个科技的属性和效果。

**方法说明**:

- `getId()`: 获取科技ID
- `getName()`: 获取科技名称
- `getDescription()`: 获取科技描述
- `getCategory()`: 获取科技类别
- `getResearchCost()`: 获取研究成本
- `isResearched()`: 检查是否已研究
- `setResearched(researched)`: 设置研究状态
- `getPrerequisites()`: 获取前置科技
- `addPrerequisite(prerequisite)`: 添加前置科技
- `getUnlockedBuildings()`: 获取解锁的建筑
- `addUnlockedBuilding(buildingId)`: 添加解锁的建筑
- `getUnlockedUnits()`: 获取解锁的单位
- `addUnlockedUnit(unitId)`: 添加解锁的单位
- `getUnlockedResources()`: 获取解锁的资源
- `addUnlockedResource(resourceId)`: 添加解锁的资源
- `getGrantedAbilities()`: 获取授予的能力
- `addGrantedAbility(ability)`: 添加授予的能力
- `getEffects()`: 获取科技效果
- `addEffect(effect)`: 添加科技效果
- `getUnlocks()`: 获取解锁内容
- `addUnlock(unlockable)`: 添加解锁内容
- `isRepeatable()`: 检查是否可重复研究
- `getMaxResearchLevel()`: 获取最大研究等级
- `getCurrentLevel()`: 获取当前研究等级
- `setResearchCost(cost)`: 设置研究成本

#### 2.6.3 ResearchProject

**作用**: 研究项目，表示正在进行的研究。

**方法说明**:

- `getTechnology()`: 获取研究的科技
- `getProgress()`: 获取研究进度
- `getTotalCost()`: 获取总成本
- `progress(points)`: 推进研究
- `getProgressPercentage()`: 获取进度百分比
- `isCompleted()`: 检查是否完成
- `getRemainingCost()`: 获取剩余成本

#### 2.6.4 TechnologyEffect

**作用**: 科技效果，定义科技对游戏系统的影响。

**方法说明**:

- `getEffectType()`: 获取效果类型
- `getEffectScope()`: 获取作用范围
- `getTargetId()`: 获取目标ID
- `getTargetCategory()`: 获取目标类别
- `getValue()`: 获取效果值
- `isPercentage()`: 检查是否为百分比效果
- `getDescription()`: 获取效果描述
- `getPriority()`: 获取优先级
- `isStackable()`: 检查是否可叠加
- `applyEffect(target)`: 应用效果
- `calculateFinalValue(baseValue)`: 计算最终值

### 2.7 外交系统类 (Diplomacy Classes)

#### 2.7.1 DiplomacyManager

**作用**: 外交管理器，管理派系间的外交关系。

**方法说明**:

- `setRelationship(faction1, faction2, status)`: 设置两个派系的关系
- `getRelationship(faction)`: 获取与指定派系的关系
- `getRelationshipStatus(faction)`: 获取与指定派系的关系状态
- `changeRelationship(faction, change)`: 改变与指定派系的关系
- `declareWar(faction)`: 向指定派系宣战
- `proposePeace(faction)`: 向指定派系提议和平
- `formAlliance(faction)`: 与指定派系结盟
- `breakAlliance(faction)`: 与指定派系断盟
- `getDiplomaticHistory()`: 获取外交历史

#### 2.7.2 DiplomaticRelationship

**作用**: 外交关系，表示两个派系间的关系状态。

**方法说明**:

- `getStatus()`: 获取关系状态
- `setStatus(status)`: 设置关系状态
- `getTrustLevel()`: 获取信任等级
- `setTrustLevel(level)`: 设置信任等级
- `getTreaties()`: 获取条约列表
- `addTreaty(treaty)`: 添加条约
- `removeTreaty(treaty)`: 移除条约
- `getLastInteraction()`: 获取最后互动时间
- `setLastInteraction(time)`: 设置最后互动时间

### 2.8 胜利条件类 (Victory Classes)

#### 2.8.1 VictoryConditionManager

**作用**: 胜利条件管理器，检查游戏胜利条件。

**方法说明**:

- `checkCompleteVictory(faction, techTree)`: 检查完全胜利条件
- `checkControlRate(faction)`: 检查控制率
- `checkTechCompletion(techTree)`: 检查科技完成度
- `checkEconomicVictory(faction)`: 检查经济胜利条件
- `checkDiplomaticVictory(faction)`: 检查外交胜利条件

## 3. 服务类 (Service Classes)

### 3.1 事件系统类 (Event System Classes)

#### 3.1.1 EventBus

**作用**: 事件总线，管理游戏事件的发布和订阅。

**方法说明**:

- `publish(event)`: 发布事件
- `register(listener)`: 注册监听器
- `unregister(listener)`: 注销监听器
- `subscribe(eventType, handler)`: 订阅特定事件类型
- `unsubscribe(eventType, handler)`: 取消订阅特定事件类型

#### 3.1.2 GameEvent

**作用**: 游戏事件基类，表示游戏中发生的事件。

**方法说明**:

- `getType()`: 获取事件类型
- `getData()`: 获取事件数据
- `getTimestamp()`: 获取时间戳
- `getSource()`: 获取事件源

#### 3.1.3 GameEventListener

**作用**: 游戏事件监听器接口，处理游戏事件。

**方法说明**:

- `onEvent(event)`: 处理事件

### 3.2 AI 服务类 (AI Service Classes)

#### 3.2.1 AIController

**作用**: AI控制器，管理AI派系的决策。

**方法说明**:

- `makeDecision()`: AI决策
- `makeDecision(gameState)`: 基于游戏状态的AI决策
- `evaluateResourceSituation()`: 评估资源情况
- `evaluateResearchOptions()`: 评估研究选项
- `evaluateExpansionOpportunities()`: 评估扩张机会
- `evaluateMilitarySituation()`: 评估军事情况

## 4. 视图类 (View Classes)

### 4.1 UI 组件类 (UI Component Classes)

#### 4.1.1 HexMapView

**作用**: 六边形地图视图，显示银河系地图和交互。

**方法说明**:

- `setHexGrid(hexGrid)`: 设置六边形网格
- `setGalaxy(galaxy)`: 设置银河系
- `setPlayerFaction(faction)`: 设置玩家派系
- `setPlayerStartHex(hex)`: 设置玩家起始六边形
- `getSelectedHex()`: 获取选中的六边形
- `setSelectedHex(hex)`: 设置选中的六边形
- `setSelectedFleet(fleet)`: 设置选中的舰队
- `getSelectedFleet()`: 获取选中的舰队
- `highlightHex(hex, color)`: 高亮六边形
- `clearHighlights()`: 清除高亮
- `centerOnHex(hex)`: 将指定六边形居中显示
- `getReachableHexes(startHex, range)`: 获取可到达的六边形
- `calculateFleetMoveRange(fleet)`: 计算舰队移动范围
- `draw()`: 绘制地图
- `handleMouseClick(event)`: 处理鼠标点击
- `handleMousePressed(event)`: 处理鼠标按下
- `handleMouseDragged(event)`: 处理鼠标拖拽
- `handleMouseReleased(event)`: 处理鼠标释放
- `handleMouseMoved(event)`: 处理鼠标移动
- `handleScroll(event)`: 处理滚轮事件
- `handleKeyPress(event)`: 处理按键事件

#### 4.1.2 TechTreeUI

**作用**: 科技树用户界面，显示和管理科技研究。

**方法说明**:

- `setTechTree(techTree)`: 设置科技树
- `show()`: 显示科技树界面
- `updateTechnologyDetails()`: 更新科技详情
- `startResearch(technology)`: 开始研究科技
- `addToQueue(technology)`: 添加到研究队列
- `removeFromQueue(project)`: 从队列中移除
- `moveSelectedUp()`: 将选中项目上移
- `moveSelectedDown()`: 将选中项目下移
- `showAllCategories()`: 显示所有类别
- `toggleCategory(category)`: 切换类别显示
- `buildTechTree()`: 构建科技树视图
- `drawConnections()`: 绘制连接线
- `updateCanvasSize(width, height)`: 更新画布大小
- `setupEventHandlers()`: 设置事件处理器

#### 4.1.3 ColonyManagerView

**作用**: 殖民地管理视图，管理殖民地建筑和资源。

**方法说明**:

- `setColony(colony)`: 设置殖民地
- `updateColonyInfo()`: 更新殖民地信息
- `showBuildingOptions()`: 显示建筑选项
- `buildBuilding(buildingType)`: 建造建筑
- `upgradeBuilding(building)`: 升级建筑
- `showGovernorOptions()`: 显示总督选项
- `assignGovernor(governor)`: 分配总督
- `setGrowthFocus(focus)`: 设置发展重点
- `updateResourceDisplay()`: 更新资源显示
- `updateBuildingList()`: 更新建筑列表
- `updateProductionInfo()`: 更新产出信息

#### 4.1.4 FleetManagerUI

**作用**: 舰队管理界面，管理舰队和舰船。

**方法说明**:

- `setFaction(faction)`: 设置派系
- `updateFleetList()`: 更新舰队列表
- `showFleetDetails(fleet)`: 显示舰队详情
- `createNewFleet()`: 创建新舰队
- `mergeFleets(fleet1, fleet2)`: 合并舰队
- `splitFleet(fleet)`: 分离舰队
- `transferShip(ship, fromFleet, toFleet)`: 转移舰船
- `setHexMapView(mapView)`: 设置地图视图
- `showMissionOptions(fleet)`: 显示任务选项
- `assignMission(fleet, mission, target)`: 分配任务

### 4.2 UI 控制器类 (UI Controller Classes)

#### 4.2.1 MainController

**作用**: 主控制器，管理游戏主界面和核心功能。

**方法说明**:

- `setGameEngine(engine)`: 设置游戏引擎
- `updateTurnDisplay()`: 更新回合显示
- `updateResourceDisplay()`: 更新资源显示
- `onHexSelected(hex)`: 处理六边形选择
- `onFleetSelected(fleet)`: 处理舰队选择
- `showStarSystemInfo(system)`: 显示星系信息
- `showPlanetInfo(planet)`: 显示行星信息
- `showFleetInfo(fleet)`: 显示舰队信息
- `showColonyInfo(colony)`: 显示殖民地信息
- `showInfoDialog(title, content)`: 显示信息对话框
- `showAlert(title, message)`: 显示警告
- `showComponentInWindow(component, title)`: 在窗口中显示组件
- `onCenterViewClicked()`: 处理居中视图点击

## 5. 工具类 (Utility Classes)

### 5.1 IO 工具类 (IO Utility Classes)

#### 5.1.1 SaveManager

**作用**: 存档管理器，处理游戏存档和读档。

**方法说明**:

- `save(gameEngine, fileName)`: 保存游戏
- `load(fileName)`: 加载游戏
- `autoSave(gameEngine)`: 自动保存
- `getSaveList()`: 获取存档列表
- `deleteSave(fileName)`: 删除存档
- `validateSaveFile(fileName)`: 验证存档文件
- `createSaveDirectory()`: 创建存档目录

### 5.2 生成器类 (Generator Classes)

#### 5.2.1 NameGenerator

**作用**: 名称生成器，生成星系、行星等的名称。

**方法说明**:

- `generateStarSystemName()`: 生成星系名称
- `generatePlanetName(systemName)`: 生成行星名称
- `generateFleetName()`: 生成舰队名称
- `generateShipName()`: 生成舰船名称
- `generateColonyName()`: 生成殖民地名称
- `generateGovernorName()`: 生成总督名称