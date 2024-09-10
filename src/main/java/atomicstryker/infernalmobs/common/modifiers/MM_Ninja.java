package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Ninja extends MobModifier {

    private static final String[] suffix = { "theZenMaster", "ofEquilibrium", "ofInnerPeace" };
    private static final String[] prefix = { "totallyzen", "innerlypeaceful", "Ronin" };
    private static long coolDown;
    private static float reflectMultiplier;
    private static float maxReflectDamage;
    private long nextAbilityUse = 0L;

    public MM_Ninja(@Nullable MobModifier next) {
        super("Ninja", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        long time = mob.ticksExisted;
        if (time > nextAbilityUse && source.getEntity() != null
            && source.getEntity() != mob
            && !InfernalMobsCore.instance()
                .isInfiniteLoop(mob, source.getEntity())
            && teleportToEntity(mob, source.getEntity())) {
            nextAbilityUse = time + coolDown;
            source.getEntity()
                .attackEntityFrom(
                    DamageSource.causeMobDamage(mob),
                    Math.min(maxReflectDamage, damage * reflectMultiplier));
            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(EntityLivingBase mob, Entity par1Entity) {
        Vec3 vector = Vec3.createVectorHelper(
            mob.posX - par1Entity.posX,
            mob.boundingBox.minY + (double) (mob.height / 2.0F) - par1Entity.posY + (double) par1Entity.getEyeHeight(),
            mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 8.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.xCoord * telDist;
        double destY = mob.posY + (double) (mob.worldObj.rand.nextInt(16) - 4) - vector.yCoord * telDist;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.zCoord * telDist;
        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(EntityLivingBase mob, double destX, double destY, double destZ) {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        boolean success = false;
        mob.posX = destX;
        mob.posY = destY;
        mob.posZ = destZ;
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        Block blockID;

        if (mob.worldObj.blockExists(x, y, z)) {
            boolean hitGround = false;
            while (!hitGround && y < 96 && y > 0) {
                blockID = mob.worldObj.getBlock(x, y - 1, z);
                if (blockID.getMaterial()
                    .blocksMovement()) {
                    hitGround = true;
                } else {
                    --mob.posY;
                    --y;
                }
            }

            if (hitGround) {
                mob.setPosition(mob.posX, mob.posY, mob.posZ);

                if (mob.worldObj.getCollidingBoundingBoxes(mob, mob.boundingBox)
                    .isEmpty() && !mob.worldObj.isAnyLiquid(mob.boundingBox)
                    && !mob.worldObj.checkBlockCollision(mob.boundingBox)) {
                    success = true;
                }
            } else {
                return false;
            }

            if (!success) {
                mob.setPosition(oldX, oldY, oldZ);
                return false;
            } else {
                mob.worldObj.playSoundEffect(
                    oldX,
                    oldY,
                    oldZ,
                    "random.explode",
                    2.0F,
                    (1.0F + (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
                mob.worldObj.spawnParticle("hugeexplosion", oldX, oldY, oldZ, 0D, 0D, 0D);
            }
        }
        return true;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Ninja> {

        public Loader() {
            super(MM_Ninja.class);
        }

        @Override
        public MM_Ninja make(@Nullable MobModifier next) {
            return new MM_Ninja(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 15000L, "Time between ability uses")
                .getInt(15000) / 50;
            reflectMultiplier = (float) config.get(
                getModifierClassName(),
                "ninjaReflectMultiplier",
                0.75D,
                "When a mob with Ninja modifier gets hurt it teleports to the attacker and reflects some of the damage originally dealt. This sets the multiplier for the reflected damage")
                .getDouble(0.75D);
            maxReflectDamage = (float) config.get(
                getModifierClassName(),
                "ninjaReflectMaxDamage",
                10.0D,
                "When a mob with Ninja modifier gets hurt it teleports to the attacker and reflects some of the damage originally dealt. This sets the maximum amount that can be inflicted (0, or less than zero for unlimited reflect damage)")
                .getDouble(10.0D);
        }
    }
}
