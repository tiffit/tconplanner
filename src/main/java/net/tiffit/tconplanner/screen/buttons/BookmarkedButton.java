package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;

public class BookmarkedButton extends Button {

    public static final Icon STAR_ICON = new Icon(6, 0);

    private final PlannerScreen parent;
    private final ItemStack stack;
    private final int index;
    private final Blueprint blueprint;
    private final boolean starred;
    private boolean selected;

    public BookmarkedButton(int index, Blueprint blueprint, boolean starred, PlannerScreen parent){
        super(0, 0, 18, 18, new StringTextComponent(""), button -> parent.setBlueprint(blueprint.clone()));
        this.index = index;
        this.blueprint = blueprint;
        this.starred = starred;
        this.parent = parent;
        stack = blueprint.createOutput();
        this.selected = parent.blueprint != null && parent.blueprint.equals(blueprint);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        RenderSystem.disableRescaleNormal();
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        parent.blit(stack, x, y, 213, 41 + (selected ? 18 : 0), 18, 18);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.renderGuiItem(this.stack, x + 1, y + 1);
        if(starred){
            stack.pushPose();
            stack.translate(x + 11, y + 11, 105);
            stack.scale(0.5f, 0.5f, 0.5f);
            STAR_ICON.render(parent, stack, 0, 0);
            stack.popPose();
        }
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(stack, this.stack, mouseX, mouseY));
    }
}
