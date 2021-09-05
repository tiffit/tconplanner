package net.tiffit.tconplanner.data;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.tiffit.tconplanner.PlannerScreen;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;

import java.util.*;
import java.util.stream.Collectors;

public class Blueprint {

    public final SlotInformation toolSlotInfo;
    public final ItemStack toolStack;
    public final IModifiable toolItem;
    public final ToolDefinition toolDefinition;
    public final IToolPart[] parts;
    public final IMaterial[] materials;
    public final HashMap<ModifierInfo, Integer> modifiers = new HashMap<>();

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
            for (Map.Entry<ModifierInfo, Integer> entry : modifiers.entrySet()) {
                ModifierInfo info = entry.getKey();
                int level = entry.getValue();
                stack.addModifier(info.modifier, level);
                if (info.count != null) {
                    stack.getPersistentData().addSlots(info.count.getType(), -info.count.getCount() * level);
                }
            }
        }
        stack.rebuildStats();
        return stack.createStack();
    }

    public ITextComponent getModifierDisplayName(ModifierInfo info){
        return info.modifier.getDisplayName(modifiers.getOrDefault(info, 1));
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
        ListNBT matList = new ListNBT();
        for(int i = 0; i < materials.length; i++){
            matList.add(StringNBT.valueOf(materials[i] == null ? "" : materials[i].getIdentifier().toString()));
        }
        nbt.put("materials", matList);

        ListNBT modList = new ListNBT();
        modifiers.forEach((info, level) -> {
            CompoundNBT mod = new CompoundNBT();
            mod.putString("mod", info.modifier.getId().toString());
            mod.putInt("level", level);
            modList.add(mod);
        });
        nbt.put("modifiers", modList);
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

        ListNBT modifiers = tag.getList("modifiers", 10);
        Map<ModifierId, IDisplayModifierRecipe> recipesMap = PlannerScreen.getModifierRecipes().stream().collect(Collectors.toMap(recipe -> recipe.getDisplayResult().getModifier().getId(), recipe -> recipe));
        for(int i = 0; i < modifiers.size(); i++){
            CompoundNBT compound = modifiers.getCompound(i);
            int level = compound.getInt("level");
            ModifierId modId = new ModifierId(compound.getString("mod"));
            if(recipesMap.containsKey(modId)) {
                bp.modifiers.put(new ModifierInfo(recipesMap.get(modId)), level);
            }
        }
        return bp;
    }

}
