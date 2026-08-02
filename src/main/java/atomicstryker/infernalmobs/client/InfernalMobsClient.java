package atomicstryker.infernalmobs.client;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import atomicstryker.infernalmobs.common.ISidedProxy;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.modifiers.MM_Gravity;
import atomicstryker.infernalmobs.common.modifiers.MobModifier;
import atomicstryker.infernalmobs.common.network.packets.HealthPacket;
import atomicstryker.infernalmobs.common.network.packets.MobModsPacket;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class InfernalMobsClient implements ISidedProxy {

    private static final double NAME_VISION_DISTANCE = 32D;
    private Minecraft mc;
    private long nextPacketTime;
    private final ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsClient = new ConcurrentHashMap<>();
    private int airOverrideValue = -999;
    private long airDisplayTimeout;

    private long healthBarRetainTime;
    private EntityLivingBase retainedTarget;

    private long nextCrosshairScanTime;
    private Entity cachedCrosshairTarget;

    private final Vec3 scratchCameraPos = Vec3.createVectorHelper(0, 0, 0);
    private final Vec3 scratchCameraLook = Vec3.createVectorHelper(0, 0, 0);
    private final Vec3 scratchReachVector = Vec3.createVectorHelper(0, 0, 0);
    private final AxisAlignedBB scratchQueryAABB = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
    private final AxisAlignedBB scratchHitAABB = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

    @Override
    public void preInit() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        mc = FMLClientHandler.instance()
            .getClient();
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new RendererBossGlow());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.isRemote) {
            retainedTarget = null;
            cachedCrosshairTarget = null;
            nextCrosshairScanTime = 0L;
        }
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote && mc.thePlayer != null
            && (event.entity instanceof EntityMob
                || (event.entity instanceof EntityLivingBase && event.entity instanceof IMob))) {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(
                new MobModsPacket(
                    mc.thePlayer.getGameProfile()
                        .getName(),
                    event.entity.getEntityId(),
                    (byte) 0));
        }
    }

    private void askServerHealth(Entity ent) {
        if (System.currentTimeMillis() > nextPacketTime) {
            InfernalMobsCore.instance().networkHelper.sendPacketToServer(
                new HealthPacket(
                    mc.thePlayer.getGameProfile()
                        .getName(),
                    ent.getEntityId(),
                    0f,
                    0f));
            nextPacketTime = System.currentTimeMillis() + 100L;
        }
    }

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (InfernalMobsCore.instance()
            .getIsHealthBarDisabled() || event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH
            || (BossStatus.bossName != null && BossStatus.statusBarTime > 0) || rareMobsClient.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Entity ent;
        boolean retained = false;

        if (now < healthBarRetainTime && retainedTarget != null) {
            ent = retainedTarget;
            retained = true;
        } else if (now < nextCrosshairScanTime) {
            ent = cachedCrosshairTarget;
        } else {
            ent = getEntityCrosshairOver(event.partialTicks, mc);
            cachedCrosshairTarget = ent;
            nextCrosshairScanTime = now + 100L;
        }

        if (ent instanceof EntityLivingBase) {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent);
            if (mod != null) {
                askServerHealth(ent);

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager()
                    .bindTexture(Gui.icons);
                GL11.glDisable(GL11.GL_BLEND);

                EntityLivingBase target = (EntityLivingBase) ent;
                String buffer = mod.getEntityDisplayName(target);

                int screenwidth = event.resolution.getScaledWidth();
                FontRenderer fontR = mc.fontRenderer;

                GuiIngame gui = mc.ingameGUI;
                short lifeBarLength = 182;
                int x = screenwidth / 2 - lifeBarLength / 2;

                int lifeBarLeft = (int) (mod.getActualHealth(target) / mod.getActualMaxHealth(target)
                    * (float) (lifeBarLength + 1));
                byte y = 12;
                gui.drawTexturedModalRect(x, y, 0, 74, lifeBarLength, 5);

                if (lifeBarLeft > 0) {
                    gui.drawTexturedModalRect(x, y, 0, 79, lifeBarLeft, 5);
                }

                int yCoord = 1;
                fontR
                    .drawStringWithShadow(buffer, screenwidth / 2 - fontR.getStringWidth(buffer) / 2, yCoord, 0x2F96EB);

                // spacing for healthbar
                yCoord += 8;

                String[] display = mod.getDisplayNames();
                int i = 0;
                while (i < display.length && display[i] != null) {
                    yCoord += 10;
                    fontR.drawStringWithShadow(
                        display[i],
                        screenwidth / 2 - fontR.getStringWidth(display[i]) / 2,
                        yCoord,
                        0xffffff);
                    i++;
                }

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager()
                    .bindTexture(Gui.icons);

                if (!retained) {
                    retainedTarget = target;
                    healthBarRetainTime = now + 3000L;
                }

            }
        }
    }

    private Entity getEntityCrosshairOver(float renderTick, Minecraft mc) {
        Entity returnedEntity = null;

        if (mc.renderViewEntity != null && mc.theWorld != null) {
            computeCameraVectors(mc.renderViewEntity, renderTick, scratchCameraPos, scratchCameraLook);

            double reachDistance = NAME_VISION_DISTANCE;
            final MovingObjectPosition mopos = mc.renderViewEntity.rayTrace(reachDistance, renderTick);
            double reachDist2 = reachDistance;

            if (mopos != null) {
                reachDist2 = mopos.hitVec.distanceTo(scratchCameraPos);
            }

            double reachX = scratchCameraLook.xCoord * reachDistance;
            double reachY = scratchCameraLook.yCoord * reachDistance;
            double reachZ = scratchCameraLook.zCoord * reachDistance;
            scratchReachVector.xCoord = reachX;
            scratchReachVector.yCoord = reachY;
            scratchReachVector.zCoord = reachZ;

            float expandBBvalue = 1.0F;
            double lowestDistance = reachDist2;
            Entity iterEnt;
            Entity pointedEntity = null;

            AxisAlignedBB baseBox = mc.renderViewEntity.boundingBox;
            double qMinX = baseBox.minX;
            double qMinY = baseBox.minY;
            double qMinZ = baseBox.minZ;
            double qMaxX = baseBox.maxX;
            double qMaxY = baseBox.maxY;
            double qMaxZ = baseBox.maxZ;
            if (reachX < 0.0D) {
                qMinX += reachX;
            } else if (reachX > 0.0D) {
                qMaxX += reachX;
            }
            if (reachY < 0.0D) {
                qMinY += reachY;
            } else if (reachY > 0.0D) {
                qMaxY += reachY;
            }
            if (reachZ < 0.0D) {
                qMinZ += reachZ;
            } else if (reachZ > 0.0D) {
                qMaxZ += reachZ;
            }
            scratchQueryAABB.minX = qMinX - expandBBvalue;
            scratchQueryAABB.minY = qMinY - expandBBvalue;
            scratchQueryAABB.minZ = qMinZ - expandBBvalue;
            scratchQueryAABB.maxX = qMaxX + expandBBvalue;
            scratchQueryAABB.maxY = qMaxY + expandBBvalue;
            scratchQueryAABB.maxZ = qMaxZ + expandBBvalue;

            for (Object obj : mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.renderViewEntity, scratchQueryAABB)) {
                iterEnt = (Entity) obj;
                if (iterEnt.canBeCollidedWith()) {
                    float entBorderSize = iterEnt.getCollisionBorderSize();
                    AxisAlignedBB entHitBox = iterEnt.boundingBox;
                    scratchHitAABB.minX = entHitBox.minX - entBorderSize;
                    scratchHitAABB.minY = entHitBox.minY - entBorderSize;
                    scratchHitAABB.minZ = entHitBox.minZ - entBorderSize;
                    scratchHitAABB.maxX = entHitBox.maxX + entBorderSize;
                    scratchHitAABB.maxY = entHitBox.maxY + entBorderSize;
                    scratchHitAABB.maxZ = entHitBox.maxZ + entBorderSize;
                    MovingObjectPosition interceptObjectPosition = scratchHitAABB
                        .calculateIntercept(scratchCameraPos, scratchReachVector);

                    if (scratchHitAABB.isVecInside(scratchCameraPos)) {
                        if (0.0D < lowestDistance || lowestDistance == 0.0D) {
                            pointedEntity = iterEnt;
                            lowestDistance = 0.0D;
                        }
                    } else if (interceptObjectPosition != null) {
                        double distanceToEnt = scratchCameraPos.distanceTo(interceptObjectPosition.hitVec);

                        if (distanceToEnt < lowestDistance || lowestDistance == 0.0D) {
                            pointedEntity = iterEnt;
                            lowestDistance = distanceToEnt;
                        }
                    }
                }
            }

            if (pointedEntity != null && (lowestDistance < reachDist2 || mopos == null)) {
                returnedEntity = pointedEntity;
            }
        }

        return returnedEntity;
    }

    /**
     * Writes the interpolated camera position (eye height) and look vector of the given view entity into the two
     * reusable scratch vectors, replicating Entity#getPosition/getLook without allocating new Vec3 instances.
     */
    private static void computeCameraVectors(Entity viewEnt, float renderTick, Vec3 outPos, Vec3 outLook) {
        double camX;
        double camY;
        double camZ;
        if (renderTick == 1.0F) {
            camX = viewEnt.posX;
            camY = viewEnt.posY + viewEnt.getEyeHeight();
            camZ = viewEnt.posZ;
        } else {
            camX = viewEnt.prevPosX + (viewEnt.posX - viewEnt.prevPosX) * renderTick;
            camY = (viewEnt.prevPosY + (viewEnt.posY - viewEnt.prevPosY) * renderTick) + viewEnt.getEyeHeight();
            camZ = viewEnt.prevPosZ + (viewEnt.posZ - viewEnt.prevPosZ) * renderTick;
        }
        outPos.xCoord = camX;
        outPos.yCoord = camY;
        outPos.zCoord = camZ;

        float pitch = renderTick == 1.0F
            ? viewEnt.rotationPitch
            : viewEnt.prevRotationPitch + (viewEnt.rotationPitch - viewEnt.prevRotationPitch) * renderTick;
        float yaw = renderTick == 1.0F
            ? viewEnt.rotationYaw
            : viewEnt.prevRotationYaw + (viewEnt.rotationYaw - viewEnt.prevRotationYaw) * renderTick;

        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        outLook.xCoord = f1 * f2;
        outLook.yCoord = f3;
        outLook.zCoord = f * f2;
    }

    @Override
    public ConcurrentHashMap<EntityLivingBase, MobModifier> getRareMobs() {
        return rareMobsClient;
    }

    @Override
    public void onHealthPacketForClient(String stringData, int entID, float health, float maxhealth) {
        Entity ent = FMLClientHandler.instance()
            .getClient().theWorld.getEntityByID(entID);
        if (ent instanceof EntityLivingBase) {
            MobModifier mod = InfernalMobsCore.getMobModifiers((EntityLivingBase) ent);
            if (mod != null) {
                // System.out.printf("health packet [%f of %f] for %s\n", health, maxhealth, ent);
                mod.setActualHealth(health, maxhealth);
            }
        }
    }

    @Override
    public void onKnockBackPacket(float xv, float zv) {
        MM_Gravity.knockBack(
            FMLClientHandler.instance()
                .getClient().thePlayer,
            xv,
            zv);
    }

    @Override
    public void onMobModsPacketToClient(String stringData, int entID) {
        InfernalMobsCore.instance()
            .addRemoteEntityModifiers(
                FMLClientHandler.instance()
                    .getClient().theWorld,
                entID,
                stringData);
    }

    @Override
    public void onVelocityPacket(float xv, float yv, float zv) {
        FMLClientHandler.instance()
            .getClient().thePlayer.addVelocity(xv, yv, zv);
    }

    @Override
    public void onAirPacket(int air) {
        airOverrideValue = air;
        airDisplayTimeout = System.currentTimeMillis() + 3000L;
    }

    @SubscribeEvent
    public void onTick(RenderGameOverlayEvent.Pre event) {
        if (System.currentTimeMillis() > airDisplayTimeout) {
            airOverrideValue = -999;
        }

        if (event.type == RenderGameOverlayEvent.ElementType.AIR) {
            if (!mc.thePlayer.isInsideOfMaterial(Material.water) && airOverrideValue != -999) {
                GL11.glEnable(GL11.GL_BLEND);
                int right_height = 39;
                final int left = event.resolution.getScaledWidth() / 2 + 91;
                final int top = event.resolution.getScaledHeight() - right_height;
                final int full = MathHelper.ceiling_double_int((double) (airOverrideValue - 2) * 10.0D / 300.0D);
                final int partial = MathHelper.ceiling_double_int((double) airOverrideValue * 10.0D / 300.0D) - full;
                for (int i = 0; i < full + partial; ++i) {
                    mc.ingameGUI.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
                }
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }
}
