package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Regen extends MobModifier
{
    private long nextAbilityUse = 0L;
    private static long coolDown;

    public MM_Regen(EntityLivingBase mob)
    {
        this.modName = "Regen";
    }
    
    public MM_Regen(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Regen";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (mob.getHealth() < getActualMaxHealth(mob))
        {
            long time = mob.ticksExisted;
            if (time > nextAbilityUse)
            {
                nextAbilityUse = time+coolDown;
                InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth()+1);
            }
        }
        return super.onUpdate(mob);
    }

    public static void loadConfig(Configuration config)
    {
        coolDown = config.get(MM_Regen.class.getSimpleName(), "coolDownMillis", 500L, "Time between ability uses").getInt(500) / 50;
    }

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofWTFIMBA", "theCancerous", "ofFirstAid" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "regenerating", "healing", "nighunkillable" };
}
