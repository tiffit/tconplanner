package net.tiffit.tconplanner.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.tiffit.tconplanner.PlannerScreen;
import slimeknights.tconstruct.library.materials.definition.IMaterial;

public class MaterialButton extends Button {

    public final IMaterial material;
    public final ItemStack stack;
    private final PlannerScreen parent;
    public boolean selected = false;

    public MaterialButton(int index, IMaterial material, ItemStack stack, int x, int y, PlannerScreen parent){
        super(x, y, 16, 16, stack.getHoverName(), button -> parent.setPart(material));
        this.material = material;
        this.stack = stack;
        this.parent = parent;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.stack, x, y);
        if(selected)Screen.fill(stack, x, y, x + width, y + height, 0x55_00_ff_00);
        if(isHovered){
            Screen.fill(stack, x, y, x + width, y + height, 0x80_ffea00);
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.renderItemTooltip(stack, this.stack, mouseX, mouseY);
    }
}
