package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

public class MM_Berserk extends MobModifier {

    private static final Class<?>[] disallowed = { EntityCreeper.class };
    private static final String[] suffix = { "ofRecklessness", "theRaging", "ofSmashing" };
    private static final String[] prefix = { "reckless", "raging", "smashing" };
    private static float damageMultiplier;
    private static float maxBerserkDamage;

    public MM_Berserk(@Nullable MobModifier next) {
        super("Berserk", next);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null) {
            source.getEntity()
                .attackEntityFrom(DamageSource.generic, damage);
            damage = Math.min(maxBerserkDamage, damage * damageMultiplier);
        }

        return super.onAttack(entity, source, damage);
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

    public static class Loader extends ModifierLoader<MM_Berserk> {

        public Loader() {
            super(MM_Berserk.class);
        }

        @Override
        public MM_Berserk make(@Nullable MobModifier next) {
            return new MM_Berserk(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            damageMultiplier = (float) config
                .get(getModifierClassName(), "damageMultiplier", 2.0D, "Damage multiplier, limited by maxOneShotDamage")
                .getDouble(2.0D);
            maxBerserkDamage = (float) config.get(
                getModifierClassName(),
                "berserkMaxDamage",
                0.0D,
                "Maximum amount of damage that a mob with Berserk can deal (0, or less than zero for unlimited berserk damage)")
                .getDouble(0.0D);
        }
    }
}
