package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Lifesteal extends MobModifier {

    private static float lifestealMultiplier;

    public MM_Lifesteal(@Nullable MobModifier next) {
        super("Lifesteal", next);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        EntityLivingBase mob = (EntityLivingBase) source.getEntity();
        if (entity != null && mob.getHealth() < getActualMaxHealth(mob)) {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth() + (damage * lifestealMultiplier));
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
    }

    private static Class<?>[] disallowed = { EntityCreeper.class };

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "theVampire", "ofTransfusion", "theBloodsucker" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "vampiric", "transfusing", "bloodsucking" };

    public static class Loader extends ModifierLoader<MM_Lifesteal> {

        public Loader() {
            super(MM_Lifesteal.class);
        }

        @Override
        public MM_Lifesteal make(@Nullable MobModifier next) {
            return new MM_Lifesteal(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            lifestealMultiplier = (float) config.get(
                    getModifierClassName(),
                    "lifestealMultiplier",
                    1.0D,
                    "Multiplies damage dealt, result is added to mob health").getDouble(1.0D);
        }
    }
}
