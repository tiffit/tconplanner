package net.tiffit.tconplanner.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.tiffit.tconplanner.TConPlanner;

public class Icon {
    public static ResourceLocation ICONS = new ResourceLocation(TConPlanner.MODID, "textures/gui/icons.png");

    private final int x, y;

    public Icon(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void render(Screen screen, MatrixStack stack, int x, int y){
        Minecraft.getInstance().getTextureManager().bind(ICONS);
        screen.blit(stack, x, y, this.x*12, this.y*12, 12, 12);
    }
}
