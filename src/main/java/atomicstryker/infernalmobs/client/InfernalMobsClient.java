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
    private ConcurrentHashMap<EntityLivingBase, MobModifier> rareMobsClient;
    private int airOverrideValue = -999;
    private long airDisplayTimeout;

    private long healthBarRetainTime;
    private EntityLivingBase retainedTarget;

    @Override
    public void preInit() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        mc = FMLClientHandler.instance()
            .getClient();
    }

    @Override
    public void load() {
        nextPacketTime = 0;
        rareMobsClient = new ConcurrentHashMap<>();

        MinecraftForge.EVENT_BUS.register(new RendererBossGlow());
        MinecraftForge.EVENT_BUS.register(this);

        healthBarRetainTime = 0;
        retainedTarget = null;
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
            || (BossStatus.bossName != null && BossStatus.statusBarTime > 0)) {
            return;
        }

        Entity ent = getEntityCrosshairOver(event.partialTicks, mc);
        boolean retained = false;

        if (ent == null && System.currentTimeMillis() < healthBarRetainTime) {
            ent = retainedTarget;
            retained = true;
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
                    healthBarRetainTime = System.currentTimeMillis() + 3000L;
                }

            }
        }
    }

    private Entity getEntityCrosshairOver(float renderTick, Minecraft mc) {
        Entity returnedEntity = null;

        if (mc.renderViewEntity != null) {
            if (mc.theWorld != null) {
                double reachDistance = NAME_VISION_DISTANCE;
                final MovingObjectPosition mopos = mc.renderViewEntity.rayTrace(reachDistance, renderTick);
                double reachDist2 = reachDistance;
                final Vec3 viewEntPositionVec = mc.renderViewEntity.getPosition(renderTick);

                if (mopos != null) {
                    reachDist2 = mopos.hitVec.distanceTo(viewEntPositionVec);
                }

                final Vec3 viewEntityLookVec = mc.renderViewEntity.getLook(renderTick);
                final Vec3 actualReachVector = viewEntPositionVec.addVector(
                    viewEntityLookVec.xCoord * reachDistance,
                    viewEntityLookVec.yCoord * reachDistance,
                    viewEntityLookVec.zCoord * reachDistance);
                float expandBBvalue = 1.0F;
                double lowestDistance = reachDist2;
                Entity iterEnt;
                Entity pointedEntity = null;
                for (Object obj : mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                    mc.renderViewEntity,
                    mc.renderViewEntity.boundingBox
                        .addCoord(
                            viewEntityLookVec.xCoord * reachDistance,
                            viewEntityLookVec.yCoord * reachDistance,
                            viewEntityLookVec.zCoord * reachDistance)
                        .expand(expandBBvalue, expandBBvalue, expandBBvalue))) {
                    iterEnt = (Entity) obj;
                    if (iterEnt.canBeCollidedWith()) {
                        float entBorderSize = iterEnt.getCollisionBorderSize();
                        AxisAlignedBB entHitBox = iterEnt.boundingBox
                            .expand(entBorderSize, entBorderSize, entBorderSize);
                        MovingObjectPosition interceptObjectPosition = entHitBox
                            .calculateIntercept(viewEntPositionVec, actualReachVector);

                        if (entHitBox.isVecInside(viewEntPositionVec)) {
                            if (0.0D < lowestDistance || lowestDistance == 0.0D) {
                                pointedEntity = iterEnt;
                                lowestDistance = 0.0D;
                            }
                        } else if (interceptObjectPosition != null) {
                            double distanceToEnt = viewEntPositionVec.distanceTo(interceptObjectPosition.hitVec);

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
        }

        return returnedEntity;
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
