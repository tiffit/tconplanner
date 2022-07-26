package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import java.util.ArrayList;
import java.util.List;

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
            if(!result.hasError())result = cloned.validate();
            if(result.hasError())errorText = result.getMessage().copy().withStyle(TextFormatting.DARK_RED);
        }
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.stack, x, y);
        int right = x + width;
        int bottom = y + height;
        if(selected)Screen.fill(stack, x, y, right, bottom, 0x55_00_ff_00);
        if(errorText != null){
            Screen.fill(stack, x, y, right, bottom, 0x55_ff_00_00);
        }
        if(isHovered){
            Screen.fill(stack, x, y, right, bottom, 0x80_ffea00);
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack ms, int mouseX, int mouseY) {
        if(errorText == null) {
            parent.postRenderTasks.add(() -> {
                List<ITextComponent> tooltip = new ArrayList<>();
                if(Screen.hasControlDown() && stack.getItem() instanceof ToolPartItem){
                    ToolPartItem part = (ToolPartItem)stack.getItem();
                    tooltip.add(part.getName(stack));
                    List<ModifierEntry> entries = MaterialRegistry.getInstance().getTraits(material.getIdentifier(), part.getStatType());
                    for (ModifierEntry entry : entries) {
                        Modifier modifier = entry.getModifier();
                        tooltip.add(new StringTextComponent("").append(modifier.getDisplayName(entry.getLevel())).withStyle(TextFormatting.UNDERLINE));
                        Color c = Color.fromRgb(modifier.getColor());
                        for (ITextComponent comp : modifier.getDescriptionList(entry.getLevel())) {
                            tooltip.add(new StringTextComponent("").append(comp).withStyle(Style.EMPTY.withColor(c)));
                        }
                    }
                }else{
                    tooltip.addAll(stack.getTooltipLines(parent.getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL));
                    if(!Screen.hasControlDown()){
                        tooltip.add(TranslationUtil.createComponent("parts.modifier_descriptions", TConstruct.makeTranslation("key", "ctrl").withStyle(TextFormatting.AQUA, TextFormatting.ITALIC)));
                    }
                }
                parent.renderWrappedToolTip(ms, tooltip, mouseX, mouseY);
            });
        }else {
            parent.postRenderTasks.add(() -> parent.renderTooltip(ms, errorText, mouseX, mouseY));
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
