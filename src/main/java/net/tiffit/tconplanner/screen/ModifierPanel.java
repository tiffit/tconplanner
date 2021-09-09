package net.tiffit.tconplanner.screen;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.tiffit.tconplanner.screen.buttons.BannerWidget;
import net.tiffit.tconplanner.screen.buttons.PaginatedPanel;
import net.tiffit.tconplanner.screen.buttons.SliderWidget;
import net.tiffit.tconplanner.screen.buttons.TooltipTextWidget;
import net.tiffit.tconplanner.screen.buttons.modifiers.ModExitButton;
import net.tiffit.tconplanner.screen.buttons.modifiers.ModLevelButton;
import net.tiffit.tconplanner.screen.buttons.modifiers.ModPreviewWidget;
import net.tiffit.tconplanner.screen.buttons.modifiers.ModifierSelectButton;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.ToolValidator;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Comparator;
import java.util.List;

public class ModifierPanel extends PlannerPanel{
    protected static final String KEY_MAX_LEVEL = TConstruct.makeTranslationKey("recipe", "modifier.max_level");

    public ModifierPanel(int x, int y, int width, int height, ItemStack result, ToolStack tool, List<IDisplayModifierRecipe> modifiers, PlannerScreen parent) {
        super(x, y, width, height, parent);
        //Show available modifier slots
        int slotIndex = 0;
        for (SlotType slotType : SlotType.getAllSlotTypes()) {
            int slots = tool.getFreeSlots(slotType);
            addChild(new TooltipTextWidget(105,  23 + slotIndex*12,
                    new StringTextComponent("" + slots),
                    TranslationUtil.createComponent("slots.available", slotType.getDisplayName()), parent)
                    .withColor(slotType.getColor().getValue() + 0xff_00_00_00));
            slotIndex++;
        }

        //Show modifier buttons
        addChild(new BannerWidget(7, 0, TranslationUtil.createComponent("banner.modifiers"), parent));
        int modGroupStartY = 23;
        int modGroupStartX = 2;
        Blueprint blueprint = parent.blueprint;
        ModifierInfo selectedModifier = parent.selectedModifier;
        if(selectedModifier == null) {
            PaginatedPanel<ModifierSelectButton> modifiersGroup = new PaginatedPanel<>(modGroupStartX, modGroupStartY, 100, 18, 1, 9, 2, "modifiersgroup", parent);
            addChild(modifiersGroup);
            for (IDisplayModifierRecipe recipe : modifiers) {
                if (recipe.getDisplayItems().get(0).stream().anyMatch(stack -> ToolStack.from(stack).getDefinition() == blueprint.toolDefinition)) {
                    modifiersGroup.addChild(ModifierSelectButton.create(recipe, tool, result, parent));
                }
            }
            modifiersGroup.sort(Comparator.comparingInt(value -> value.state.ordinal()));
            modifiersGroup.refresh();
        } else {
            ModifierSelectButton modSelectButton = ModifierSelectButton.create(selectedModifier.recipe, tool, result, parent);
            modSelectButton.x = modGroupStartX;
            modSelectButton.y = modGroupStartY;
            addChild(modSelectButton);

            Modifier modifier = selectedModifier.modifier;
            ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) selectedModifier.recipe;

            addChild(new ModPreviewWidget(2 + 50 - 9, 50, result, parent));
            int arrowOffset = 11;
            ModLevelButton addButton = new ModLevelButton(2 + 50 + arrowOffset - 2, 50, 1, parent);
            ValidatedResult validatedResultAdd = modifier instanceof SingleUseModifier && tool.getModifierLevel(modifier) == 1 ?
                    ValidatedResult.failure(KEY_MAX_LEVEL, modifier.getDisplayName(), 1) :tsrecipe.getValidatedResult(new DummyTinkersStationInventory(result));
            if(!validatedResultAdd.isSuccess()){
                addButton.disable(validatedResultAdd.getMessage().copy().setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
                addChild(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, 50, ItemStack.EMPTY, parent));
            }else if(blueprint.modStack.getIncrementalDiff(modifier) > 0){
                addButton.disable(TranslationUtil.createComponent("modifiers.error.incrementnotmax").setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
                addChild(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, 50, ItemStack.EMPTY, parent));
            } else {
                Blueprint copy = blueprint.clone();
                copy.modStack.push(selectedModifier);
                addChild(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, 50, copy.createOutput(), parent));
            }
            addChild(addButton);

            ModLevelButton subtractButton = new ModLevelButton(2 + 50 - arrowOffset - 18, 50, -1, parent);
            ValidatedResult validatedResultSubtract = ToolValidator.validateModRemoval(blueprint, tool, selectedModifier);
            if(validatedResultSubtract.hasError()){
                subtractButton.disable(((IFormattableTextComponent)validatedResultSubtract.getMessage()).setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
            }
            addChild(new ModPreviewWidget(subtractButton.x - 2 - 18, 50, subtractButton.isDisabled() ? ItemStack.EMPTY : validatedResultSubtract.getResult(), parent));
            addChild(subtractButton);
            int perLevel = ModifierRecipeLookup.getNeededPerLevel(modifier);
            if(perLevel > 0 && blueprint.modStack.getLevel(selectedModifier) > 0){
                addChild(new SliderWidget(2 + 10, 70, 80, 20, val -> {blueprint.modStack.setIncrementalDiff(modifier, perLevel-val); parent.refresh();},
                        1, perLevel, perLevel - blueprint.modStack.getIncrementalDiff(modifier), parent));
            }

            addChild(new ModExitButton(2 + 50 - 58/2, 115, parent));
        }
    }
}
