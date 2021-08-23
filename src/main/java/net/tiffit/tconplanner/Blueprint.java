package net.tiffit.tconplanner;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
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

    public Blueprint(SlotInformation information){
        this.toolSlotInfo = information;
        toolStack = toolSlotInfo.getToolForRendering();
        toolItem = (IModifiable) toolSlotInfo.getItem();
        toolDefinition = toolItem.getToolDefinition();
        parts = toolDefinition.getRequiredComponents().toArray(new IToolPart[0]);
        materials = new IMaterial[parts.length];
    }

    public ItemStack createOutput(){
        return !isComplete() ? ItemStack.EMPTY : ToolBuildHandler.buildItemFromMaterials(toolItem, Lists.newArrayList(materials));
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

    public CompoundNBT toNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("tool", Objects.requireNonNull(toolSlotInfo.getItem().getRegistryName()).toString());
        ListNBT list = new ListNBT();
        for(int i = 0; i < materials.length; i++){
            list.add(StringNBT.valueOf(materials[i] == null ? "" : materials[i].getIdentifier().toString()));
        }
        nbt.put("materials", list);
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
        return bp;
    }

}
