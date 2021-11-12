package net.tiffit.tconplanner.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TCTool {
    private static List<TCTool> ALL_TOOLS = null;
    private final SlotInformation info;

    private TCTool(SlotInformation info){
        this.info = info;
    }

    public ITextComponent getDescription(){
        return info.getItem().getDescription();
    }

    public ItemStack getRenderTool(){
        return info.getToolForRendering();
    }

    public IModifiable getModifiable(){
        return (IModifiable) info.getItem();
    }

    public Item getItem(){
        return info.getItem();
    }

    public List<TCSlotPos> getSlotPos(){
        return info.getPoints().stream().map(TCSlotPos::new).collect(Collectors.toList());
    }

    public static List<TCTool> getTools(){
        if(ALL_TOOLS == null){
            ALL_TOOLS = SlotInformationLoader.getSlotInformationList().stream()
                    .filter(info -> !info.isRepair()).map(TCTool::new).collect(Collectors.toList());
        }
        return ALL_TOOLS;
    }

}
