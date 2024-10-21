package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Vengeance extends MobModifier {

    private static final String[] suffix = { "ofRetribution", "theThorned", "ofStrikingBack" };
    private static final String[] prefix = { "thorned", "thorny", "spiky" };
    private static float reflectMultiplier;
    private static float maxReflectDamage;

    public MM_Vengeance(@Nullable MobModifier next) {
        super("Vengeance", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && source.getEntity() != mob
            && !InfernalMobsCore.instance()
                .isInfiniteLoop(mob, source.getEntity())) {
            float reflectedDamage = Math.max(Math.min(damage, mob.getHealth()) * reflectMultiplier, 1);
            if (maxReflectDamage > 0.0f) {
                reflectedDamage = Math.min(maxReflectDamage, reflectedDamage);
            }
            source.getEntity()
                .attackEntityFrom(DamageSource.causeThornsDamage(mob), reflectedDamage);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Vengeance> {

        public Loader() {
            super(MM_Vengeance.class);
        }

        @Override
        public MM_Vengeance make(@Nullable MobModifier next) {
            return new MM_Vengeance(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            reflectMultiplier = (float) config
                .get(
                    getModifierClassName(),
                    "vengeanceMultiplier",
                    0.5D,
                    "Multiplies damage received, result is subtracted from attacking entity's health")
                .getDouble(0.5D);
            maxReflectDamage = (float) config
                .get(
                    getModifierClassName(),
                    "vengeanceMaxDamage",
                    0.0D,
                    "Maximum amount of damage that is reflected (0, or less than zero for unlimited vengeance damage)")
                .getDouble(0.0D);
        }
    }
}
