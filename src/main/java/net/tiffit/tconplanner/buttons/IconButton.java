package net.tiffit.tconplanner.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.tiffit.tconplanner.PlannerScreen;

import java.awt.*;

public class IconButton extends Button {

    private final int u, v;
    private final PlannerScreen parent;
    private SoundEvent pressSound = SoundEvents.UI_BUTTON_CLICK;
    private Color color = Color.WHITE;

    public IconButton(int x, int y, int u, int v, ITextComponent tooltip, PlannerScreen parent, IPressable action) {
        super(x, y, 12, 12, tooltip, action);
        this.u = u;
        this.v = v;
        this.parent = parent;
    }

    public IconButton withSound(SoundEvent sound){
        this.pressSound = sound;
        return this;
    }

    public IconButton withColor(Color color){
        this.color = color;
        return this;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.color4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, isHovered ? 1 : 0.8F);
        parent.blit(stack, x, y, u, v, width, height);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.renderTooltip(stack, this.getMessage(), mouseX, mouseY);
    }

    @Override
    public void playDownSound(SoundHandler handler) {
        if(pressSound != null)handler.play(SimpleSound.forUI(pressSound, 1.0F));
    }
}
