package net.tiffit.tconplanner.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TCTool {
    private static List<TCTool> ALL_TOOLS = null;
    private final StationSlotLayout layout;

    private TCTool(StationSlotLayout layout){
        this.layout = layout;
    }

    public ITextComponent getName(){
        return layout.getDisplayName();
    }

    public ITextComponent getDescription(){
        return layout.getDescription();
    }

    public ItemStack getRenderTool(){
        return layout.getIcon().getValue(ItemStack.class);
    }

    public IModifiable getModifiable(){
        return (IModifiable) getRenderTool().getItem();
    }

    public Item getItem(){
        return getRenderTool().getItem();
    }

    public List<TCSlotPos> getSlotPos(){
        return layout.getInputSlots().stream().map(TCSlotPos::new).collect(Collectors.toList());
    }

    public static List<TCTool> getTools(){
        if(ALL_TOOLS == null){
            ALL_TOOLS = StationSlotLayoutLoader.getInstance().getSortedSlots().stream()
                    .filter(layout -> TinkerTags.Items.MODIFIABLE.contains(Objects.requireNonNull(layout.getIcon().getValue(ItemStack.class)).getItem()))
                    .map(TCTool::new).collect(Collectors.toList());
        }
        return ALL_TOOLS;
    }

}
