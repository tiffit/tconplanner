package net.tiffit.tconplanner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EventListener {
    private static final int width = 12, height = 12;

    @SubscribeEvent
    public static void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Post e){
        if(e.getGui() instanceof TinkerStationScreen) {
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            int x = screen.cornerX + Config.CONFIG.buttonX.get(), y = screen.cornerY + Config.CONFIG.buttonY.get();
            int mx = e.getMouseX(), my = e.getMouseY();
            MatrixStack stack = e.getMatrixStack();
            PlannerScreen.bindTexture();
            boolean hovered = mx > x && mx < x + width && my > y && my < y + height;
            RenderSystem.enableBlend();
            if(!hovered)RenderSystem.color4f(1F, 1.0F, 1.0F, 0.5F);
            screen.blit(stack, x, y, 176, 78, 12, 12);
            if(hovered){
                screen.renderTooltip(stack, TranslationUtil.createComponent("plannerbutton"), mx, my);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenMouseClick(GuiScreenEvent.MouseClickedEvent.Pre e){
        if(e.getGui() instanceof TinkerStationScreen) {
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            int x = screen.cornerX + Config.CONFIG.buttonX.get(), y = screen.cornerY + Config.CONFIG.buttonY.get();
            int mx = (int)e.getMouseX(), my = (int)e.getMouseY();
            if(mx > x && mx < x + width && my > y && my < y + height){
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                mc.setScreen(new PlannerScreen(screen));
                e.setCanceled(true);
            }
        }
    }

}
