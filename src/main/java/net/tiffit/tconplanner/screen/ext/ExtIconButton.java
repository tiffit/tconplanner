package net.tiffit.tconplanner.screen.ext;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;

import java.awt.*;
import java.util.List;

public class ExtIconButton extends Button {

    private final Icon icon;
    private final Screen screen;
    private SoundEvent pressSound = SoundEvents.UI_BUTTON_CLICK;
    private Color color = Color.WHITE;

    public ExtIconButton(int x, int y, Icon icon, ITextComponent tooltip, IPressable action, Screen screen) {
        super(x, y, 12, 12, new StringTextComponent(""), action,
                (btn, stack, mx, my) -> screen.renderTooltip(stack, tooltip, mx, my));
        this.icon = icon;
        this.screen = screen;
    }

    public ExtIconButton withSound(SoundEvent sound){
        this.pressSound = sound;
        return this;
    }

    public ExtIconButton withColor(Color color){
        this.color = color;
        return this;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.color4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, isHovered ? 1 : 0.8F);
        icon.render(screen, stack, x, y);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if (this.isHovered()) {
            this.renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void playDownSound(SoundHandler handler) {
        if(pressSound != null)handler.play(SimpleSound.forUI(pressSound, 1.0F));
    }
}

