package net.tiffit.tconplanner.util;

import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationInventory;

import javax.annotation.Nullable;

public class DummyTinkersStationInventory implements ITinkerStationInventory {

    private ItemStack stack;

    public DummyTinkersStationInventory(ItemStack stack){
        this.stack = stack;
    }

    @Override
    public ItemStack getTinkerableStack() {
        return stack;
    }

    @Override
    public ItemStack getInput(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getInputCount() {
        return 0;
    }

    @Nullable
    @Override
    public MaterialRecipe getInputMaterial(int i) {
        return null;
    }
}
