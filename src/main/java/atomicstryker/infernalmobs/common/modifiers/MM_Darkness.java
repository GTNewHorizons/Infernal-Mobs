package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Darkness extends MobModifier {

    private static final String[] suffix = { "ofDarkness", "theShadow", "theEclipse" };
    private static final String[] prefix = { "dark", "shadowkin", "eclipsed" };
    private static int potionDuration;

    public MM_Darkness(@Nullable MobModifier next) {
        super("Darkness", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
            && InfernalMobsCore.instance()
                .getIsEntityAllowedTarget(source.getEntity())
            && !(source instanceof EntityDamageSourceIndirect)
            && !source.isProjectile()) {
            ((EntityLivingBase) source.getEntity())
                .addPotionEffect(new PotionEffect(Potion.blindness.id, potionDuration, 0));
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null && InfernalMobsCore.instance()
            .getIsEntityAllowedTarget(entity)) {
            entity.addPotionEffect(new PotionEffect(Potion.blindness.id, potionDuration, 0));
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Darkness> {

        public Loader() {
            super(MM_Darkness.class);
        }

        @Override
        public MM_Darkness make(@Nullable MobModifier next) {
            return new MM_Darkness(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            potionDuration = config
                .get(getModifierClassName(), "darknessDurationTicks", 120L, "Time attacker is darkened")
                .getInt(120);
        }
    }
}
