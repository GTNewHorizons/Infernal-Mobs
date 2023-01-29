package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Exhaust extends MobModifier {

    public MM_Exhaust(@Nullable MobModifier next) {
        super("Exhaust", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityPlayer)) {
            ((EntityPlayer) source.getEntity()).addExhaustion(1F);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null && entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).addExhaustion(1F);
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "ofFatigue", "theDrainer" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "exhausting", "draining" };

    public static class Loader extends ModifierLoader<MM_Exhaust> {

        public Loader() {
            super(MM_Exhaust.class);
        }

        @Override
        public MM_Exhaust make(@Nullable MobModifier next) {
            return new MM_Exhaust(next);
        }
    }
}
