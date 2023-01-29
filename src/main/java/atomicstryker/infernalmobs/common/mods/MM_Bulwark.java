package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Bulwark extends MobModifier {

    private static float damageMultiplier;

    public MM_Bulwark(@Nullable MobModifier next) {
        super("Bulwark", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        return super.onHurt(mob, source, Math.max(damage * damageMultiplier, 1));
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "ofTurtling", "theDefender", "ofeffingArmor" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "turtling", "defensive", "armoured" };

    public static class Loader extends ModifierLoader<MM_Bulwark> {

        public Loader() {
            super(MM_Bulwark.class);
        }

        @Override
        public MM_Bulwark make(@Nullable MobModifier next) {
            return new MM_Bulwark(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            damageMultiplier = (float) config.get(
                    getModifierClassName(),
                    "damageMultiplier",
                    0.5D,
                    "Damage (taken) multiplier, only makes sense for values < 1.0").getDouble(0.5D);
        }
    }
}
