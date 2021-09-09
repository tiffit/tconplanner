package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class OutputToolWidget extends Widget {

    private final ItemStack stack;
    private final PlannerScreen parent;

    public OutputToolWidget(int x, int y, ItemStack stack, PlannerScreen parent){
        super(x, y, 16, 16, new StringTextComponent(""));
        this.parent = parent;
        this.stack = stack;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        parent.blit(stack, x - 6, y - 6, 176, 117, 28, 28);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.stack, x, y);
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(stack, this.stack, mouseX, mouseY));
    }

    @Override
    public void playDownSound(SoundHandler sound) {}
}
