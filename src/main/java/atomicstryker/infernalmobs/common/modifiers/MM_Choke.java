package atomicstryker.infernalmobs.common.modifiers;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;

import atomicstryker.infernalmobs.common.InfernalMobsCore;

public class MM_Choke extends MobModifier {

    private static final String[] suffix = { "ofBreathlessness", "theAnaerobic", "ofDeprivation" };
    private static final String[] prefix = { "Sith Lord", "Dark Lord", "Darth" };
    private EntityLivingBase lastTarget;
    private int lastAir = -999;

    public MM_Choke(@Nullable MobModifier next) {
        super("Choke", next);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob) {
        if (getMobTarget() != lastTarget) {
            lastAir = -999;
            if (lastTarget != null) {
                updateAir();
            }
            lastTarget = getMobTarget();
        }

        if (lastTarget != null) {
            if (mob.canEntityBeSeen(lastTarget)) {
                if (lastAir == -999) {
                    lastAir = lastTarget.getAir();
                } else {
                    lastAir = Math.min(lastAir, lastTarget.getAir());
                }

                if (!(lastTarget instanceof EntityPlayer && ((EntityPlayer) lastTarget).capabilities.disableDamage)) {
                    lastAir--;
                    if (lastAir < -19) {
                        lastAir = 0;
                        lastTarget.attackEntityFrom(DamageSource.drown, 2.0F);
                    }

                    if (!mob.isDead) {
                        updateAir();
                    }
                }
            }
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (lastTarget != null && source.getSourceOfDamage() == lastTarget && lastAir != -999) {
            lastAir += 60;
            updateAir();
        }

        return damage;
    }

    @Override
    public boolean onDeath() {
        lastAir = -999;
        if (lastTarget != null) {
            updateAir();
            lastTarget = null;
        }
        return false;
    }

    private void updateAir() {
        if (lastTarget == null) return;

        lastTarget.setAir(lastAir);
        if (lastTarget instanceof EntityPlayerMP) {
            InfernalMobsCore.instance()
                .sendAirPacket((EntityPlayerMP) lastTarget, lastAir);

            UUID id = lastTarget.getUniqueID();
            if (id != null) {
                InfernalMobsCore.instance()
                    .getModifiedPlayerTimes()
                    .put(id, System.currentTimeMillis());
            }
        }
    }

    @Override
    public void resetModifiedVictim(EntityPlayer victim) {
        victim.setAir(-999);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    public static class Loader extends ModifierLoader<MM_Choke> {

        public Loader() {
            super(MM_Choke.class);
        }

        @Override
        public MM_Choke make(@Nullable MobModifier next) {
            return new MM_Choke(next);
        }
    }
}
