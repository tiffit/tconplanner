package net.tiffit.tconplanner.data;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.tiffit.tconplanner.api.TCTool;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.ModifierStack;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.PartRequirement;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.*;
import java.util.stream.Collectors;

public class Blueprint {

    public final TCTool tool;
    public final ItemStack toolStack;
    public final IModifiable toolItem;
    public final ToolDefinition toolDefinition;
    public final IToolPart[] parts;
    public final IMaterial[] materials;

    public final Map<SlotType, Integer> creativeSlots = new HashMap<>();

    public ModifierStack modStack = new ModifierStack();

    public Blueprint(TCTool tool){
        this.tool = tool;
        toolStack = tool.getRenderTool();
        toolItem = tool.getModifiable();
        toolDefinition = toolItem.getToolDefinition();
        parts = toolDefinition.getData().getParts().stream().map(PartRequirement::getPart).filter(Objects::nonNull).toArray(IToolPart[]::new);
        materials = new IMaterial[parts.length];
    }

    public ItemStack createOutput(){
        return createOutput(true);
    }

    public ItemStack createOutput(boolean applyMods){
        if(!isComplete())return ItemStack.EMPTY;
        ItemStack built = ToolBuildHandler.buildItemFromMaterials(toolItem, Lists.newArrayList(materials));
        ToolStack stack = ToolStack.from(built);
        creativeSlots.forEach((slotType, amount) -> stack.getPersistentData().addSlots(slotType, amount));
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

    public void addCreativeSlot(SlotType type){
        addCreativeSlot(type, 1);
    }

    public void addCreativeSlot(SlotType type, int amount){
        creativeSlots.compute(type, (slotType, val) -> {
            if(val == null)val = 0;
            return val + amount;
        });
    }

    public void removeCreativeSlot(SlotType type){
        addCreativeSlot(type, -1);
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
        ToolStack ts = ToolStack.from(createOutput(false));
        ValidatedResult result = null;
        for (ModifierInfo info : modStack.getStack()) {
            ValidatedResult rs = ((ITinkerStationRecipe) info.recipe).getValidatedResult(new DummyTinkersStationInventory(ts.createStack()));
            if(rs.hasError()){
                result = rs;
                break;
            }else{
                ts.addModifier(info.modifier, 1);
            }
        }
        if(result == null)return ValidatedResult.PASS;
        return result;
    }

    public CompoundNBT toNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("tool", Objects.requireNonNull(tool.getItem().getRegistryName()).toString());
        ListNBT matList = new ListNBT();
        for(int i = 0; i < materials.length; i++){
            matList.add(StringNBT.valueOf(materials[i] == null ? "" : materials[i].getIdentifier().toString()));
        }
        nbt.put("materials", matList);
        nbt.put("modifiers", modStack.toNBT());

        if(creativeSlots.size() > 0){
            CompoundNBT creativeSlotsNbt = new CompoundNBT();
            creativeSlots.forEach((slotType, integer) -> {
                if(integer > 0) {
                    creativeSlotsNbt.putInt(slotType.getName(), integer);
                }
            });
            nbt.put("creativeSlots", creativeSlotsNbt);
        }
        return nbt;
    }

    public static Blueprint fromNBT(CompoundNBT tag){
        ResourceLocation toolRL = new ResourceLocation(tag.getString("tool"));
        Optional<TCTool> optional = TCTool.getTools().stream()
                .filter(tool -> Objects.equals(tool.getItem().getRegistryName(), toolRL)).findFirst();
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

        if(tag.contains("creativeSlots")){
            CompoundNBT creativeSlotsTag = tag.getCompound("creativeSlots");
            for (String key : creativeSlotsTag.getAllKeys()) {
                SlotType type = SlotType.getIfPresent(key);
                if(type != null){
                    bp.creativeSlots.put(type, creativeSlotsTag.getInt(key));
                }
            }
        }
        return bp;
    }

}
