package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

public class MM_Storm extends MobModifier {

    private final static float MIN_DISTANCE = 3F;
    private static final String[] suffix = { "ofLightning", "theRaiden" };
    private static final String[] prefix = { "striking", "thundering", "electrified" };
    private static long coolDown;
    private long nextAbilityUse = 0L;

    public MM_Storm(@Nullable MobModifier next) {
        super("Storm", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (getMobTarget() != null && getMobTarget() instanceof EntityPlayer) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target) {
        if (target == null || target.ridingEntity != null || !mob.canEntityBeSeen(target)) {
            return;
        }

        long time = mob.ticksExisted;
        if (time > nextAbilityUse && mob.getDistanceToEntity(target) > MIN_DISTANCE
            && target.worldObj.canBlockSeeTheSky(
                MathHelper.floor_double(target.posX),
                MathHelper.floor_double(target.posY),
                MathHelper.floor_double(target.posZ))) {
            nextAbilityUse = time + coolDown;
            mob.worldObj
                .addWeatherEffect(new EntityLightningBolt(mob.worldObj, target.posX, target.posY - 1, target.posZ));
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

    public static class Loader extends ModifierLoader<MM_Storm> {

        public Loader() {
            super(MM_Storm.class);
        }

        @Override
        public MM_Storm make(@Nullable MobModifier next) {
            return new MM_Storm(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 15000L, "Time between ability uses")
                .getInt(15000) / 50;
        }
    }
}
