package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Regen extends MobModifier {

    private long nextAbilityUse = 0L;
    private static long coolDown;

    public MM_Regen(@Nullable MobModifier next) {
        super("Regen", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (mob.getHealth() < getActualMaxHealth(mob)) {
            long time = mob.ticksExisted;
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth() + 1);
            }
        }
        return super.onUpdate(mob);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "ofWTFIMBA", "theCancerous", "ofFirstAid" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "regenerating", "healing", "nighunkillable" };

    public static class Loader extends ModifierLoader<MM_Regen> {

        public Loader() {
            super(MM_Regen.class);
        }

        @Override
        public MM_Regen make(@Nullable MobModifier next) {
            return new MM_Regen(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 500L, "Time between ability uses")
                    .getInt(500) / 50;
        }
    }
}
