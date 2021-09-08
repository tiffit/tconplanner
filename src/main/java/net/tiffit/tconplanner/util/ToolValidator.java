package net.tiffit.tconplanner.util;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

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
        if(bp.modStack.getLevel(modInfo) + toolBaseLevel <= minLevel)return ValidatedResult.failure("Level can not go lower");
        toolClone.removeModifier(modInfo.modifier, 1);
        ValidatedResult validatedResultSubtract = toolClone.validate();
        if(validatedResultSubtract.hasError())return validatedResultSubtract;
        Blueprint bpClone = bp.clone();
        bpClone.modStack.pop(modInfo);
        ValidatedResult bpResult = bpClone.validate();
        if(bpResult.hasError())return bpResult;
        return ValidatedResult.success(toolClone.createStack());
    }

}
