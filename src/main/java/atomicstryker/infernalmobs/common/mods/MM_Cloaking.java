package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Cloaking extends MobModifier
{
    private long nextAbilityUse = 0L;
    private static long coolDown;
    private static int potionDuration;

    public MM_Cloaking(EntityLivingBase mob)
    {
        this.modName = "Cloaking";
    }
    
    public MM_Cloaking(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Cloaking";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob);
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLivingBase)
        {
            tryAbility(mob);
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob)
    {
        long time = mob.ticksExisted;
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            mob.addPotionEffect(new PotionEffect(Potion.invisibility.id, potionDuration));
        }
    }

    public static void loadConfig(Configuration config)
    {
        potionDuration = config.get(MM_Cloaking.class.getSimpleName(), "cloakingDurationTicks", 200L, "Time mob is cloaked").getInt(200);
        coolDown = config.get(MM_Cloaking.class.getSimpleName(), "coolDownMillis", 12000L, "Time between ability uses").getInt(12000) / 50;
    }

    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class<?>[] disallowed = { EntitySpider.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofStalking", "theUnseen", "thePredator" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "stalking", "unseen", "hunting" };
    
}
