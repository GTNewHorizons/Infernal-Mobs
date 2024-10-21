package atomicstryker.infernalmobs.common.modifiers;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.config.Configuration;

public class MM_Rust extends MobModifier {

    private static final String[] suffix = { "ofDecay", "theEquipmentHaunter" };
    private static final String[] prefix = { "rusting", "decaying" };
    private static int itemDamage;

    public MM_Rust(@Nullable MobModifier next) {
        super("Rust", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityPlayer)
            && !(source instanceof EntityDamageSourceIndirect)) {
            EntityPlayer p = (EntityPlayer) source.getEntity();
            if (p.inventory.getCurrentItem() != null) {
                p.inventory.getCurrentItem()
                    .damageItem(itemDamage, (EntityLivingBase) source.getEntity());
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).inventory.damageArmor(damage * 3);
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Rust> {

        public Loader() {
            super(MM_Rust.class);
        }

        @Override
        public MM_Rust make(@Nullable MobModifier next) {
            return new MM_Rust(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            itemDamage = config
                .get(getModifierClassName(), "itemDamage", 4, "Damage dealt to Item in hand of attacking entity")
                .getInt(4);
        }
    }
}
