package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

public class MM_Alchemist extends MobModifier {

    private static final float MIN_DISTANCE = 2F;
    private static final String[] suffix = { "theWitchkin", "theBrewmaster", "theSinged" };
    private static final String[] prefix = { "witchkin", "brewing", "singed" };
    private static long coolDown;
    private long nextAbilityUse = 0L;

    public MM_Alchemist(@Nullable MobModifier next) {
        super("Alchemist", next);
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
            EntityPotion potion = new EntityPotion(mob.worldObj, mob, 32732);
            potion.rotationPitch -= -20.0F;
            double diffX = target.posX + target.motionX - mob.posX;
            double diffY = target.posY + (double) target.getEyeHeight() - 1.100000023841858D - mob.posY;
            double diffZ = target.posZ + target.motionZ - mob.posZ;
            float distance = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

            if (distance >= 8.0F && !target.isPotionActive(Potion.moveSlowdown)) {
                potion.setPotionDamage(32698);
            } else if (target.getHealth() >= 8 && !target.isPotionActive(Potion.poison)) {
                potion.setPotionDamage(32660);
            } else if (distance <= 3.0F && !target.isPotionActive(Potion.weakness)
                && mob.getRNG()
                    .nextFloat() < 0.25F) {
                        potion.setPotionDamage(32696);
                    }

            potion.setThrowableHeading(diffX, diffY + (double) (distance * 0.2F), diffZ, 0.75F, 8.0F);
            mob.worldObj.spawnEntityInWorld(potion);
        }
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Alchemist> {

        public Loader() {
            super(MM_Alchemist.class);
        }

        @Override
        public MM_Alchemist make(@Nullable MobModifier next) {
            return new MM_Alchemist(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 6000L, "Time between ability uses")
                .getInt(6000) / 50;
        }
    }
}
