package atomicstryker.infernalmobs.common;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

import atomicstryker.infernalmobs.common.modifiers.MobModifier;

public class InfernalMobsServer implements ISidedProxy {

    private final ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsServer;

    public InfernalMobsServer() {
        rareMobsServer = new ConcurrentHashMap<>();
    }

    @Override
    public void preInit() {
        // NOOP
    }

    @Override
    public void load() {
        // NOOP
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs() {
        return rareMobsServer;
    }

    @Override
    public void onHealthPacketForClient(String stringData, int entID, float health, float maxhealth) {
        // NOOP
    }

    @Override
    public void onKnockBackPacket(float xv, float zv) {
        // NOOP
    }

    @Override
    public void onMobModsPacketToClient(String stringData, int entID) {
        // NOOP
    }

    @Override
    public void onVelocityPacket(float xv, float yv, float zv) {
        // NOOP
    }

    @Override
    public void onAirPacket(int air) {
        // NOOP
    }

}
