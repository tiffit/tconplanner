package net.tiffit.tconplanner.util;

import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public final class ToolValidator {

    /**
     * Validate if a modifier is able to be removed from a tool
     * @param tool The tool to try to remove a modifier from
     * @param modInfo The modifier to remove
     */
    public static ValidatedResult validateModRemoval(Blueprint bp, ToolStack tool, ModifierInfo modInfo){
        ToolStack toolClone = tool.copy();
        int toolBaseLevel = ToolStack.from(bp.createOutput(false)).getModifierLevel(modInfo.modifier);
        int minLevel = Math.max(0, toolBaseLevel);
        if(bp.modStack.getLevel(modInfo.modifier) + toolBaseLevel <= minLevel || !bp.modStack.isRecipeUsed((ITinkerStationRecipe) modInfo.recipe))
            return ValidatedResult.failure("gui.tconplanner.modifiers.error.minlevel");
        toolClone.removeModifier(modInfo.modifier, 1);
        IncrementalModifier.setAmount(toolClone.getPersistentData(), modInfo.modifier, ModifierRecipeLookup.getNeededPerLevel(modInfo.modifier));
        ValidatedResult validatedResultSubtract = toolClone.validate();
        if(validatedResultSubtract.hasError())return validatedResultSubtract;
        Blueprint bpClone = bp.clone();
        bpClone.modStack.pop(modInfo);
        ValidatedResult bpResult = bpClone.validate();
        if(bpResult.hasError())return bpResult;
        return ValidatedResult.success(toolClone.createStack());
    }


}
