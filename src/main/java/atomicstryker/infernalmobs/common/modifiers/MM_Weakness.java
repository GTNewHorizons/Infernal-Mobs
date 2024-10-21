package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Weakness extends MobModifier {

    private static final String[] suffix = { "ofApathy", "theDeceiver" };
    private static final String[] prefix = { "apathetic", "deceiving" };
    private static int potionDuration;

    public MM_Weakness(@Nullable MobModifier next) {
        super("Weakness", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
            && InfernalMobsCore.instance()
                .getIsEntityAllowedTarget(source.getEntity())) {
            ((EntityLivingBase) source.getEntity())
                .addPotionEffect(new PotionEffect(Potion.weakness.id, potionDuration, 0));
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null && InfernalMobsCore.instance()
            .getIsEntityAllowedTarget(entity)) {
            entity.addPotionEffect(new PotionEffect(Potion.weakness.id, potionDuration, 0));
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

    public static class Loader extends ModifierLoader<MM_Weakness> {

        public Loader() {
            super(MM_Weakness.class);
        }

        @Override
        public MM_Weakness make(@Nullable MobModifier next) {
            return new MM_Weakness(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            potionDuration = config
                .get(getModifierClassName(), "weaknessDurationTicks", 120L, "Time attacker is weakened")
                .getInt(120);
        }
    }
}
