package net.tiffit.tconplanner;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.screen.ext.ExtIconButton;
import net.tiffit.tconplanner.util.Icon;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.tools.definition.PartRequirement;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;
import slimeknights.tconstruct.tables.inventory.table.tinkerstation.TinkerStationSlot;
import slimeknights.tconstruct.tools.TinkerToolParts;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EventListener {
    private static final Icon plannerIcon = new Icon(0, 0);

    private static StationSlotLayout layout = null;

    @SubscribeEvent
    public static void onScreenInit(GuiScreenEvent.InitGuiEvent.Post e){
        if(e.getGui() instanceof TinkerStationScreen){
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            PlannerData data = TConPlanner.DATA;
            try {
                data.firstLoad();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            updateLayout(screen);
            int x = screen.cornerX + Config.CONFIG.buttonX.get(), y = screen.cornerY + Config.CONFIG.buttonY.get();
            e.addWidget(new ExtIconButton(x, y, plannerIcon, TranslationUtil.createComponent("plannerbutton"), action -> {
                Minecraft mc = screen.getMinecraft();
                mc.setScreen(new PlannerScreen(screen));
            }, screen));
        }
    }

    @SubscribeEvent
    public static void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Post e){
        if(e.getGui() instanceof TinkerStationScreen) {
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            if(layout != null){
                Blueprint starred = TConPlanner.DATA.starred;
                MatrixStack ms = e.getMatrixStack();
                for (int i = 0; i < layout.getInputSlots().size(); i++) {
                    LayoutSlot slot = layout.getInputSlots().get(i);
                    int slotX = slot.getX() + screen.cornerX, slotY = slot.getY() + screen.cornerY;
                    IToolPart part = starred.parts[i];
                    if(e.getMouseX() > slotX && e.getMouseY() > slotY && e.getMouseX() < slotX + 16 && e.getMouseY() < slotY + 16){
                        if(screen.getSlotUnderMouse() != null){
                            ItemStack stack = screen.getSlotUnderMouse().getItem();
                            if(stack.isEmpty()){
                                ms.pushPose();
                                ms.translate(0, 0, 101);
                                Screen.fill(ms, slotX, slotY, slotX + 16, slotY + 16, 0x5a000050);
                                ms.popPose();
                                screen.renderComponentTooltip(ms, Lists.newArrayList(new StringTextComponent(TextFormatting.DARK_RED + "Missing Tool Part:"), part.withMaterialForDisplay(starred.materials[i].getIdentifier()).getDisplayName()), e.getMouseX(), e.getMouseY());
                            }else if(!starred.materials[i].getIdentifier().equals(part.getMaterialId(stack).orElse(null))){
                                ms.pushPose();
                                ms.translate(0, 0, 101);
                                Screen.fill(ms, slotX, slotY, slotX + 16, slotY + 16, 0x5aff0000);
                                ms.popPose();
                                screen.renderComponentTooltip(ms, Lists.newArrayList(new StringTextComponent(TextFormatting.DARK_RED + "Incorrect tool part! Should be:"), part.withMaterialForDisplay(starred.materials[i].getIdentifier()).getDisplayName()), e.getMouseX(), e.getMouseY() - 30);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenMouseClick(GuiScreenEvent.MouseClickedEvent.Post e){
        if(e.getGui() instanceof TinkerStationScreen) {
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            updateLayout(screen);
        }
    }

    private static void updateLayout(TinkerStationScreen screen){
        PlannerData data = TConPlanner.DATA;
        try {
            if(data.starred != null){
                Field currentLayoutField = TinkerStationScreen.class.getDeclaredField("currentLayout");
                currentLayoutField.setAccessible(true);
                StationSlotLayout layout = (StationSlotLayout) currentLayoutField.get(screen);
                if(layout == data.starred.tool.getLayout()){
                    EventListener.layout = layout;
                }else{
                    EventListener.layout = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
