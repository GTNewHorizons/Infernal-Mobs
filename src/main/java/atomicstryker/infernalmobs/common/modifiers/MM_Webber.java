package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

public class MM_Webber extends MobModifier {

    private static final Class<?>[] modBans = { MM_Gravity.class, MM_Blastoff.class };
    private static final String[] suffix = { "ofTraps", "theMutated", "theSpider" };
    private static final String[] prefix = { "ensnaring", "webbing" };
    private static long coolDown;
    private long lastAbilityUse = 0L;

    public MM_Webber(@Nullable MobModifier next) {
        super("Webber", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (getMobTarget() != null && getMobTarget() instanceof EntityPlayer) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && source.getEntity() instanceof EntityLivingBase) {
            tryAbility(mob, (EntityLivingBase) source.getEntity());
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target) {
        if (target == null) return;
        if (target instanceof EntityPlayer player && player.capabilities.disableDamage) return;
        if (!target.worldObj.getGameRules()
            .getGameRuleBooleanValue("mobGriefing")) return;
        if (!mob.canEntityBeSeen(target)) return;

        int x = MathHelper.floor_double(target.posX);
        int y = MathHelper.floor_double(target.posY);
        int z = MathHelper.floor_double(target.posZ);

        long time = mob.ticksExisted;
        if (time > lastAbilityUse + coolDown) {
            int offset;
            if (target.worldObj.getBlock(x, y - 1, z) == Blocks.air) {
                offset = -1;
            } else if (target.worldObj.getBlock(x, y, z) == Blocks.air) {
                offset = 0;
            } else {
                return;
            }

            lastAbilityUse = time;
            target.worldObj.setBlock(x, y + offset, z, Blocks.web, 0, 3);
            mob.worldObj.playSoundAtEntity(
                mob,
                "mob.spider.say",
                1.0F,
                (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
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

    public static class Loader extends ModifierLoader<MM_Webber> {

        public Loader() {
            super(MM_Webber.class);
        }

        @Override
        public MM_Webber make(@Nullable MobModifier next) {
            return new MM_Webber(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 15000L, "Time between ability uses")
                .getInt(15000) / 50;
        }
    }
}
