package atomicstryker.infernalmobs.common.mods.api;

import javax.annotation.Nullable;

import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.MobModifier;

public abstract class ModifierLoader<T extends MobModifier> {

    private final String modifierClassName;

    protected ModifierLoader(Class<T> modifierClass) {
        this.modifierClassName = modifierClass.getSimpleName();
    }

    public abstract T make(@Nullable MobModifier next);

    public void loadConfig(Configuration config) {

    }

    public String getModifierClassName() {
        return modifierClassName;
    }
}
