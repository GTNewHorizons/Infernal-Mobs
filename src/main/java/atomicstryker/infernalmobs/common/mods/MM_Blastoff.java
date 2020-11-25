package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Blastoff extends MobModifier
{
    private long nextAbilityUse = 0L;
    private static long coolDown;

    public MM_Blastoff(EntityLivingBase mob)
    {
        this.modName = "Blastoff";
    }
    
    public MM_Blastoff(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Blastoff";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        EntityLivingBase target = getMobTarget();

        if (target != null
        && target instanceof EntityPlayer
        && !(target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.disableDamage))
        {
            tryAbility(mob, target);
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLivingBase
        && !(source.getEntity() instanceof EntityPlayer && ((EntityPlayer) source.getEntity()).capabilities.disableDamage))
        {
            tryAbility(mob, (EntityLivingBase) source.getEntity());
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        long time = mob.ticksExisted;
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            mob.worldObj.playSoundAtEntity(mob, "mob.slime.attack", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            
            if (target.worldObj.isRemote || !(target instanceof EntityPlayerMP))
            {
                target.addVelocity(0, 1.1D, 0);
            }
            else
            {
                InfernalMobsCore.instance().sendVelocityPacket((EntityPlayerMP)target, 0f, 1.1f, 0f);
            }
        }
    }
    
    public static void loadConfig(Configuration config)
    {
        coolDown = config.get(MM_Blastoff.class.getSimpleName(), "coolDownMillis", 15000L, "Time between ability uses").getInt(15000) / 50;
    }

    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Webber.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofMissionControl", "theNASA", "ofWEE" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "thumping", "trolling", "byebye" };
    
}
