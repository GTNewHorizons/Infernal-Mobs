package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Sticky extends MobModifier {

    private static final String[] suffix = { "ofSnagging", "theQuickFingered", "ofPettyTheft", "yoink" };
    private static final String[] prefix = { "thieving", "snagging", "quickfingered" };
    private static long coolDown;
    private static Class<?>[] disallowed = {};
    private long nextAbilityUse = 0L;

    public MM_Sticky(@Nullable MobModifier next) {
        super("Sticky", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityPlayer)
            && !((EntityPlayer) source.getEntity()).capabilities.isCreativeMode
            && !InfernalMobsCore.instance()
                .isRangedProjectile(source)) {
            EntityPlayer p = (EntityPlayer) source.getEntity();
            ItemStack weapon = p.inventory.getStackInSlot(p.inventory.currentItem);
            if (weapon != null) {
                long time = InfernalMobsCore.instance()
                    .getCooldownTime(mob);
                if (time > nextAbilityUse && source.getEntity() != null) {
                    nextAbilityUse = time + coolDown;
                    EntityItem drop = p
                        .dropPlayerItemWithRandomChoice(p.inventory.decrStackSize(p.inventory.currentItem, 1), false);
                    if (drop != null) {
                        drop.delayBeforeCanPickup = 50;
                        p.worldObj.playSoundAtEntity(
                            mob,
                            "mob.slime.attack",
                            1.0F,
                            (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
                    }
                }
            }
        }

        return super.onHurt(mob, source, damage);
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

    public static class Loader extends ModifierLoader<MM_Sticky> {

        public Loader() {
            super(MM_Sticky.class, new String[] { "net.minecraft.entity.monster.EntityCreeper" });
        }

        @Override
        public MM_Sticky make(@Nullable MobModifier next) {
            return new MM_Sticky(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            super.loadConfig(config);
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 15000L, "Time between ability uses")
                .getInt(15000)
                / InfernalMobsCore.instance()
                    .getOldIFFactor();

            disallowed = getBannedClassesToArray();
        }
    }
}
