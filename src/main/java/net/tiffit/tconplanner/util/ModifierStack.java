package net.tiffit.tconplanner.util;

import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.MathHelper;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModifierStack {
    private final LinkedList<ModifierInfo> stack = new LinkedList<>();
    private final HashMap<Modifier, Integer> incrementalDiffMap = new HashMap<>();

    public void push(ModifierInfo info){
        stack.push(info);
    }

    public void pop(ModifierInfo info){
        stack.removeLastOccurrence(info);
    }

    public void setIncrementalDiff(Modifier modifier, int amount){
        incrementalDiffMap.put(modifier, MathHelper.clamp(amount,0, ModifierRecipeLookup.getNeededPerLevel(modifier)));
    }

    public int getIncrementalDiff(Modifier modifier){
        return incrementalDiffMap.getOrDefault(modifier, 0);
    }

    public int getLevel(ModifierInfo info){
        return (int) stack.stream().filter(info1 -> info1.equals(info)).count();
    }

    public void applyIncrementals(ToolStack tool){
        stack.stream().distinct().forEach(info -> {
            Modifier mod = info.modifier;
            int amount = ModifierRecipeLookup.getNeededPerLevel(mod);
            if(amount > 0){
                IncrementalModifier.setAmount(tool.getPersistentData(), mod, amount - getIncrementalDiff(mod));
            }
        });
    }

    public void forEach(Consumer<ModifierInfo> consumer){
        for (ModifierInfo info : stack) {
            consumer.accept(info);
        }
    }

    public ListNBT toNBT(){
        ListNBT modList = new ListNBT();
        for (ModifierInfo info : stack) {
            modList.add(StringNBT.valueOf(info.modifier.getId().toString()));
        }
        return modList;
    }

    public void fromNBT(ListNBT modList){
        stack.clear();
        Map<ModifierId, IDisplayModifierRecipe> recipesMap = PlannerScreen.getModifierRecipes().stream().collect(Collectors.toMap(recipe -> recipe.getDisplayResult().getModifier().getId(), recipe -> recipe));
        for(int i = 0; i < modList.size(); i++){
            ModifierId modId = new ModifierId(modList.getString(i));
            if(recipesMap.containsKey(modId)) {
                push(new ModifierInfo(recipesMap.get(modId)));
            }
        }
    }
}
