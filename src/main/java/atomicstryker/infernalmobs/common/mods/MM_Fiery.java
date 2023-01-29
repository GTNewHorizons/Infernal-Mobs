package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Fiery extends MobModifier {

    private static int fireDuration;

    public MM_Fiery(@Nullable MobModifier next) {
        super("Fiery", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
                && !(source instanceof EntityDamageSourceIndirect)
                && !source.isProjectile()) {
            ((EntityLivingBase) source.getEntity()).setFire(fireDuration);
        }

        mob.extinguish();
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null) {
            entity.setFire(fireDuration);
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "ofConflagration", "thePhoenix", "ofCrispyness" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "burning", "toasting" };

    public static class Loader extends ModifierLoader<MM_Fiery> {

        public Loader() {
            super(MM_Fiery.class);
        }

        @Override
        public MM_Fiery make(@Nullable MobModifier next) {
            return new MM_Fiery(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            fireDuration = config.get(getModifierClassName(), "fieryDurationSecs", 3L, "Time attacker is set on fire")
                    .getInt(3);
        }
    }
}
