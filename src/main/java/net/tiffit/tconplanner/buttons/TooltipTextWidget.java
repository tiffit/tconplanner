package net.tiffit.tconplanner.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.util.TextPosEnum;

import java.util.Collections;
import java.util.List;

public class TooltipTextWidget extends Widget {

    private final PlannerScreen parent;
    private int color = 0xff_ff_ff_ff;
    private final FontRenderer font;
    private final List<ITextComponent> tooltip;

    public TooltipTextWidget(int x, int y, ITextComponent text, ITextComponent tooltip, PlannerScreen parent) {
        this(x, y, TextPosEnum.LEFT, text, tooltip, parent);
    }

    public TooltipTextWidget(int x, int y, TextPosEnum pos, ITextComponent text, ITextComponent tooltip, PlannerScreen parent) {
        this(x, y, pos, text, Collections.singletonList(tooltip), parent);
    }

    public TooltipTextWidget(int x, int y, TextPosEnum pos, ITextComponent text, List<ITextComponent> tooltip, PlannerScreen parent) {
        super(x, y, 0, 0, text);
        this.parent = parent;
        this.tooltip = tooltip;
        font = Minecraft.getInstance().font;
        setWidth(font.width(text));
        setHeight(font.lineHeight);
        if(pos == TextPosEnum.CENTER){
            this.x -= getWidth()/2;
        }
    }

    public TooltipTextWidget withColor(int color){
        this.color = color;
        return this;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        drawString(stack, font, getMessage(), x, y, color);
        if(isHovered()){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderComponentTooltip(stack, tooltip, mouseX, mouseY));
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {}
}
