package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Quicksand extends MobModifier {

    public MM_Quicksand(@Nullable MobModifier next) {
        super("Quicksand", next);
    }

    int ticker = 0;

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (getMobTarget() != null && InfernalMobsCore.instance().getIsEntityAllowedTarget(getMobTarget())
                && mob.canEntityBeSeen(getMobTarget())
                && ++ticker >= 80) {
            ticker = 0;
            getMobTarget().addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 0));
        }

        return super.onUpdate(mob);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static final String[] suffix = { "ofYouCantRun", "theSlowingB" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static final String[] prefix = { "slowing", "Quicksand" };

    public static class Loader extends ModifierLoader<MM_Quicksand> {

        public Loader() {
            super(MM_Quicksand.class);
        }

        @Override
        public MM_Quicksand make(@Nullable MobModifier next) {
            return new MM_Quicksand(next);
        }
    }
}
