package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Ghastly extends MobModifier {

    private long nextAbilityUse = 0L;
    private static long coolDown;
    private final static float MIN_DISTANCE = 3F;

    public MM_Ghastly(@Nullable MobModifier next) {
        super("Ghastly", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        long time = mob.ticksExisted;
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            tryAbility(mob, getMobTarget());
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        if (mob.getDistanceToEntity(target) > MIN_DISTANCE) {
            double diffX = target.posX - mob.posX;
            double diffY = target.boundingBox.minY + (double) (target.height / 2.0F)
                    - (mob.posY + (double) (mob.height / 2.0F));
            double diffZ = target.posZ - mob.posZ;
            mob.renderYawOffset = mob.rotationYaw = -((float) Math.atan2(diffX, diffZ)) * 180.0F / (float) Math.PI;

            mob.worldObj
                    .playAuxSFXAtEntity((EntityPlayer) null, 1008, (int) mob.posX, (int) mob.posY, (int) mob.posZ, 0);
            EntityLargeFireball entFB = new EntityLargeFireball(mob.worldObj, mob, diffX, diffY, diffZ);
            double spawnOffset = 2.0D;
            Vec3 mobLook = mob.getLook(1.0F);
            entFB.posX = mob.posX + mobLook.xCoord * spawnOffset;
            entFB.posY = mob.posY + (double) (mob.height / 2.0F) + 0.5D;
            entFB.posZ = mob.posZ + mobLook.zCoord * spawnOffset;
            mob.worldObj.spawnEntityInWorld(entFB);
        }
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "OMFGFIREBALLS", "theBomber", "ofBallsofFire" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "bombing", "fireballsy" };

    public static class Loader extends ModifierLoader<MM_Ghastly> {

        public Loader() {
            super(MM_Ghastly.class);
        }

        @Override
        public MM_Ghastly make(@Nullable MobModifier next) {
            return new MM_Ghastly(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 6000L, "Time between ability uses")
                    .getInt(6000) / 50;
        }
    }
}
