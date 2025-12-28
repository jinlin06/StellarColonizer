package com.stellarcolonizer.model.fleet;


import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;

// 船体模块
public class HullModule extends ShipModule {

    public HullModule(int hullSize) {
        super("基础船体", ModuleType.HULL, 0, 0); // 默认船体模块不占用自身空间
        this.size.set(hullSize); // 但实际船体大小由参数设置
    }

    // 用于复制模块的构造函数
    public HullModule(String name, ModuleType type, int size, int powerRequirement) {
        super(name, type, size, powerRequirement);
    }

    @Override
    protected void initializeCosts() {
        constructionCost.put(ResourceType.METAL, size.get() * 0.8f);
        
        // 只有大型船体（航母、无畏舰等，假设大小超过500）才需要稀有资源
        if (size.get() > 500) {
            constructionCost.put(ResourceType.EXOTIC_MATTER, size.get() * 0.01f);
            constructionCost.put(ResourceType.DARK_MATTER, size.get() * 0.005f);
        }
    }

    @Override
    public float getHitPointBonus() {
        return size.get() * 2.0f; // 每单位船体提供2点生命
    }

    @Override
    public float getArmorBonus() {
        return size.get() * 0.1f; // 每单位船体提供0.1点装甲
    }

    @Override
    public float getShieldBonus() {
        return 0;
    }

    @Override
    public float getEvasionBonus() {
        return -size.get() * 0.01f; // 船体越大，回避越低
    }

    @Override
    public float getEnginePowerBonus() {
        return 0;
    }

    @Override
    public float getWarpSpeedBonus() {
        return 0;
    }

    @Override
    public float getManeuverabilityBonus() {
        return -size.get() * 0.02f; // 船体越大，机动性越差
    }

    @Override
    public int getCrewBonus() {
        return size.get() / 20; // 每20单位船体提供1名船员容量
    }

    @Override
    public int getCargoBonus() {
        return size.get() / 10; // 每10单位船体提供1单位货舱
    }

    @Override
    public int getFuelBonus() {
        return size.get() / 50; // 每50单位船体提供1单位燃料
    }
}