package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Regen extends MobModifier {

    private static Class<?>[] disallowed = {};
    private static final String[] suffix = { "ofWTFIMBA", "theCancerous", "ofFirstAid" };
    private static final String[] prefix = { "regenerating", "healing", "nighunkillable" };
    private static long coolDown;
    private long nextAbilityUse = 0L;

    public MM_Regen(@Nullable MobModifier next) {
        super("Regen", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (mob.getHealth() < getActualMaxHealth(mob)) {
            long time = InfernalMobsCore.instance()
                .getCooldownTime(mob);
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                InfernalMobsCore.instance()
                    .setEntityHealthPastMax(mob, mob.getHealth() + 1);
            }
        }
        return super.onUpdate(mob);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
    }

    public static class Loader extends ModifierLoader<MM_Regen> {

        public Loader() {
            super(MM_Regen.class, emptyString);
        }

        @Override
        public MM_Regen make(@Nullable MobModifier next) {
            return new MM_Regen(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            super.loadConfig(config);
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 500L, "Time between ability uses")
                .getInt(500)
                / InfernalMobsCore.instance()
                    .getOldIFFactor();

            disallowed = getBannedClassesToArray();
        }
    }
}
