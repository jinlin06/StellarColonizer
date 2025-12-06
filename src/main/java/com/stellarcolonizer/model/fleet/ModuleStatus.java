package com.stellarcolonizer.model.fleet;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;

// 模块状态类
class ModuleStatus {
    private final ShipModule module;
    private final FloatProperty integrity; // 0-100%
    private final BooleanProperty isActive;
    private final BooleanProperty isDestroyed;

    public ModuleStatus(ShipModule module) {
        this.module = module;
        this.integrity = new SimpleFloatProperty(100.0f);
        this.isActive = new SimpleBooleanProperty(true);
        this.isDestroyed = new SimpleBooleanProperty(false);
    }

    public void damage(float amount) {
        integrity.set(integrity.get() - amount);

        if (integrity.get() <= 0) {
            integrity.set(0);
            isActive.set(false);
            isDestroyed.set(true);
        } else if (integrity.get() < 30) {
            isActive.set(false); // 严重损伤时失效
        }
    }

    public void repair(float amount) {
        integrity.set(Math.min(100, integrity.get() + amount));

        if (integrity.get() > 30 && !isActive.get()) {
            isActive.set(true);
        }

        if (isDestroyed.get() && integrity.get() > 50) {
            isDestroyed.set(false);
        }
    }

    public float getEffectiveness() {
        if (!isActive.get() || isDestroyed.get()) return 0;
        return integrity.get() / 100.0f;
    }

    // Getter 方法
    public ShipModule getModule() { return module; }
    public float getIntegrity() { return integrity.get(); }
    public FloatProperty integrityProperty() { return integrity; }
    public boolean isActive() { return isActive.get(); }
    public BooleanProperty activeProperty() { return isActive; }
    public boolean isDestroyed() { return isDestroyed.get(); }
    public BooleanProperty destroyedProperty() { return isDestroyed; }
}
