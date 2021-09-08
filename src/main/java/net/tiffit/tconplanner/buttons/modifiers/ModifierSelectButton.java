package net.tiffit.tconplanner.buttons.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.ModifierStateEnum;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ModifierSelectButton extends Button {

    private static final Style ERROR_STYLE = Style.EMPTY.withColor(Color.fromLegacyFormat(TextFormatting.RED));

    private final IDisplayModifierRecipe recipe;
    private final Modifier modifier;
    private final boolean selected;
    private final ITextComponent error;
    private final ITextComponent displayName;
    public final ModifierStateEnum state;
    private final PlannerScreen parent;
    private final List<ItemStack> recipeStacks = new ArrayList<>();

    private final StringTextComponent levelText;

    public ModifierSelectButton(IDisplayModifierRecipe recipe, ModifierStateEnum state, @Nullable ITextComponent error, int level, PlannerScreen parent) {
        super(0, 0, 100, 18, new StringTextComponent(""), e -> {});
        this.recipe = recipe;
        this.modifier = recipe.getDisplayResult().getModifier();
        this.parent = parent;
        this.selected = false;
        this.state = state;
        this.error = error;
        List<List<ItemStack>> itemstacks = recipe.getDisplayItems();
        itemstacks.subList(1, itemstacks.size()).forEach(recipeStacks::addAll);
        displayName = level == 0 ? modifier.getDisplayName() : modifier.getDisplayName(level);
        int maxLevel = modifier instanceof SingleUseModifier ? 1 : recipe.getMaxLevel();
        levelText = new StringTextComponent(parent.blueprint.modStack.getLevel(new ModifierInfo(recipe)) + "/" +(maxLevel > 0 ? maxLevel : "\u221E"));
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
        Screen.drawString(stack, font, displayName, x + 20, y + 2, 0xff_ff_ff_ff);

        stack.pushPose();
        stack.translate(x + 20, y + 11, 0);
        stack.scale(0.5f, 0.5f, 1);
        if(recipe.getSlots() != null) {
            SlotType.SlotCount count = recipe.getSlots();
            IFormattableTextComponent text = count.getCount() == 1 ? TranslationUtil.createComponent("modifiers.usedslot", count.getType().getDisplayName()) :
                    TranslationUtil.createComponent("modifiers.usedslots", count.getCount(), count.getType().getDisplayName());
            Screen.drawString(stack, font, text, 0, 0, 0xff_ff_ff_ff);
        }
        stack.popPose();

        stack.pushPose();
        stack.translate(x + width - 1, y + 11, 0);
        stack.scale(0.5f, 0.5f, 1);
        Screen.drawString(stack, font, levelText, -font.width(levelText), 0, 0xff_ff_ff_ff);
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
            parent.renderComponentTooltip(stack, tooltips, mouseX, mouseY);
        });
    }

    @Override
    public void onPress() {
        switch (state){
            case AVAILABLE: {
                ModifierInfo info = new ModifierInfo(recipe);
                parent.selectedModifier = info;
                parent.refresh();
                break;
            }
            case APPLIED: {
                parent.selectedModifier = new ModifierInfo(recipe);
                parent.refresh();
                break;
            }
        }
    }

    @Override
    public void playDownSound(SoundHandler sound) {
        if(state == ModifierStateEnum.UNAVAILABLE){
            sound.play(SimpleSound.forUI(SoundEvents.ANVIL_HIT, 1.0F));
        } else {
            super.playDownSound(sound);
        }
    }

    public static ModifierSelectButton create(IDisplayModifierRecipe recipe, ToolStack tstack, ItemStack stack, PlannerScreen screen){
        ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) recipe;
        ModifierStateEnum mstate = ModifierStateEnum.UNAVAILABLE;
        ITextComponent error = null;
        int currentLevel = tstack.getModifierLevel(recipe.getDisplayResult().getModifier());
        if (currentLevel != 0)
            mstate = ModifierStateEnum.APPLIED;
        else {
            ValidatedResult validatedResult = tsrecipe.getValidatedResult(new DummyTinkersStationInventory(stack));
            if (validatedResult.isSuccess()) mstate = ModifierStateEnum.AVAILABLE;
            else error = validatedResult.getMessage();
        }
        return new ModifierSelectButton(recipe, mstate, error, currentLevel, screen);
    }
}
