package net.tiffit.tconplanner.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModifierStack {
    private final LinkedList<ModifierInfo> stack = new LinkedList<>();
    private final HashMap<ModifierId, Integer> incrementalDiffMap = new HashMap<>();

    public void push(ModifierInfo info){
        stack.add(info);
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

    public boolean isRecipeUsed(ITinkerStationRecipe recipe){
        return stack.stream().anyMatch(info -> ((ITinkerStationRecipe)info.recipe).getId().equals(recipe.getId()));
    }

    public int getLevel(Modifier modifier){
        return (int) stack.stream().filter(info1 -> info1.modifier.equals(modifier)).count();
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

    public List<ModifierInfo> getStack(){
        return ImmutableList.copyOf(stack);
    }

    public CompoundNBT toNBT(){
        CompoundNBT tag = new CompoundNBT();
        ListNBT modList = new ListNBT();
        for (ModifierInfo info : stack) {
            modList.add(StringNBT.valueOf(((ITinkerStationRecipe)info.recipe).getId().toString()));
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
        Map<ResourceLocation, IDisplayModifierRecipe> recipesMap = PlannerScreen.getModifierRecipes().stream().collect(Collectors.toMap(recipe -> ((ITinkerStationRecipe)recipe).getId(), recipe -> recipe));
        for(int i = 0; i < modList.size(); i++){
            ResourceLocation resourceLocation = new ResourceLocation(modList.getString(i));
            if(recipesMap.containsKey(resourceLocation)) {
                push(new ModifierInfo(recipesMap.get(resourceLocation)));
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
