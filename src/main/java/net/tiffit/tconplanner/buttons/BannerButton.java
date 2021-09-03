package net.tiffit.tconplanner.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.tiffit.tconplanner.PlannerScreen;

public class BannerButton extends Widget {

    private final PlannerScreen parent;

    public BannerButton(int x, int y, ITextComponent text, PlannerScreen parent) {
        super(x, y, 90, 19, text);
        this.parent = parent;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        parent.blit(stack, x, y, 0, 205, width, height);
        drawCenteredString(stack, Minecraft.getInstance().font, getMessage(), x + width/2, y + 5, 0xff_90_90_ff);
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {}
}
