package atomicstryker.infernalmobs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Lets players move, resize and hide the elite mob health bar in Dashboard's HUD editor. The only class using
 * Dashboard, so only call it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardHud {

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int NAME_TOP = 1;
    private static final int BAR_TOP = 12;
    private static final int MODIFIERS_TOP = 19;
    private static final int TEXT_HEIGHT = 8;

    /** Sized for one line of modifiers, which most mobs have. */
    private static final HudElement HEALTH_BAR = new HudElement(
        "infernalmobs:health_bar",
        (screenWidth, screenHeight) -> new HudBounds(
            screenWidth / 2 - BAR_WIDTH / 2,
            NAME_TOP,
            BAR_WIDTH,
            MODIFIERS_TOP + TEXT_HEIGHT - NAME_TOP));

    private DashboardHud() {}

    public static void register() {
        HEALTH_BAR.setPreview(
            (screenWidth, screenHeight) -> HudRegistry
                .render(HEALTH_BAR, () -> drawPreview(screenWidth, screenHeight)));
        HudRegistry.register(HEALTH_BAR);
    }

    /** Vanilla's boss bar two thirds full, with a rare mob's name and modifiers, like in game. */
    private static void drawPreview(int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        HudBounds bounds = HEALTH_BAR.getDefaultBounds(screenWidth, screenHeight);
        mc.getTextureManager()
            .bindTexture(Gui.icons);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.ingameGUI.drawTexturedModalRect(bounds.x, BAR_TOP, 0, 74, BAR_WIDTH, BAR_HEIGHT);
        mc.ingameGUI.drawTexturedModalRect(bounds.x, BAR_TOP, 0, 79, BAR_WIDTH * 2 / 3, BAR_HEIGHT);
        int centerX = screenWidth / 2;
        drawCentered(mc.fontRenderer, "Rare draining Zombie of Repulsion", centerX, NAME_TOP, 0xFFFF55);
        drawCentered(mc.fontRenderer, "Fiery Weakness Exhaust Gravity", centerX, MODIFIERS_TOP, 0xFFFFFF);
    }

    private static void drawCentered(FontRenderer font, String text, int centerX, int y, int color) {
        font.drawStringWithShadow(text, centerX - font.getStringWidth(text) / 2, y, color);
    }
}
