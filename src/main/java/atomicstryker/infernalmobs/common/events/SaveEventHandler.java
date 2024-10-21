package atomicstryker.infernalmobs.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SaveEventHandler {

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        Chunk chunk = event.getChunk();
        Entity newEnt;
        for (int i = 0; i < chunk.entityLists.length; i++) {
            for (int j = 0; j < chunk.entityLists[i].size(); j++) {
                newEnt = (Entity) chunk.entityLists[i].get(j);
                if (newEnt instanceof EntityLivingBase) {
                    /*
                     * an EntityLiving was just dumped to a save file and removed from the world
                     */
                    if (InfernalMobsCore.getIsRareEntity((EntityLivingBase) newEnt)) {
                        InfernalMobsCore.removeEntFromElites((EntityLivingBase) newEnt);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Chunk chunk = event.getChunk();
        Entity newEnt;
        for (int i = 0; i < chunk.entityLists.length; i++) {
            for (int j = 0; j < chunk.entityLists[i].size(); j++) {
                newEnt = (Entity) chunk.entityLists[i].get(j);
                if (newEnt instanceof EntityLivingBase) {
                    String savedMods = newEnt.getEntityData()
                        .getString(InfernalMobsCore.getNBTTag());
                    if (!savedMods.isEmpty()) {
                        InfernalMobsCore.instance()
                            .addEntityModifiersByString((EntityLivingBase) newEnt, savedMods);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        InfernalMobsCore.proxy.getRareMobs()
            .entrySet()
            .removeIf(entry -> entry.getKey().worldObj == event.world);
    }
}
