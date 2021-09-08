package net.tiffit.tconplanner.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModifierStack {
    private final LinkedList<ModifierInfo> stack = new LinkedList<>();

    public void push(ModifierInfo info){
        stack.push(info);
    }

    public void pop(ModifierInfo info){
        stack.removeLastOccurrence(info);
    }

    public int getLevel(ModifierInfo info){
        return (int) stack.stream().filter(info1 -> info1.equals(info)).count();
    }

    public int getSize(){
        return stack.size();
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
