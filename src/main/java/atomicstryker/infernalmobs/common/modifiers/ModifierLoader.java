package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraftforge.common.config.Configuration;

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
