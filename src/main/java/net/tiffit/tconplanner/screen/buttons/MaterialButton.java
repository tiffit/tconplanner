package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.data.Blueprint;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class MaterialButton extends Button {

    public final IMaterial material;
    public final ItemStack stack;
    private final PlannerScreen parent;
    public boolean selected = false;
    public ITextComponent errorText;

    public MaterialButton(int index, IMaterial material, ItemStack stack, int x, int y, PlannerScreen parent){
        super(x, y, 16, 16, stack.getHoverName(), button -> parent.setPart(material));
        this.material = material;
        this.stack = stack;
        this.parent = parent;
        if(parent.blueprint.isComplete()){
            Blueprint cloned = parent.blueprint.clone();
            cloned.materials[parent.selectedPart] = material;
            ValidatedResult result = ToolStack.from(cloned.createOutput()).validate();
            if(result.hasError())errorText = result.getMessage().copy().withStyle(TextFormatting.DARK_RED);
        }
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.stack, x, y);
        if(selected)Screen.fill(stack, x, y, x + width, y + height, 0x55_00_ff_00);
        if(errorText != null){
            stack.pushPose();
            stack.translate(x, y, 100);
            Screen.fill(stack, 0, 0, width, height, 0x55_ff_00_00);
            stack.popPose();
        }
        if(isHovered){
            Screen.fill(stack, x, y, x + width, y + height, 0x80_ffea00);
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        if(errorText == null) {
            parent.postRenderTasks.add(() -> parent.renderItemTooltip(stack, this.stack, mouseX, mouseY));
        }else {
            parent.postRenderTasks.add(() -> parent.renderTooltip(stack, errorText, mouseX, mouseY));
        }
    }

    @Override
    public void onPress() {
        if(errorText == null){
            parent.setPart(material);
        }
    }

    @Override
    public void playDownSound(SoundHandler sound) {
        if(errorText != null){
            sound.play(SimpleSound.forUI(SoundEvents.ANVIL_HIT, 1.0F));
        } else {
            super.playDownSound(sound);
        }
    }
}
