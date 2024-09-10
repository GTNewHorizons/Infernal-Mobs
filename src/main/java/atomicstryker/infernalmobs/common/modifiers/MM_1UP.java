package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_1UP extends MobModifier {

    private static final Class<?>[] disallowed = { EntityCreeper.class };
    private static final String[] suffix = { "ofRecurrence", "theUndying", "oftwinLives" };
    private static final String[] prefix = { "recurring", "undying", "twinlived" };
    private static double healAmount;
    private boolean healed = false;

    public MM_1UP(@Nullable MobModifier next) {
        super("1UP", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (!healed && mob.getHealth() < (getActualMaxHealth(mob) * 0.25)) {
            InfernalMobsCore.instance()
                .setEntityHealthPastMax(mob, getActualMaxHealth(mob) * (float) healAmount);
            mob.worldObj.playSoundAtEntity(mob, "random.levelup", 1.0F, 1.0F);
            healed = true;
        }
        return super.onUpdate(mob);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_1UP> {

        public Loader() {
            super(MM_1UP.class);
        }

        @Override
        public MM_1UP make(@Nullable MobModifier next) {
            return new MM_1UP(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            healAmount = config
                .get(
                    getModifierClassName(),
                    "healAmountMultiplier",
                    1.0D,
                    "Multiplies the mob maximum health when healing back up, cannot get past maximum mob health")
                .getDouble(1.0D);
        }
    }
}
