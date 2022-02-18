package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;

public class TextButton extends Button {

    private final PlannerScreen parent;
    private final Runnable onPress;
    private int color = 0xff_ff_ff;
    private ITextComponent tooltip = null;

    public TextButton(int x, int y, ITextComponent text, Runnable onPress, PlannerScreen parent) {
        super(x, y, 58, 18, text, e -> {});
        this.parent = parent;
        this.onPress = onPress;
    }

    public TextButton withColor(int color){
        this.color = color;
        return this;
    }

    public TextButton withTooltip(ITextComponent tooltip){
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        RenderSystem.enableBlend();
        PlannerScreen.bindTexture();
        RenderSystem.color4f(((color & 0xff0000) >> 16)/255f, ((color & 0x00ff00) >> 8)/255f, (color & 0x0000ff)/255f,1f);
        parent.blit(stack, x, y, 176, 183, width, height);
        Screen.drawCenteredString(stack, Minecraft.getInstance().font, getMessage(), x + width/2, y + 5, isHovered ? 0xffffffff : 0xa0ffffff);
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        if(tooltip != null) {
            parent.postRenderTasks.add(() -> parent.renderTooltip(stack, tooltip, mouseX, mouseY));
        }
    }

    @Override
    public void onPress() {
        onPress.run();
    }
}
