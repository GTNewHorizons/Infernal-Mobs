package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Sapper extends MobModifier {

    private static final String[] suffix = { "ofHunger", "thePaleRider" };
    private static final String[] prefix = { "hungering", "starving" };
    private static int potionDuration;

    public MM_Sapper(@Nullable MobModifier next) {
        super("Sapper", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
            && InfernalMobsCore.instance()
                .getIsEntityAllowedTarget(source.getEntity())) {
            EntityLivingBase ent = (EntityLivingBase) source.getEntity();
            if (!ent.isPotionActive(Potion.hunger)) {
                ent.addPotionEffect(new PotionEffect(Potion.hunger.id, potionDuration, 0));
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null && InfernalMobsCore.instance()
            .getIsEntityAllowedTarget(entity) && !entity.isPotionActive(Potion.poison)) {
            entity.addPotionEffect(new PotionEffect(Potion.hunger.id, potionDuration, 0));
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

    public static class Loader extends ModifierLoader<MM_Sapper> {

        public Loader() {
            super(MM_Sapper.class);
        }

        @Override
        public MM_Sapper make(@Nullable MobModifier next) {
            return new MM_Sapper(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            potionDuration = config
                .get(getModifierClassName(), "hungerDurationTicks", 120L, "Time attacker is hungering")
                .getInt(120);
        }
    }
}
