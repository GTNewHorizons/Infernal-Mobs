package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraftforge.common.config.Configuration;

public class MM_Unyielding extends MobModifier {

    private static Class<?>[] disallowed = {};
    private static final String[] suffix = { "ofRelentlessness", "theUnYielding", "theUnstoppable" };
    private static final String[] prefix = { "relentless", "unyielding", "unstoppable" };

    public MM_Unyielding(@Nullable MobModifier next) {
        super("Unyielding", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        mob.getEntityAttribute(SharedMonsterAttributes.knockbackResistance)
            .setBaseValue(Double.MAX_VALUE);

        return super.onUpdate(mob);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
    }

    public static class Loader extends ModifierLoader<MM_Unyielding> {

        public Loader() {
            super(MM_Unyielding.class, emptyString);
        }

        @Override
        public MM_Unyielding make(@Nullable MobModifier next) {
            return new MM_Unyielding(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            super.loadConfig(config);
            disallowed = getBannedClassesToArray();
        }
    }
}
