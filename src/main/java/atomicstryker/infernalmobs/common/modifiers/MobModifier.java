package atomicstryker.infernalmobs.common.modifiers;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;

import org.apache.logging.log4j.Level;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import cpw.mods.fml.common.FMLLog;

public abstract class MobModifier {

    /**
     * next MobModifier in a linked chain, on the last one this field is null
     */
    @Nullable
    protected final MobModifier nextMod;

    /**
     * name of this particular MobModifier instance
     */
    private final String modName;

    /**
     * keeps track of our past-max-bounds health patch
     */
    private boolean healthHacked;

    /**
     * clientside health value to be displayed, because health is not networked
     */
    private float actualHealth;

    /**
     * Display-sized (up to 5) series of Modifier Strings, buffered
     */
    private String[] bufferedNames;

    /**
     * buffered maximum health
     */
    private float actualMaxHealth;

    /**
     * internal mob attack target
     */
    private EntityLivingBase attackTarget;

    /**
     * buffered modifier size
     */
    private int bufferedSize;

    /**
     * buffered modifier string result
     */
    private String bufferedEntityName;

    public MobModifier(String name, @Nullable MobModifier next) {
        nextMod = next;
        healthHacked = false;
        actualHealth = 100;
        actualMaxHealth = -1;
        bufferedSize = 0;
        modName = name;
    }

    /**
     * @return the complete List of linked Modifiers as their Names
     */
    public String getLinkedModName() {
        return (StatCollector.translateToLocal("translation.infernalmobs:mod." + modName) + " "
            + ((nextMod != null) ? nextMod.getLinkedModName() : ""));
    }

    /**
     * @return same as above, but without using the translation system
     */
    public String getLinkedModNameUntranslated() {
        return modName + " " + ((nextMod != null) ? nextMod.getLinkedModNameUntranslated() : "");
    }

    /**
     * @return Display-sized (up to 5) series of Modifier Strings
     */
    public String[] getDisplayNames() {
        if (bufferedNames == null) {
            String[] allMods = getLinkedModName().split(" ");
            int index = 0;
            int j = 0;
            bufferedNames = new String[3];
            bufferedNames[index] = "";
            for (String m : allMods) {
                bufferedNames[index] = bufferedNames[index] + " " + m;
                j++;
                if (j % 5 == 0 && index + 1 < bufferedNames.length) {
                    index++;
                    bufferedNames[index] = "";
                }
            }
        }
        return bufferedNames;
    }

    /**
     * Helper to avoid adding the same mod twice
     */
    public boolean containsModifierClass(Class<?> checkfor) {
        if (checkfor.equals(this.getClass())) {
            return true;
        }

        if (nextMod != null) {
            return nextMod.containsModifierClass(checkfor);
        }

        return false;
    }

    /**
     * Called when local Spawn Processing is completed or when a client remote-attached Modifiers to a local Entity
     */
    public void onSpawningComplete(EntityLivingBase entity) {
        String oldTag = entity.getEntityData()
            .getString(InfernalMobsCore.getNBTTag());
        if (!oldTag.isEmpty() && !oldTag.equals(getLinkedModNameUntranslated())) {
            FMLLog.log(
                "InfernalMobs",
                Level.DEBUG,
                String.format(
                    "Infernal Mobs tag mismatch!! Was [%s], now trying to set [%s] \n",
                    oldTag,
                    getLinkedModNameUntranslated()));
        }
        entity.getEntityData()
            .setString(InfernalMobsCore.getNBTTag(), getLinkedModNameUntranslated());
    }

    /**
     * Passes the death event to the modifier list
     *
     * @return true if death should be aborted
     */
    public boolean onDeath() {
        // TODO does it properly fires for every mod in the chain including the first that it was called on ?
        attackTarget = null;
        if (nextMod != null) {
            return nextMod.onDeath();
        }

        return false;
    }

    /**
     * Passes the loot drop event to the modifier list
     */
    public void onDropItems(EntityLivingBase moddedMob, DamageSource killSource, ArrayList<EntityItem> drops,
        int lootingLevel, boolean recentlyHit, int specialDropValue) {
        if (recentlyHit) {
            InfernalMobsCore.instance()
                .dropLootForEnt(moddedMob, this);
        }
    }

    /**
     * passes the setAttackTarget event to the modifier list
     *
     * @param target being passed from the event
     */
    public void onSetAttackTarget(EntityLivingBase target) {
        attackTarget = target;
        if (nextMod != null) {
            nextMod.onSetAttackTarget(target);
        }
    }

    /**
     * Modified Mob attacks something
     *
     * @param entity Entity being attacked
     * @param source DamageSource instance doing the attacking
     * @param amount unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public float onAttack(EntityLivingBase entity, DamageSource source, float amount) {
        // TODO does it properly fires for every mod in the chain including the first that it was called on ?
        if (nextMod != null) {
            return nextMod.onAttack(entity, source, amount);
        }
        return amount;
    }

    /**
     * Modified Mob is being hurt
     *
     * @param source Damagesource doing the hurting
     * @param amount unmitigated damage value
     * @return damage to be applied after we processed the value
     */
    public float onHurt(EntityLivingBase mob, DamageSource source, float amount) {
        // TODO does it properly fires for every mod in the chain including the first that it was called on ?
        if (nextMod != null) {
            amount = nextMod.onHurt(mob, source, amount);
        } else if (source.getEntity() != null) {
            if (source.getEntity().worldObj.isRemote && source.getEntity() instanceof EntityPlayer) {
                InfernalMobsCore.instance()
                    .sendHealthRequestPacket(mob);
            }
        }

        return amount;
    }

    /**
     * passes the fall event to the modifier list
     */
    public boolean onFall(float distance) {
        // TODO does it properly fires for every mod in the chain including the first that it was called on ?
        if (nextMod != null) {
            return nextMod.onFall(distance);
        }
        return false;
    }

    /**
     * passes the jump event to the modifier list
     */
    public void onJump(EntityLivingBase entityLiving) {
        // TODO does it properly fires for every mod in the chain including the first that it was called on ?
        if (nextMod != null) {
            nextMod.onJump(entityLiving);
        }
    }

    /**
     * passes the update event to the modifier list the return value is currently unused
     */
    public boolean onUpdate(EntityLivingBase mob) {
        // TODO does it properly fires for every mod in the chain including the first that it was called on ?
        if (nextMod != null) {
            return nextMod.onUpdate(mob);
        } else {
            if (attackTarget == null) {
                if (mob instanceof EntityMob && ((EntityMob) mob).getEntityToAttack() instanceof EntityLivingBase) {
                    attackTarget = (EntityLivingBase) ((EntityMob) mob).getEntityToAttack();
                }
            }

            if (attackTarget != null) {
                if (attackTarget.isDead || attackTarget.getDistanceToEntity(mob) > 15f
                    || (attackTarget instanceof EntityPlayer
                        && ((EntityPlayer) attackTarget).capabilities.disableDamage)) {
                    attackTarget = null;
                }
            }
        }

        return false;
    }

    /**
     * clientside helper method. Due to the health not being networked, we keep track of it internally, here. Also, this
     * is a good spot for the more-than-allowed health hack.
     */
    public float getActualHealth(EntityLivingBase mob) {
        if (!mob.worldObj.isRemote) {
            increaseHealthForMob(mob, getActualMaxHealth(mob));
        }

        return actualHealth;
    }

    /**
     * Prevents exponential health increase from re-loading the same infernal mob again and again
     */
    public void setHealthAlreadyHacked(EntityLivingBase mob) {
        if (!mob.worldObj.isRemote) {
            actualMaxHealth = getActualMaxHealth(mob);
            healthHacked = true;
        }
    }

    private void increaseHealthForMob(EntityLivingBase mob, float baseHealth) {
        if (!healthHacked) {
            actualMaxHealth = getActualMaxHealth(mob);
            actualHealth = actualMaxHealth;
            InfernalMobsCore.instance()
                .setEntityHealthPastMax(mob, actualHealth);
            healthHacked = true;
        }
    }

    /**
     * @return buffered modified max health
     */
    public float getActualMaxHealth(EntityLivingBase mob) {
        if (actualMaxHealth < 0) {
            actualMaxHealth = (float) (InfernalMobsCore.instance()
                .getMobClassMaxHealth(mob) * getModSize()
                * InfernalMobsCore.instance()
                    .getMobModHealthFactor());
        }
        return actualMaxHealth;
    }

    /**
     * clientside receiving end of health packets sent from the InfernalMobs server instance
     */
    public void setActualHealth(float health, float maxHealth) {
        actualHealth = health;
        actualMaxHealth = maxHealth;
    }

    protected EntityLivingBase getMobTarget() {
        return attackTarget;
    }

    /**
     * @return Array of classes an EntityLiving cannot equal, implement or extend in order for this MobModifier to be
     *         applied to it
     */
    public Class<?>[] getBlackListMobClasses() {
        return null;
    }

    /**
     * @return Array of MobModifiers a considered MobModifier should not be mixed with. Both sides need to exclude each
     *         other for this to work.
     */
    public Class<?>[] getModsNotToMixWith() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MobModifier && ((MobModifier) o).modName.equals(modName));
    }

    /**
     * @return size of linked Mod list
     */
    public int getModSize() {
        if (bufferedSize == 0) {
            bufferedSize = 1;
            MobModifier nextmod = this.nextMod;
            while (nextmod != null) {
                bufferedSize++;
                nextmod = nextmod.nextMod;
            }
        }
        return bufferedSize;
    }

    /**
     * Should be overridden by modifiers to provide possible name prefixes
     */
    protected String[] getModNamePrefix() {
        return null;
    }

    /**
     * Should be overridden by modifiers to provide possible name suffixes
     */
    protected String[] getModNameSuffix() {
        return null;
    }

    /**
     * Creates the Entity name the Infernal Mobs GUI displays, and buffers it
     *
     * @param target Entity to create the Name from
     * @return Entity display name such as 'Rare Zombie'
     */
    public String getEntityDisplayName(EntityLivingBase target) {
        if (bufferedEntityName == null) {
            StringBuilder buffer = new StringBuilder();

            String mobName = target.getCommandSenderName();
            if (mobName == null) {
                mobName = target.getClass()
                    .getSimpleName();
                if (!StringUtils.isNullOrEmpty(mobName)) {
                    mobName = mobName.replaceFirst("Entity", "");
                } else {
                    mobName = "Monster";
                }
            }

            int size = getModSize();

            int randomMod = target.getRNG()
                .nextInt(getModSize());
            MobModifier mod = this;
            while (randomMod > 0) {
                mod = mod.nextMod;
                randomMod--;
            }

            String modprefix = "";
            if (mod.getModNamePrefix() != null) {
                modprefix = mod.getModNamePrefix()[target.getRNG()
                    .nextInt(mod.getModNamePrefix().length)];
                modprefix = StatCollector.translateToLocal("translation.infernalmobs:prefix." + modprefix);
            }

            if (size <= 5) {
                buffer.append(EnumChatFormatting.YELLOW)
                    .append(StatCollector.translateToLocal("translation.infernalmobs:rareClass"));
            } else if (size <= 10) {
                buffer.append(EnumChatFormatting.GOLD)
                    .append(StatCollector.translateToLocal("translation.infernalmobs:ultraClass"));
            } else {
                buffer.append(EnumChatFormatting.RED)
                    .append(StatCollector.translateToLocal("translation.infernalmobs:infernalClass"));
            }
            buffer.append(modprefix);
            buffer.append(mobName);

            if (size > 1) {
                mod = mod.nextMod != null ? mod.nextMod : this;
                if (mod.getModNameSuffix() != null) {
                    String pickedSuffix = mod.getModNameSuffix()[target.getRNG()
                        .nextInt(mod.getModNameSuffix().length)];
                    buffer.append(StatCollector.translateToLocal("translation.infernalmobs:suffix." + pickedSuffix));
                }
            }

            bufferedEntityName = buffer.toString();
        }

        return bufferedEntityName;
    }

    /**
     * if a modifier does permanent changes to an entity, they need to register the playername at
     * InfernalMobsCore.instance().getModifiedPlayerTimes() with the current timestamp a timer keeps track of the player
     * not being infernal'd for a while and triggers this modifiers should override this and clean up their changes when
     * necessary
     */
    public void resetModifiedVictim(EntityPlayer victim) {
        // NOOP by default
    }

    public String getModName() {
        return modName;
    }
}
