package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Blastoff extends MobModifier {

    private static final Class<?>[] modBans = { MM_Webber.class };
    private static final String[] suffix = { "ofMissionControl", "theNASA", "ofWEE" };
    private static final String[] prefix = { "thumping", "trolling", "byebye" };
    private static long coolDown;
    private long nextAbilityUse = 0L;

    public MM_Blastoff(@Nullable MobModifier next) {
        super("Blastoff", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        EntityLivingBase target = getMobTarget();

        if (target instanceof EntityPlayer && !((EntityPlayer) target).capabilities.disableDamage) {
            tryAbility(mob, target);
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && source.getEntity() instanceof EntityLivingBase
            && !(source.getEntity() instanceof EntityPlayer
                && ((EntityPlayer) source.getEntity()).capabilities.disableDamage)) {
            tryAbility(mob, (EntityLivingBase) source.getEntity());
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        long time = mob.ticksExisted;
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            mob.worldObj.playSoundAtEntity(
                mob,
                "mob.slime.attack",
                1.0F,
                (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);

            if (target.worldObj.isRemote || !(target instanceof EntityPlayerMP)) {
                target.addVelocity(0, 1.1D, 0);
            } else {
                InfernalMobsCore.instance()
                    .sendVelocityPacket((EntityPlayerMP) target, 0f, 1.1f, 0f);
            }
        }
    }

    @Override
    public Class<?>[] getModsNotToMixWith() {
        return modBans;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Blastoff> {

        public Loader() {
            super(MM_Blastoff.class);
        }

        @Override
        public MM_Blastoff make(@Nullable MobModifier next) {
            return new MM_Blastoff(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 15000L, "Time between ability uses")
                .getInt(15000) / 50;
        }
    }
}
