package net.tiffit.tconplanner.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.PlannerScreen;

import java.util.function.Consumer;

public class SliderWidget extends Widget {

    private final PlannerScreen parent;
    private final Consumer<Integer> listener;
    private final int min, max;
    private double percent;
    private int value;

    public SliderWidget(int x, int y, int width, int height, Consumer<Integer> listener, int min, int max, int value, PlannerScreen parent) {
        super(x, y, width, height, new StringTextComponent(""));
        this.parent = parent;
        this.listener = listener;
        this.min = min;
        this.max = max;
        this.value = value;
        percent = (value - min)/(double)(max-min);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        int center = y + height/2;
        RenderSystem.enableBlend();
        Screen.fill(stack, x, center-2, x + width, center+2, 0x50_f0_f0_f0);
        int sliderX = x + (int)(width*percent);
        Screen.fill(stack, sliderX - 2, y, sliderX + 2, y + height, 0x50_f0_f0_f0);
        FontRenderer font = Minecraft.getInstance().font;
        int minValSize = font.width(min + "");
        drawString(stack, font, min + "", x - minValSize - 5, y + 6, 0xff_ff_ff_ff);
        drawString(stack, font, max + "", x + width + 5, y + 6, 0xff_ff_ff_ff);
        drawCenteredString(stack, font, value + "", sliderX, y + 22, 0xff_ff_ff_ff);
    }

    @Override
    public void onClick(double mx, double my) {
        updateVal(mx);
    }

    @Override
    protected void onDrag(double mx1, double my1, double mx2, double my2) {
        updateVal(mx1);
    }

    private void updateVal(double mouseX){
        percent = (mouseX - x)/width;
        int oldVal = value;
        value = (int)MathHelper.clamp((max-min)*percent + min, min, max);
        if(value != oldVal)listener.accept(value);
    }
}
