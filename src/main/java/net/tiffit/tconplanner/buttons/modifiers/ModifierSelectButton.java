package net.tiffit.tconplanner.buttons.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.util.ModifierStateEnum;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.SlotType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ModifierSelectButton  extends Button {

    private static final Style ERROR_STYLE = Style.EMPTY.withColor(Color.fromLegacyFormat(TextFormatting.RED));
    private static final ITextComponent ADD_COMPONENT = new StringTextComponent("Click to add!").withStyle(Style.EMPTY.withColor(Color.fromLegacyFormat(TextFormatting.GOLD)));

    private final IDisplayModifierRecipe recipe;
    private final Modifier modifier;
    private final boolean selected;
    private final ITextComponent error;
    public final ModifierStateEnum state;
    private final PlannerScreen parent;
    private final List<ItemStack> recipeStacks = new ArrayList<>();

    public ModifierSelectButton(IDisplayModifierRecipe recipe, ModifierStateEnum state, @Nullable ITextComponent error, PlannerScreen parent) {
        super(0, 0, 100, 18, new StringTextComponent(""), e -> {});
        this.recipe = recipe;
        this.modifier = recipe.getDisplayResult().getModifier();
        this.parent = parent;
        this.selected = false;
        this.state = state;
        this.error = error;
        List<List<ItemStack>> itemstacks = recipe.getDisplayItems();
        itemstacks.subList(1, itemstacks.size()).forEach(recipeStacks::addAll);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        switch (state){
            case APPLIED:
                RenderSystem.color4f(0.5f, 1f, 0.5f, 1f); break;
            case UNAVAILABLE:
                RenderSystem.color4f(1f, 0.5f, 0.5f, 1f); break;
            default:
                RenderSystem.color4f(1f, 1f, 1f, 1f);
        }
        parent.blit(stack, x, y, 0, 224, 100, 18);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(recipeStacks.get((int)((System.currentTimeMillis() / 1000) % recipeStacks.size())), x + 1, y + 1);
        FontRenderer font = Minecraft.getInstance().font;
        Screen.drawString(stack, font, modifier.getDisplayName(), x + 20, y + 2, 0xff_ff_ff_ff);

        stack.pushPose();
        stack.translate(x + 20, y + 11, 0);
        stack.scale(0.5f, 0.5f, 1);
        if(recipe.getSlots() != null) {
            SlotType.SlotCount count = recipe.getSlots();
            IFormattableTextComponent text = new StringTextComponent("Uses ").append(count.getCount() + " ").append(count.getType().getDisplayName()).append(count.getCount() != 1 ? " slots" : " slot");
            Screen.drawString(stack, font, text, 0, 0, 0xff_ff_ff_ff);
        }
        stack.popPose();

        stack.pushPose();
        stack.translate(x + width - 1, y + 11, 0);
        stack.scale(0.5f, 0.5f, 1);
        String usedLevelsText = parent.blueprint.modifiers.getOrDefault(new ModifierInfo(recipe), 0) + (recipe.getMaxLevel() > 0 ? "/" + recipe.getMaxLevel() : "");
        Screen.drawString(stack, font, new StringTextComponent(usedLevelsText), -font.width(usedLevelsText), 0, 0xff_ff_ff_ff);
        stack.popPose();
        if(isHovered){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> {
            List<ITextComponent> tooltips = new ArrayList<>(modifier.getDescriptionList());
            if(state == ModifierStateEnum.UNAVAILABLE)tooltips.add(error.copy().withStyle(ERROR_STYLE));
            else tooltips.add(ADD_COMPONENT);
            parent.renderComponentTooltip(stack, tooltips, mouseX, mouseY);
        });
    }

    @Override
    public void onPress() {
        ModifierInfo info = new ModifierInfo(recipe);
        parent.blueprint.modifiers.put(info, 1);
        parent.selectedModifier = info;
        parent.refresh();
    }
}
