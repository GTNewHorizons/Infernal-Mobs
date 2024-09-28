package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

public class MM_Cloaking extends MobModifier {

    private static final Class<?>[] disallowed = { EntitySpider.class };
    private static final String[] suffix = { "ofStalking", "theUnseen", "thePredator" };
    private static final String[] prefix = { "stalking", "unseen", "hunting" };
    private static long coolDown;
    private static int potionDuration;
    private long nextAbilityUse = 0L;

    public MM_Cloaking(@Nullable MobModifier next) {
        super("Cloaking", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (getMobTarget() != null && getMobTarget() instanceof EntityPlayer) {
            tryAbility(mob);
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && source.getEntity() instanceof EntityLivingBase) {
            tryAbility(mob);
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob) {
        long time = mob.ticksExisted;
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            mob.addPotionEffect(new PotionEffect(Potion.invisibility.id, potionDuration));
        }
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

    public static class Loader extends ModifierLoader<MM_Cloaking> {

        public Loader() {
            super(MM_Cloaking.class);
        }

        @Override
        public MM_Cloaking make(@Nullable MobModifier next) {
            return new MM_Cloaking(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            potionDuration = config.get(getModifierClassName(), "cloakingDurationTicks", 200L, "Time mob is cloaked")
                .getInt(200);
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 12000L, "Time between ability uses")
                .getInt(12000) / 50;
        }
    }
}
