package net.tiffit.tconplanner.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.MathHelper;
import net.tiffit.tconplanner.screen.PlannerScreen;
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
    private final HashMap<ModifierId, Integer> incrementalDiffMap = new HashMap<>();

    public void push(ModifierInfo info){
        stack.push(info);
    }

    public void pop(ModifierInfo info){
        stack.removeLastOccurrence(info);
    }

    public void setIncrementalDiff(Modifier modifier, int amount){
        incrementalDiffMap.put(modifier.getId(), MathHelper.clamp(amount,0, ModifierRecipeLookup.getNeededPerLevel(modifier)));
    }

    public int getIncrementalDiff(Modifier modifier){
        return incrementalDiffMap.getOrDefault(modifier.getId(), 0);
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

    public CompoundNBT toNBT(){
        CompoundNBT tag = new CompoundNBT();
        ListNBT modList = new ListNBT();
        for (ModifierInfo info : stack) {
            modList.add(StringNBT.valueOf(info.modifier.getId().toString()));
        }
        tag.put("mods", modList);
        ListNBT diffList = new ListNBT();
        for (Map.Entry<ModifierId, Integer> entry : incrementalDiffMap.entrySet()) {
            CompoundNBT diffNBT = new CompoundNBT();
            diffNBT.putString("mod", entry.getKey().toString());
            diffNBT.putInt("amount", entry.getValue());
            diffList.add(diffNBT);
        }
        tag.put("diff", diffList);
        return tag;
    }

    public void fromNBT(CompoundNBT tag){
        stack.clear();
        incrementalDiffMap.clear();
        ListNBT modList = tag.getList("mods", 8);
        Map<ModifierId, IDisplayModifierRecipe> recipesMap = PlannerScreen.getModifierRecipes().stream().collect(Collectors.toMap(recipe -> recipe.getDisplayResult().getModifier().getId(), recipe -> recipe));
        for(int i = 0; i < modList.size(); i++){
            ModifierId modId = new ModifierId(modList.getString(i));
            if(recipesMap.containsKey(modId)) {
                push(new ModifierInfo(recipesMap.get(modId)));
            }
        }
        ListNBT diffList = tag.getList("diff", 10);
        for(int i = 0; i < diffList.size(); i++){
            CompoundNBT diffNBT = diffList.getCompound(i);
            ModifierId modId = new ModifierId(diffNBT.getString("mod"));
            int amount = diffNBT.getInt("amount");
            incrementalDiffMap.put(modId, amount);
        }
    }
}
