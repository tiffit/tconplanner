package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class OutputToolWidget extends AbstractWidget {

    private final ItemStack stack;
    private final PlannerScreen parent;

    public OutputToolWidget(int x, int y, ItemStack stack, PlannerScreen parent){
        super(x, y, 16, 16, new TextComponent(""));
        this.parent = parent;
        this.stack = stack;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        parent.blit(stack, x - 6, y - 6, 176, 117, 28, 28);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.stack, x, y);
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(PoseStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(stack, this.stack, mouseX, mouseY));
    }

    @Override
    public void playDownSound(SoundManager sound) {}

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
