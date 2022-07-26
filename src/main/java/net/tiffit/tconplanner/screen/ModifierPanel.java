package net.tiffit.tconplanner.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.buttons.*;
import net.tiffit.tconplanner.screen.buttons.modifiers.*;
import net.tiffit.tconplanner.util.*;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.SoundUtils;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ModifierPanel extends PlannerPanel{
    public static final String KEY_MAX_LEVEL = TConstruct.makeTranslationKey("recipe", "modifier.max_level");
    private final static SlotType[] ValidSlots = new SlotType[]{SlotType.UPGRADE, SlotType.ABILITY};

    public ModifierPanel(int x, int y, int width, int height, ItemStack result, ToolStack tool, List<IDisplayModifierRecipe> modifiers, PlannerScreen parent) {
        super(x, y, width, height, parent);
        //Show available modifier slots
        int slotIndex = 0;
        for (SlotType slotType : ValidSlots) {
            int slots = tool.getFreeSlots(slotType);
            List<ITextComponent> tooltips = new ArrayList<>();
            ITextComponent coloredName = new StringTextComponent("")
                    .withStyle(Style.EMPTY.withColor(slotType.getColor()))
                    .append(slotType.getDisplayName())
                    .append(new StringTextComponent("").withStyle(TextFormatting.RESET));
            tooltips.add(TranslationUtil.createComponent("slots.available", coloredName));
            tooltips.add(new StringTextComponent(""));
            tooltips.add(TranslationUtil.createComponent("modifiers.addcreativeslot").withStyle(TextFormatting.GREEN));
            IFormattableTextComponent removeCreativeSlotTextComponent = TranslationUtil.createComponent("modifiers.removecreativeslot").withStyle(TextFormatting.RED);
            if(slots == 0){
                removeCreativeSlotTextComponent.withStyle(removeCreativeSlotTextComponent.getStyle().applyFormats(TextFormatting.STRIKETHROUGH));
            }
            tooltips.add(removeCreativeSlotTextComponent);
            IFormattableTextComponent slotsRemaining = new StringTextComponent("" + slots);
            int creativeSlots = parent.blueprint.creativeSlots.getOrDefault(slotType, 0);
            if(creativeSlots > 0){
                slotsRemaining.append(" (+" + parent.blueprint.creativeSlots.get(slotType) + ")");
            }
            addChild(new TooltipTextWidget(108, 23 + slotIndex * 12, TextPosEnum.LEFT, slotsRemaining, tooltips, parent)
                    .withColor(slotType.getColor().getValue() + 0xff_00_00_00)
                    .withClickHandler((mouseX, mouseY, mouseButton) -> handleCreativeSlotButton(slotType, slots, creativeSlots, mouseButton)));
            slotIndex++;
        }
        addChild(new BannerWidget(7, 0, TranslationUtil.createComponent("banner.modifiers"), parent));
        int modGroupStartY = 23;
        int modGroupStartX = 2;
        Blueprint blueprint = parent.blueprint;
        ModifierInfo selectedModifier = parent.selectedModifier;
        ModifierStack modifierStack = parent.modifierStack;

        //Show modifier stack
        if(modifierStack != null){
            HashMap<ModifierId, Integer> levelCount = new HashMap<>();
            PaginatedPanel<ModifierStackButton> stackGroup = new PaginatedPanel<>(modGroupStartX, modGroupStartY, 100, 18, 1, 5, 2, "modifierstackgroup", parent);
            addChild(stackGroup);
            ToolStack displayStack = ToolStack.from(blueprint.createOutput(false));
            List<ModifierInfo> modStack = modifierStack.getStack();
            Blueprint resultingBlueprint = parent.blueprint.clone();
            resultingBlueprint.modStack = modifierStack;
            ValidatedResult validatedResult = resultingBlueprint.validate();
            boolean isValid = !validatedResult.hasError();
            for (int i = 0; i < modStack.size(); i++) {
                ModifierInfo info = modStack.get(i);
                int newLevel = levelCount.getOrDefault(info.modifier.getId(), 0) + 1;
                levelCount.put(info.modifier.getId(), newLevel);
                displayStack.addModifier(info.modifier, 1);
                if (info.count != null) {
                    displayStack.getPersistentData().addSlots(info.count.getType(), -info.count.getCount());
                }
                displayStack.rebuildStats();
                stackGroup.addChild(new ModifierStackButton(info, i, newLevel, displayStack.copy().createStack(), parent));
            }
            stackGroup.refresh();
            addChild(new TextButton(2 + 50 - 58 / 2, 158, TranslationUtil.createComponent("modifierstack.save"), () -> {
                if(isValid) {
                    parent.blueprint.modStack = parent.modifierStack;
                    parent.modifierStack = null;
                    parent.refresh();
                }
            }, parent).withColor(isValid ? 0x50ff50 : 0x1a0000).withTooltip(isValid ? null : validatedResult.getMessage()));
            addChild(new TextButton(2 + 50 - 58 / 2, 180, TranslationUtil.createComponent("modifierstack.cancel"), () -> {
                parent.modifierStack = null;
                parent.refresh();
            }, parent).withColor(0xe02121));

            if(parent.selectedModifierStackIndex != -1){
                addChild(new StackMoveButton(2 + 50 - 9, 130, true, stackGroup, parent));
                addChild(new StackMoveButton(2 + 50 - 9, 141, false, stackGroup, parent));
            }
        }else if(selectedModifier == null) { //Show list of modifiers
            PaginatedPanel<ModifierSelectButton> modifiersGroup = new PaginatedPanel<>(modGroupStartX, modGroupStartY, 100, 18, 1, 9, 2, "modifiersgroup", parent);
            addChild(modifiersGroup);
            for (IDisplayModifierRecipe recipe : modifiers) {
                if (recipe.getDisplayItems().get(0).stream().anyMatch(stack -> ToolStack.from(stack).getDefinition() == blueprint.toolDefinition)) {
                    modifiersGroup.addChild(ModifierSelectButton.create(recipe, tool, result, parent));
                }
            }
            modifiersGroup.sort(Comparator.comparingInt(value -> value.state.ordinal()));
            modifiersGroup.refresh();
            addChild(new IconButton(100, 0, new Icon(5, 0),
                    TranslationUtil.createComponent("editmodifierstack"), parent, e -> {
                parent.modifierStack = blueprint.clone().modStack;
                parent.selectedModifierStackIndex = -1;
                parent.refresh();
            }));
        } else { //Add/remove a modifier
            ModifierSelectButton modSelectButton = ModifierSelectButton.create(selectedModifier.recipe, tool, result, parent);
            modSelectButton.x = modGroupStartX;
            modSelectButton.y = modGroupStartY;
            addChild(modSelectButton);

            Modifier modifier = selectedModifier.modifier;
            ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) selectedModifier.recipe;

            addChild(new ModPreviewWidget(2 + 50 - 9, 50, result, parent));
            int arrowOffset = 11;
            ModLevelButton addButton = new ModLevelButton(2 + 50 + arrowOffset - 2, 50, 1, parent);
            ValidatedResult validatedResultAdd = modifier instanceof SingleUseModifier && tool.getModifierLevel(modifier) >= 1 ?
                    ValidatedResult.failure(KEY_MAX_LEVEL, modifier.getDisplayName(), 1) : tsrecipe.getValidatedResult(new DummyTinkersStationInventory(result));
            if (!validatedResultAdd.isSuccess()) {
                addButton.disable(validatedResultAdd.getMessage().copy().setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
                addChild(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, 50, ItemStack.EMPTY, parent));
            } else if (blueprint.modStack.getIncrementalDiff(modifier) > 0) {
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
            if (validatedResultSubtract.hasError()) {
                subtractButton.disable(((IFormattableTextComponent) validatedResultSubtract.getMessage()).setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
            }
            addChild(new ModPreviewWidget(subtractButton.x - 2 - 18, 50, subtractButton.isDisabled() ? ItemStack.EMPTY : validatedResultSubtract.getResult(), parent));
            addChild(subtractButton);
            int perLevel = ModifierRecipeLookup.getNeededPerLevel(modifier);
            if (perLevel > 0 && blueprint.modStack.getLevel(modifier) > 0) {
                addChild(new SliderWidget(2 + 10, 70, 80, 20, val -> {
                    blueprint.modStack.setIncrementalDiff(modifier, perLevel - val);
                    parent.refresh();
                },1, perLevel, perLevel - blueprint.modStack.getIncrementalDiff(modifier), parent));
            }

            addChild(new TextButton(2 + 50 - 58 / 2, 115, TranslationUtil.createComponent("modifiers.exit"), () -> {
                parent.selectedModifier = null;
                parent.refresh();
            }, parent).withColor(0xe02121));
        }
    }

    private boolean handleCreativeSlotButton(SlotType type, int remainingSlots, int creativeSlots, int mb){
        SoundHandler soundHandler = Minecraft.getInstance().getSoundManager();
        if(mb == 0){
            parent.blueprint.addCreativeSlot(type);
            parent.refresh();
            soundHandler.play(SimpleSound.forUI(SoundEvents.ANVIL_PLACE, 2f, 0.08f));
            return true;
        }
        if(mb == 1){
            if(creativeSlots > 0 && remainingSlots > 0){
                parent.blueprint.removeCreativeSlot(type);
                parent.refresh();
                soundHandler.play(SimpleSound.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 2f, 0.08f));
                return true;
            }
            soundHandler.play(SimpleSound.forUI(SoundEvents.BAMBOO_FALL, 2f, 0.08f));
        }
        return false;
    }
}
