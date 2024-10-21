package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Gravity extends MobModifier {

    private static final Class<?>[] modBans = { MM_Webber.class };
    private static final String[] suffix = { "ofRepulsion", "theFlipper" };
    private static final String[] prefix = { "repulsing", "sproing" };
    private static long coolDown;
    private static double maxDistanceSquared;
    private long nextAbilityUse = 0L;

    public MM_Gravity(@Nullable MobModifier next) {
        super("Gravity", next);
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
        if (target == null || !mob.canEntityBeSeen(target) || mob.getDistanceSqToEntity(target) >= maxDistanceSquared) {
            return;
        }

        long time = mob.ticksExisted;
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;

            double diffX = target.posX - mob.posX;
            double diffZ;
            for (diffZ = target.posZ - mob.posZ; diffX * diffX + diffZ * diffZ
                < 1.0E-4D; diffZ = (Math.random() - Math.random()) * 0.01D) {
                diffX = (Math.random() - Math.random()) * 0.01D;
            }

            mob.worldObj.playSoundAtEntity(
                mob,
                "mob.irongolem.throw",
                1.0F,
                (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);

            if (mob.worldObj.isRemote || !(target instanceof EntityPlayerMP)) {
                knockBack(target, diffX, diffZ);
            } else {
                InfernalMobsCore.instance()
                    .sendKnockBackPacket((EntityPlayerMP) target, (float) diffX, (float) diffZ);
            }
        }
    }

    public static void knockBack(EntityLivingBase target, double x, double z) {
        target.isAirBorne = true;
        float normalizedPower = MathHelper.sqrt_double(x * x + z * z);
        float knockPower = 0.8F;
        target.motionX /= 2.0D;
        target.motionY /= 2.0D;
        target.motionZ /= 2.0D;
        target.motionX -= x / (double) normalizedPower * (double) knockPower;
        target.motionY += knockPower;
        target.motionZ -= z / (double) normalizedPower * (double) knockPower;

        if (target.motionY > 0.4000000059604645D) {
            target.motionY = 0.4000000059604645D;
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

    public static class Loader extends ModifierLoader<MM_Gravity> {

        public Loader() {
            super(MM_Gravity.class);
        }

        @Override
        public MM_Gravity make(@Nullable MobModifier next) {
            return new MM_Gravity(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 5000L, "Time between ability uses")
                .getInt(5000) / 50;
            double maxDistance = config.get(getModifierClassName(), "maxDistance", 40, "Range of ability.")
                .getDouble(40);
            maxDistanceSquared = maxDistance * maxDistance;
        }
    }
}
