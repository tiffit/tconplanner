package net.tiffit.tconplanner.data;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.ModifierStack;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Blueprint {

    public final SlotInformation toolSlotInfo;
    public final ItemStack toolStack;
    public final IModifiable toolItem;
    public final ToolDefinition toolDefinition;
    public final IToolPart[] parts;
    public final IMaterial[] materials;
    public final ModifierStack modStack = new ModifierStack();

    public Blueprint(SlotInformation information){
        this.toolSlotInfo = information;
        toolStack = toolSlotInfo.getToolForRendering();
        toolItem = (IModifiable) toolSlotInfo.getItem();
        toolDefinition = toolItem.getToolDefinition();
        parts = toolDefinition.getRequiredComponents().toArray(new IToolPart[0]);
        materials = new IMaterial[parts.length];
    }

    public ItemStack createOutput(){
        return createOutput(true);
    }

    public ItemStack createOutput(boolean applyMods){
        if(!isComplete())return ItemStack.EMPTY;
        ItemStack built = ToolBuildHandler.buildItemFromMaterials(toolItem, Lists.newArrayList(materials));
        ToolStack stack = ToolStack.from(built);
        if(applyMods) {
            for (ModifierInfo info : modStack.getStack()) {
                stack.addModifier(info.modifier, 1);
                if (info.count != null) {
                    stack.getPersistentData().addSlots(info.count.getType(), -info.count.getCount());
                }
            }
            modStack.applyIncrementals(stack);
        }
        stack.rebuildStats();
        return stack.createStack();
    }

    public boolean isComplete(){
        return Arrays.stream(materials).noneMatch(Objects::isNull);
    }

    public Blueprint clone(){
        return fromNBT(toNBT());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blueprint blueprint = (Blueprint) o;
        return toNBT().equals(blueprint.toNBT());
    }

    public ValidatedResult validate(){
        ItemStack is = createOutput(false);
        ValidatedResult result = null;
        for (ModifierInfo info : modStack.getStack()) {
            ValidatedResult rs = ((ITinkerStationRecipe) info.recipe).getValidatedResult(new DummyTinkersStationInventory(is));
            if(rs.hasError()){
                result = rs;
                break;
            }else{
                is = rs.getResult();
            }
        }
        if(result == null)return ValidatedResult.PASS;
        return result;
    }

    public CompoundNBT toNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("tool", Objects.requireNonNull(toolSlotInfo.getItem().getRegistryName()).toString());
        ListNBT matList = new ListNBT();
        for(int i = 0; i < materials.length; i++){
            matList.add(StringNBT.valueOf(materials[i] == null ? "" : materials[i].getIdentifier().toString()));
        }
        nbt.put("materials", matList);
        nbt.put("modifiers", modStack.toNBT());
        return nbt;
    }

    public static Blueprint fromNBT(CompoundNBT tag){
        ResourceLocation toolRL = new ResourceLocation(tag.getString("tool"));
        Optional<SlotInformation> optional = SlotInformationLoader.getSlotInformationList().stream()
                .filter(info -> !info.isRepair() && Objects.equals(info.getItem().getRegistryName(), toolRL)).findFirst();
        if(!optional.isPresent())return null;
        Blueprint bp = new Blueprint(optional.get());

        ListNBT materials = tag.getList("materials", 8);
        for(int i = 0; i < materials.size(); i++){
            String id = materials.getString(i);
            if("".equals(id))continue;
            IMaterial material = MaterialRegistry.getMaterial(new MaterialId(id));
            if(i < bp.materials.length && bp.parts[i].canUseMaterial(material)){
                bp.materials[i] = material;
            }
        }

        CompoundNBT modifiers = tag.getCompound("modifiers");
        bp.modStack.fromNBT(modifiers);
        return bp;
    }

}
