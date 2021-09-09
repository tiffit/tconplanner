package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.tiffit.tconplanner.screen.PlannerScreen;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;

public class ToolTypeButton extends Button {

    private final SlotInformation info;
    private final boolean selected;
    public final int index;
    private final PlannerScreen parent;

    public ToolTypeButton(int index, SlotInformation info, PlannerScreen parent) {
        super(0, 0, 18, 18, info.getItem().getDescription(), button -> parent.setSelectedTool(index));
        this.info = info;
        this.index = index;
        this.parent = parent;
        this.selected = parent.blueprint != null && info == parent.blueprint.toolSlotInfo;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        parent.blit(stack, x, y, 213, 41 + (selected ? 18 : 0), 18, 18);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(info.getToolForRendering(), x + 1, y + 1);
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(stack, info.getToolForRendering(), mouseX, mouseY));
    }
}
