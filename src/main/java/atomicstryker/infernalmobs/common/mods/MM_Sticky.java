package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Sticky extends MobModifier
{
    private long nextAbilityUse = 0L;
    private static long coolDown;

    public MM_Sticky(EntityLivingBase mob)
    {
        this.modName = "Sticky";
    }
    
    public MM_Sticky(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Sticky";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityPlayer)
        && !((EntityPlayer) source.getEntity()).capabilities.isCreativeMode
        && !(source instanceof EntityDamageSourceIndirect)
        && !source.isProjectile())
        {
            EntityPlayer p = (EntityPlayer)source.getEntity();
            ItemStack weapon = p.inventory.getStackInSlot(p.inventory.currentItem);
            if (weapon != null)
            {
                long time = mob.ticksExisted;
                if (time > nextAbilityUse
                && source.getEntity() != null
                && !(source instanceof EntityDamageSourceIndirect))
                {
                    nextAbilityUse = time+coolDown;
                    EntityItem drop = p.dropPlayerItemWithRandomChoice(p.inventory.decrStackSize(p.inventory.currentItem, 1), false);
                    if (drop != null)
                    {
                        drop.delayBeforeCanPickup = 50;
                        p.worldObj.playSoundAtEntity(mob, "mob.slime.attack", 1.0F, (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
                    }
                }
            }
        }
        
        return super.onHurt(mob, source, damage);
    }

    public static void loadConfig(Configuration config)
    {
        coolDown = config.get(MM_Sticky.class.getSimpleName(), "coolDownMillis", 15000L, "Time between ability uses").getInt(15000) / 50;
    }

    private Class<?>[] disallowed = { EntityCreeper.class };
    
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofSnagging", "theQuickFingered", "ofPettyTheft", "yoink" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "thieving", "snagging", "quickfingered" };
    
}
