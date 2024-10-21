package atomicstryker.infernalmobs.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;

import atomicstryker.infernalmobs.Tags;
import atomicstryker.infernalmobs.common.commands.InfernalCommandFindEntityClass;
import atomicstryker.infernalmobs.common.commands.InfernalCommandSpawnInfernal;
import atomicstryker.infernalmobs.common.events.EntityEventHandler;
import atomicstryker.infernalmobs.common.events.SaveEventHandler;
import atomicstryker.infernalmobs.common.modifiers.MM_1UP;
import atomicstryker.infernalmobs.common.modifiers.MM_Alchemist;
import atomicstryker.infernalmobs.common.modifiers.MM_Berserk;
import atomicstryker.infernalmobs.common.modifiers.MM_Blastoff;
import atomicstryker.infernalmobs.common.modifiers.MM_Bulwark;
import atomicstryker.infernalmobs.common.modifiers.MM_Choke;
import atomicstryker.infernalmobs.common.modifiers.MM_Cloaking;
import atomicstryker.infernalmobs.common.modifiers.MM_Darkness;
import atomicstryker.infernalmobs.common.modifiers.MM_Ender;
import atomicstryker.infernalmobs.common.modifiers.MM_Exhaust;
import atomicstryker.infernalmobs.common.modifiers.MM_Fiery;
import atomicstryker.infernalmobs.common.modifiers.MM_Ghastly;
import atomicstryker.infernalmobs.common.modifiers.MM_Gravity;
import atomicstryker.infernalmobs.common.modifiers.MM_Lifesteal;
import atomicstryker.infernalmobs.common.modifiers.MM_Ninja;
import atomicstryker.infernalmobs.common.modifiers.MM_Poisonous;
import atomicstryker.infernalmobs.common.modifiers.MM_Quicksand;
import atomicstryker.infernalmobs.common.modifiers.MM_Regen;
import atomicstryker.infernalmobs.common.modifiers.MM_Rust;
import atomicstryker.infernalmobs.common.modifiers.MM_Sapper;
import atomicstryker.infernalmobs.common.modifiers.MM_Sprint;
import atomicstryker.infernalmobs.common.modifiers.MM_Sticky;
import atomicstryker.infernalmobs.common.modifiers.MM_Storm;
import atomicstryker.infernalmobs.common.modifiers.MM_Vengeance;
import atomicstryker.infernalmobs.common.modifiers.MM_Weakness;
import atomicstryker.infernalmobs.common.modifiers.MM_Webber;
import atomicstryker.infernalmobs.common.modifiers.MM_Wither;
import atomicstryker.infernalmobs.common.modifiers.MobModifier;
import atomicstryker.infernalmobs.common.modifiers.ModifierLoader;
import atomicstryker.infernalmobs.common.network.NetworkHelper;
import atomicstryker.infernalmobs.common.network.packets.AirPacket;
import atomicstryker.infernalmobs.common.network.packets.HealthPacket;
import atomicstryker.infernalmobs.common.network.packets.KnockBackPacket;
import atomicstryker.infernalmobs.common.network.packets.MobModsPacket;
import atomicstryker.infernalmobs.common.network.packets.VelocityPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.registry.GameData;

@Mod(modid = "InfernalMobs", name = "Infernal Mobs", version = Tags.VERSION)
public class InfernalMobsCore {

    private final long existCheckDelay = 5000L;

    private long nextExistCheckTime;

    /**
     * Array of ItemStacks
     */
    private ArrayList<Integer> dimensionBlackList;
    private ArrayList<ItemStack> dropIdListElite;
    private ArrayList<ItemStack> dropIdListUltra;
    private ArrayList<ItemStack> dropIdListInfernal;

    private HashMap<String, Boolean> classesAllowedMap;
    private HashMap<String, Boolean> classesForcedMap;
    private HashMap<String, Float> classesHealthMap;
    private boolean useSimpleEntityClassNames;
    private boolean disableHealthBar;
    private double modHealthFactor;

    private Entity infCheckA;
    private Entity infCheckB;

    @Instance("InfernalMobs")
    private static InfernalMobsCore instance;

    public static InfernalMobsCore instance() {
        return instance;
    }

    public static String getNBTTag() {
        return "InfernalMobsMod";
    }

    private ArrayList<ModifierLoader<?>> modifierLoaders;

    private int eliteRarity;
    private int ultraRarity;
    private int infernoRarity;

    private int minEliteModifiers;
    private int maxEliteModifiers;
    private int minUltraModifiers;
    private int maxUltraModifiers;
    private int minInfernoModifiers;
    private int maxInfernoModifiers;
    public Configuration config;

    @SidedProxy(
        clientSide = "atomicstryker.infernalmobs.client.InfernalMobsClient",
        serverSide = "atomicstryker.infernalmobs.common.InfernalMobsServer")
    public static ISidedProxy proxy;

    public NetworkHelper networkHelper;

    /*
     * saves the last timestamp of long term affected players (eg choke) reset the players by timer if the mod didn't
     * remove them
     */
    private HashMap<UUID, Long> modifiedPlayerTimes;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        dimensionBlackList = new ArrayList<>();
        dropIdListElite = new ArrayList<>();
        dropIdListUltra = new ArrayList<>();
        dropIdListInfernal = new ArrayList<>();
        nextExistCheckTime = System.currentTimeMillis();
        classesAllowedMap = new HashMap<>();
        classesForcedMap = new HashMap<>();
        classesHealthMap = new HashMap<>();
        modifiedPlayerTimes = new HashMap<>();

        config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        if (config.hasChanged()) config.save();
        loadMods();

        proxy.preInit();
        FMLCommonHandler.instance()
            .bus()
            .register(this);

        networkHelper = new NetworkHelper(
            "AS_IF",
            MobModsPacket.class,
            HealthPacket.class,
            VelocityPacket.class,
            KnockBackPacket.class,
            AirPacket.class);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new SaveEventHandler());

        proxy.load();

        FMLLog.log(
            "InfernalMobs",
            Level.INFO,
            String.format("InfernalMobsCore load() completed! Modifiers ready: %s", modifierLoaders.size()));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        // lets use postInit so mod Blocks and Items are present
        loadConfig();
    }

    @EventHandler
    public void serverStarted(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new InfernalCommandFindEntityClass());
        evt.registerServerCommand(new InfernalCommandSpawnInfernal());
    }

    /**
     * Registers the MobModifier factories for consideration
     */
    private void loadMods() {
        modifierLoaders = Lists.newArrayList(
            new MM_1UP.Loader(),
            new MM_Alchemist.Loader(),
            new MM_Berserk.Loader(),
            new MM_Blastoff.Loader(),
            new MM_Bulwark.Loader(),
            new MM_Choke.Loader(),
            new MM_Cloaking.Loader(),
            new MM_Darkness.Loader(),
            new MM_Ender.Loader(),
            new MM_Exhaust.Loader(),
            new MM_Fiery.Loader(),
            new MM_Ghastly.Loader(),
            new MM_Gravity.Loader(),
            new MM_Lifesteal.Loader(),
            new MM_Ninja.Loader(),
            new MM_Poisonous.Loader(),
            new MM_Quicksand.Loader(),
            new MM_Regen.Loader(),
            new MM_Rust.Loader(),
            new MM_Sapper.Loader(),
            new MM_Sprint.Loader(),
            new MM_Sticky.Loader(),
            new MM_Storm.Loader(),
            new MM_Vengeance.Loader(),
            new MM_Weakness.Loader(),
            new MM_Webber.Loader(),
            new MM_Wither.Loader());
        modifierLoaders.removeIf(
            loader -> !config.get(Configuration.CATEGORY_GENERAL, loader.getModifierClassName() + " enabled", true)
                .getBoolean(true));

        if (config.hasChanged()) config.save();
    }

    /**
     * Forge Config file
     */
    private void loadConfig() {
        eliteRarity = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "eliteRarity",
                    15,
                    "One in THIS many Mobs will become atleast rare")
                .getString());
        ultraRarity = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "ultraRarity",
                    7,
                    "One in THIS many already rare Mobs will become atleast ultra")
                .getString());
        infernoRarity = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "infernoRarity",
                    7,
                    "One in THIS many already ultra Mobs will become infernal")
                .getString());
        minEliteModifiers = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "minEliteModifiers",
                    2,
                    "Minimum number of Modifiers an Elite mob will receive")
                .getString());
        maxEliteModifiers = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "maxEliteModifiers",
                    5,
                    "Maximum number of Modifiers an Elite mob will receive")
                .getString());
        minUltraModifiers = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "minUltraModifiers",
                    5,
                    "Minimum number of Modifiers an Ultra mob will receive")
                .getString());
        maxUltraModifiers = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "maxUltraModifiers",
                    10,
                    "Maximum number of Modifiers an Ultra mob will receive")
                .getString());
        minInfernoModifiers = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "minInfernoModifiers",
                    8,
                    "Minimum number of Modifiers an Inferno mob will receive")
                .getString());
        maxInfernoModifiers = Integer.parseInt(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "maxInfernoModifiers",
                    15,
                    "Maximum number of Modifiers an Inferno mob will receive")
                .getString());
        useSimpleEntityClassNames = config
            .get(
                Configuration.CATEGORY_GENERAL,
                "useSimpleEntityClassnames",
                true,
                "Use Entity class names instead of ingame Entity names for the config")
            .getBoolean(true);
        disableHealthBar = config
            .get(
                Configuration.CATEGORY_GENERAL,
                "disableGUIoverlay",
                false,
                "Disables the ingame Health and Name overlay")
            .getBoolean(false);
        modHealthFactor = config
            .get(
                Configuration.CATEGORY_GENERAL,
                "mobHealthFactor",
                "1.0",
                "Multiplier applied ontop of all of the modified Mobs health")
            .getDouble(1.0D);

        parseItemsForList(
            config.get(
                Configuration.CATEGORY_GENERAL,
                "droppedItemIDsElite",
                "iron_shovel,iron_pickaxe,iron_axe,iron_sword,iron_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,iron_helmet,iron_chestplate,iron_leggings,iron_boots,cookie-0-6",
                "List of equally likely to drop Items for Elites, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                .getString(),
            this.dropIdListElite);

        parseItemsForList(
            config.get(
                Configuration.CATEGORY_GENERAL,
                "droppedItemIDsUltra",
                "bow,iron_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,iron_helmet,iron_chestplate,iron_leggings,iron_boots,golden_helmet,golden_chestplate,golden_leggings,golden_boots,golden_apple,blaze_powder-0-3,enchanted_book",
                "List of equally likely to drop Items for Ultras, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                .getString(),
            this.dropIdListUltra);

        parseItemsForList(
            config.get(
                Configuration.CATEGORY_GENERAL,
                "droppedItemIDsInfernal",
                "diamond-0-3,diamond_sword,diamond_shovel,diamond_pickaxe,diamond_axe,diamond_hoe,chainmail_helmet,chainmail_chestplate,chainmail_leggings,chainmail_boots,diamond_helmet,diamond_chestplate,diamond_leggings,diamond_boots,ender_pearl,enchanted_book",
                "List of equally likely to drop Items for Infernals, seperated by commas, syntax: ID-meta-stackSize-stackSizeRandomizer, everything but ID is optional, see changelog")
                .getString(),
            this.dropIdListInfernal);

        parseIDsForList(
            config
                .get(
                    Configuration.CATEGORY_GENERAL,
                    "dimensionIDBlackList",
                    "",
                    "List of DimensionIDs where InfernalMobs will NEVER spawn")
                .getString(),
            this.dimensionBlackList);

        for (ModifierLoader<?> loader : modifierLoaders) {
            loader.loadConfig(config);
        }

        if (config.hasChanged()) config.save();
    }

    private void parseIDsForList(String dimensionIDs, ArrayList<Integer> list) {
        dimensionIDs = dimensionIDs.trim();
        for (String s : dimensionIDs.split(",")) {
            String trimmedDimIDString = s.trim();
            if (s.isEmpty()) continue; // Skipping empty entries if list is empty at all

            try {
                Integer tDimID = Integer.parseInt(trimmedDimIDString);
                list.add(tDimID);
                FMLLog.log(
                    "InfernalMobs",
                    Level.INFO,
                    String.format("DimensionID %d is now Blacklisted for InfernalMobs spawn", tDimID));
            } catch (Exception e) {
                FMLLog.log(
                    "InfernalMobs",
                    Level.ERROR,
                    String.format(
                        "Configured DimensionID %s is not an integer! All values must be numeric. Ignoring entry",
                        trimmedDimIDString));
            }
        }
    }

    private void parseItemsForList(String itemIDs, ArrayList<ItemStack> list) {
        Random rand = new Random();
        itemIDs = itemIDs.trim();
        for (String s : itemIDs.split(",")) {
            String[] meta = s.split("-");

            Object itemOrBlock = parseOrFind(meta[0]);
            if (itemOrBlock != null) {
                int imeta = (meta.length > 1) ? Integer.parseInt(meta[1]) : 0;
                int stackSize = (meta.length > 2) ? Integer.parseInt(meta[2]) : 1;
                int randomizer = (meta.length > 3) ? Integer.parseInt(meta[3]) + 1 : 1;
                if (randomizer < 1) {
                    randomizer = 1;
                }

                if (itemOrBlock instanceof Block) {
                    list.add(new ItemStack(((Block) itemOrBlock), stackSize + rand.nextInt(randomizer), imeta));
                } else {
                    list.add(new ItemStack(((Item) itemOrBlock), stackSize + rand.nextInt(randomizer), imeta));
                }
            }
        }
    }

    private Object parseOrFind(String s) {
        Item item = GameData.getItemRegistry()
            .getObject(s);
        if (item != null) {
            return item;
        }

        Block block = GameData.getBlockRegistry()
            .getObject(s);
        if (block != Blocks.air) {
            return block;
        }
        return null;
    }

    /**
     * Called when an Entity is spawned by natural (Biome Spawning) means, turn them into Elites here
     *
     * @param entity Entity in question
     */
    public void processEntitySpawn(EntityLivingBase entity) {
        if (!entity.worldObj.isRemote) {
            if (!getIsRareEntity(entity)) {
                if (isClassAllowed(entity)
                    && (checkEntityClassForced(entity) || entity.worldObj.rand.nextInt(eliteRarity) == 0)) {
                    try {
                        Integer tEntityDim = entity.dimension;

                        // Skip Infernal-Spawn when Dimension is Blacklisted
                        if (dimensionBlackList.contains(tEntityDim)) {
                            // System.out.println("InfernalMobsCore skipped spawning InfernalMob due blacklisted
                            // Dimension");
                        } else {
                            MobModifier mod = createMobModifiers(entity);
                            if (mod != null) {
                                addEntityModifiers(entity, mod, false);
                                // System.out.println("InfernalMobsCore modded mob: "+entity+", id
                                // "+entity.getEntityId()+": "+mod.getLinkedModName());
                            }
                        }
                    } catch (Exception e) {
                        FMLLog.log("InfernalMobs", Level.ERROR, "processEntitySpawn() threw an exception");
                        FMLLog.severe(e.getMessage());
                    }
                }
            }
        }
    }

    public boolean isClassAllowed(EntityLivingBase entity) {
        if ((entity instanceof IMob)) {
            if (entity instanceof IEntityOwnable) {
                return false;
            }
            return checkEntityClassAllowed(entity);
        }
        return false;
    }

    private String getEntityNameSafe(Entity entity) {
        String result;
        try {
            result = EntityList.getEntityString(entity);
        } catch (Exception e) {
            result = entity.getClass()
                .getSimpleName();
            FMLLog.log(
                "InfernalMobs",
                Level.INFO,
                String.format(
                    "Entity of class %s crashed when EntityList.getEntityString was queried, for shame! Using classname instead. If this message is spamming too much for your taste set useSimpleEntityClassnames true in your Infernal Mobs config",
                    result));
        }
        return result;
    }

    private boolean checkEntityClassAllowed(EntityLivingBase entity) {
        String entName = useSimpleEntityClassNames ? entity.getClass()
            .getSimpleName() : getEntityNameSafe(entity);
        if (classesAllowedMap.containsKey(entName)) {
            return classesAllowedMap.get(entName);
        }

        boolean result = config.get("permittedentities", entName, true)
            .getBoolean(true);
        if (config.hasChanged()) config.save();
        classesAllowedMap.put(entName, result);

        return result;
    }

    public boolean checkEntityClassForced(EntityLivingBase entity) {
        String entName = useSimpleEntityClassNames ? entity.getClass()
            .getSimpleName() : getEntityNameSafe(entity);
        if (classesForcedMap.containsKey(entName)) {
            return classesForcedMap.get(entName);
        }

        boolean result = config.get("entitiesalwaysinfernal", entName, false)
            .getBoolean(false);
        if (config.hasChanged()) config.save();
        classesForcedMap.put(entName, result);

        return result;
    }

    public float getMobClassMaxHealth(EntityLivingBase entity) {
        String entName = useSimpleEntityClassNames ? entity.getClass()
            .getSimpleName() : getEntityNameSafe(entity);
        if (classesHealthMap.containsKey(entName)) {
            return classesHealthMap.get(entName);
        }

        float result = (float) config.get("entitybasehealth", entName, entity.getMaxHealth())
            .getDouble(entity.getMaxHealth());
        if (config.hasChanged()) config.save();
        classesHealthMap.put(entName, result);

        return result;
    }

    /**
     * Allows setting Entity Health past the hardcoded getMaxHealth() constraint
     *
     * @param entity Entity instance whose health you want changed
     * @param amount value to set
     */
    public void setEntityHealthPastMax(EntityLivingBase entity, float amount) {
        entity.setHealth(amount);
        this.sendHealthPacket(entity, amount);
    }

    /**
     * Decides on what, if any, of the possible Modifications to apply to the Entity
     *
     * @param entity Target Entity
     * @return null or the first linked MobModifier instance for the Entity
     */
    @SuppressWarnings("unchecked")
    public MobModifier createMobModifiers(EntityLivingBase entity) {
        /* lets just be lazy and scratch mods off a list copy */
        ArrayList<ModifierLoader<?>> possibleMods = (ArrayList<ModifierLoader<?>>) modifierLoaders.clone();

        int minModifiers = minEliteModifiers;
        int maxModifiers = maxEliteModifiers;
        if (entity.worldObj.rand.nextInt(ultraRarity) == 0) // ultra mobs
        {
            minModifiers = minUltraModifiers;
            maxModifiers = maxUltraModifiers;
            if (entity.worldObj.rand.nextInt(infernoRarity) == 0) // infernal mobs
            {
                minModifiers = minInfernoModifiers;
                maxModifiers = maxInfernoModifiers;
            }
        }
        int number = Math.min(minModifiers, maxModifiers) + entity.worldObj.rand
            .nextInt((Math.max(minModifiers, maxModifiers) - Math.min(minModifiers, maxModifiers)) + 1);

        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) // so long we need more
        // and have some
        {
            /* random index of mod list */
            int index = entity.worldObj.rand.nextInt(possibleMods.size());

            MobModifier nextMod = possibleMods.get(index)
                .make(lastMod);

            boolean allowed = true;
            if (nextMod.getBlackListMobClasses() != null) {
                for (Class<?> cl : nextMod.getBlackListMobClasses()) {
                    if (entity.getClass()
                        .isAssignableFrom(cl)) {
                        allowed = false;
                        break;
                    }
                }
            }
            if (lastMod != null) {
                if (lastMod.getModsNotToMixWith() != null) {
                    for (Class<?> cl : lastMod.getModsNotToMixWith()) {
                        if (lastMod.containsModifierClass(cl)) {
                            allowed = false;
                            break;
                        }
                    }
                }
            }

            /* scratch mod off list */
            possibleMods.remove(index);

            if (allowed) // so can we use it?
            {
                // link it, note that we need one less, repeat
                lastMod = nextMod;
                number--;
            }
        }

        return lastMod;
    }

    public void addEntityModifiers(EntityLivingBase entity, MobModifier mod, boolean isHealthHacked) {
        if (mod != null) {
            proxy.getRareMobs()
                .put(entity, mod);
            mod.onSpawningComplete(entity);
            if (isHealthHacked) {
                mod.setHealthAlreadyHacked(entity);
            }
        }
    }

    /**
     * Converts a String to MobModifier instances and connects them to an Entity
     *
     * @param entity    Target Entity
     * @param savedMods String depicting the MobModifiers, equal to the ingame Display
     */
    public void addEntityModifiersByString(EntityLivingBase entity, String savedMods) {
        if (!getIsRareEntity(entity)) {
            MobModifier mod = stringToMobModifiers(savedMods);
            if (mod != null) {
                addEntityModifiers(entity, mod, true);
            } else {
                FMLLog.log(
                    "InfernalMobs",
                    Level.DEBUG,
                    String.format("Infernal Mobs error, could not instantiate modifier(s) %s", savedMods));
            }
        }
    }

    private MobModifier stringToMobModifiers(String buffer) {
        MobModifier lastMod = null;

        String[] tokens = buffer.split("\\s");
        for (int j = tokens.length - 1; j >= 0; j--) {
            String modName = tokens[j];

            MobModifier nextMod;
            for (ModifierLoader<?> loader : modifierLoaders) {
                nextMod = loader.make(lastMod);

                if (nextMod.getModName()
                    .equalsIgnoreCase(modName)) {
                    /*
                     * Only actually keep the new linked instance if it's what we wanted
                     */
                    lastMod = nextMod;
                    break;
                }
            }
        }

        return lastMod;
    }

    public static MobModifier getMobModifiers(EntityLivingBase ent) {
        return proxy.getRareMobs()
            .get(ent);
    }

    public static boolean getIsRareEntity(EntityLivingBase ent) {
        return proxy.getRareMobs()
            .containsKey(ent);
    }

    public static void removeEntFromElites(EntityLivingBase entity) {
        proxy.getRareMobs()
            .remove(entity);
    }

    /**
     * Used by the client side to answer to a server packet carrying the Entity ID and mod string
     *
     * @param world World the client is in, and the Entity aswell
     * @param entID unique Entity ID
     * @param mods  MobModifier compliant data String from the server
     */
    public void addRemoteEntityModifiers(World world, int entID, String mods) {
        Entity ent = world.getEntityByID(entID);
        if (ent != null) {
            addEntityModifiersByString((EntityLivingBase) ent, mods);
            // System.out.println("Client added remote infernal mod on entity "+ent+", is now "+mod.getModName());
        }
    }

    public void dropLootForEnt(EntityLivingBase mob, MobModifier mods) {
        int xpValue = 25;
        while (xpValue > 0) {
            int xpDrop = EntityXPOrb.getXPSplit(xpValue);
            xpValue -= xpDrop;
            mob.worldObj.spawnEntityInWorld(new EntityXPOrb(mob.worldObj, mob.posX, mob.posY, mob.posZ, xpDrop));
        }

        dropRandomEnchantedItems(mob, mods);
    }

    private void dropRandomEnchantedItems(EntityLivingBase mob, MobModifier mods) {
        int modStr = mods.getModSize();
        /* 0 for elite, 1 for ultra, 2 for infernal */
        int prefix = (modStr <= 5) ? 0 : (modStr <= 10) ? 1 : 2;
        while (modStr > 0) {
            ItemStack itemStack = getRandomItem(mob, prefix);
            if (itemStack != null) {
                Item item = itemStack.getItem();
                if (item != null) {
                    if (item instanceof ItemEnchantedBook) {
                        itemStack = ((ItemEnchantedBook) item).func_92114_b(mob.getRNG()).theItemId;
                    } else {
                        int usedStr = (modStr - 5 > 0) ? 5 : modStr;
                        enchantRandomly(mob.worldObj.rand, itemStack, item.getItemEnchantability(), usedStr);
                        // EnchantmentHelper.addRandomEnchantment(mob.worldObj.rand,
                        // itemStack, item.getItemEnchantability());
                    }
                }
                EntityItem itemEnt = new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, itemStack);
                mob.worldObj.spawnEntityInWorld(itemEnt);
                modStr -= 5;
            } else {
                // fixes issue with empty drop lists
                modStr--;
            }
        }
    }

    /**
     * Custom Enchanting Helper
     *
     * @param rand               Random gen to use
     * @param itemStack          ItemStack to be enchanted
     * @param itemEnchantability ItemStack max enchantability level
     * @param modStr             MobModifier strength to be used. Should be in range 2-5
     */
    private void enchantRandomly(Random rand, ItemStack itemStack, int itemEnchantability, int modStr) {
        int remainStr = (modStr + 1) / 2; // should result in 1-3
        List<?> enchantments = EnchantmentHelper.buildEnchantmentList(rand, itemStack, itemEnchantability);
        if (enchantments != null) {
            Iterator<?> iter = enchantments.iterator();
            while (iter.hasNext() && remainStr > 0) {
                remainStr--;
                EnchantmentData eData = (EnchantmentData) iter.next();
                itemStack.addEnchantment(eData.enchantmentobj, eData.enchantmentLevel);
            }
        }
    }

    /**
     * @param mob    Infernal Entity
     * @param prefix 0 for Elite rarity, 1 for Ultra and 2 for Infernal
     * @return ItemStack instance to drop to the World
     */
    private ItemStack getRandomItem(EntityLivingBase mob, int prefix) {
        ArrayList<ItemStack> list = (prefix == 0) ? dropIdListElite
            : (prefix == 1) ? dropIdListUltra : dropIdListInfernal;
        return !list.isEmpty() ? list.get(mob.worldObj.rand.nextInt(list.size()))
            .copy() : null;
    }

    public void sendVelocityPacket(EntityPlayerMP target, float xVel, float yVel, float zVel) {
        if (getIsEntityAllowedTarget(target)) {
            networkHelper.sendPacketToPlayer(new VelocityPacket(xVel, yVel, zVel), target);
        }
    }

    public void sendKnockBackPacket(EntityPlayerMP target, float xVel, float zVel) {
        if (getIsEntityAllowedTarget(target)) {
            networkHelper.sendPacketToPlayer(new KnockBackPacket(xVel, zVel), target);
        }
    }

    public void sendHealthPacket(EntityLivingBase mob, float health) {
        networkHelper.sendPacketToAllAroundPoint(
            new HealthPacket("", mob.getEntityId(), mob.getHealth(), mob.getMaxHealth()),
            new TargetPoint(mob.dimension, mob.posX, mob.posY, mob.posZ, 32d));
    }

    public void sendHealthRequestPacket(EntityLivingBase mob) {
        networkHelper.sendPacketToServer(
            new HealthPacket(
                FMLClientHandler.instance()
                    .getClient().thePlayer.getGameProfile()
                        .getName(),
                mob.getEntityId(),
                0f,
                0f));
    }

    public void sendAirPacket(EntityPlayerMP target, int lastAir) {
        if (getIsEntityAllowedTarget(target)) {
            networkHelper.sendPacketToPlayer(new AirPacket(lastAir), target);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent tick) {
        if (System.currentTimeMillis() > nextExistCheckTime) {
            nextExistCheckTime = System.currentTimeMillis() + existCheckDelay;
            Map<EntityLivingBase, MobModifier> mobsmap = InfernalMobsCore.proxy.getRareMobs();
            for (EntityLivingBase mob : mobsmap.keySet()) {
                if (mob.isDead || !mob.worldObj.loadedEntityList.contains(mob)) {
                    // System.out.println("Removed unloaded Entity "+mob+" with ID "+mob.getEntityId()+" from
                    // rareMobs");
                    removeEntFromElites(mob);
                }
            }

            resetModifiedPlayerEntitiesAsNeeded(tick.world);
        }

        if (!tick.world.isRemote) {
            infCheckA = null;
            infCheckB = null;
        }
    }

    private void resetModifiedPlayerEntitiesAsNeeded(World world) {
        Iterator<Entry<UUID, Long>> iterator = modifiedPlayerTimes.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            Entry<UUID, Long> entry = iterator.next();
            if (System.currentTimeMillis() > entry.getValue() + (existCheckDelay * 2)) {
                UUID id = entry.getKey();
                for (Object p : world.playerEntities) {
                    if (p instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) p;
                        if (player.getUniqueID()
                            .equals(id)) {
                            for (ModifierLoader<?> loader : modifierLoaders) {
                                MobModifier modifier = loader.make(null);
                                modifier.resetModifiedVictim(player);
                            }
                        }
                    }
                }

                iterator.remove();
            }
        }
    }

    public boolean getIsHealthBarDisabled() {
        return disableHealthBar;
    }

    public double getMobModHealthFactor() {
        return modHealthFactor;
    }

    public boolean getIsEntityAllowedTarget(Entity entity) {
        return !(entity instanceof FakePlayer);
    }

    /**
     * By caching the last reflection pairing we make sure it doesn't trigger more than once (reflections battling each
     * other, infinite loop, crash)
     *
     * @return true when inf loop is suspected, false otherwise
     */
    public boolean isInfiniteLoop(EntityLivingBase mob, Entity entity) {
        if ((mob == infCheckA && entity == infCheckB) || (mob == infCheckB && entity == infCheckA)) {
            return true;
        }
        infCheckA = mob;
        infCheckB = entity;
        return false;
    }

    /**
     * add modified player entities to this map with the current time. a timer will call a reset on the players to the
     * modifier class. do not remove players from here in a modifier as aliasing may occur (different mods using this at
     * the same time)
     */
    public HashMap<UUID, Long> getModifiedPlayerTimes() {
        return modifiedPlayerTimes;
    }

    public ArrayList<Integer> getDimensionBlackList() {
        return dimensionBlackList;
    }

    public ArrayList<ItemStack> getDropIdListElite() {
        return dropIdListElite;
    }

    public ArrayList<ItemStack> getDropIdListUltra() {
        return dropIdListUltra;
    }

    public ArrayList<ItemStack> getDropIdListInfernal() {
        return dropIdListInfernal;
    }

    public HashMap<String, Boolean> getClassesAllowedMap() {
        return classesAllowedMap;
    }

    public HashMap<String, Boolean> getClassesForcedMap() {
        return classesForcedMap;
    }

    public HashMap<String, Float> getClassesHealthMap() {
        return classesHealthMap;
    }

    public boolean isUseSimpleEntityClassNames() {
        return useSimpleEntityClassNames;
    }

    public boolean isDisableHealthBar() {
        return disableHealthBar;
    }

    public double getModHealthFactor() {
        return modHealthFactor;
    }

    public ArrayList<ModifierLoader<?>> getModifierLoaders() {
        return modifierLoaders;
    }

    public int getEliteRarity() {
        return eliteRarity;
    }

    public int getUltraRarity() {
        return ultraRarity;
    }

    public int getInfernoRarity() {
        return infernoRarity;
    }

    public int getMinEliteModifiers() {
        return minEliteModifiers;
    }

    public int getMaxEliteModifiers() {
        return maxEliteModifiers;
    }

    public int getMinUltraModifiers() {
        return minUltraModifiers;
    }

    public int getMaxUltraModifiers() {
        return maxUltraModifiers;
    }

    public int getMinInfernoModifiers() {
        return minInfernoModifiers;
    }

    public int getMaxInfernoModifiers() {
        return maxInfernoModifiers;
    }
}
