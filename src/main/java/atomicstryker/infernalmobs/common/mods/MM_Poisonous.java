package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Poisonous extends MobModifier {

    private static int potionDuration;

    public MM_Poisonous(@Nullable MobModifier next) {
        super("Poisonous", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getEntity())) {
            EntityLivingBase ent = (EntityLivingBase) source.getEntity();
            if (!ent.isPotionActive(Potion.poison) && !(source instanceof EntityDamageSourceIndirect)
                    && !source.isProjectile()) {
                ent.addPotionEffect(new PotionEffect(Potion.poison.id, potionDuration, 0));
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity)
                && !entity.isPotionActive(Potion.poison)) {
            entity.addPotionEffect(new PotionEffect(Potion.poison.id, potionDuration, 0));
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "ofVenom", "thedeadlyChalice" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "poisonous", "stinging", "despoiling" };

    public static class Loader extends ModifierLoader<MM_Poisonous> {

        public Loader() {
            super(MM_Poisonous.class);
        }

        @Override
        public MM_Poisonous make(@Nullable MobModifier next) {
            return new MM_Poisonous(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            potionDuration = config
                    .get(getModifierClassName(), "poisonDurationTicks", 120L, "Time attacker is poisoned").getInt(120);
        }
    }
}
