package net.tiffit.tconplanner;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.screen.buttons.BookmarkedButton;
import net.tiffit.tconplanner.screen.ext.ExtIconButton;
import net.tiffit.tconplanner.screen.ext.ExtItemStackButton;
import net.tiffit.tconplanner.util.Icon;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tables.client.inventory.SlotButtonItem;
import slimeknights.tconstruct.tables.client.inventory.module.TinkerStationButtonsScreen;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EventListener {
    private static final Icon plannerIcon = new Icon(0, 0);
    private static final Icon importIcon = new Icon(8, 0);

    public static final Queue<Runnable> postRenderQueue = new LinkedBlockingQueue<>();

    private static StationSlotLayout layout = null;
    private static boolean starredLayout = false;
    private static final Field currentLayoutField;
    private static SlotButtonItem starredButton = null;
    private static boolean forceNextUpdate = false;
    private static TinkerStationButtonsScreen buttonScreen;
    private static final Field buttonsScreenField;

    static {
        try {
            currentLayoutField = TinkerStationScreen.class.getDeclaredField("currentLayout");
            currentLayoutField.setAccessible(true);

            buttonsScreenField = TinkerStationScreen.class.getDeclaredField("buttonsScreen");
            buttonsScreenField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onScreenInit(GuiScreenEvent.InitGuiEvent.Post e) {
        postRenderQueue.clear();
        if (e.getGui() instanceof TinkerStationScreen) {
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            Minecraft mc = screen.getMinecraft();
            PlannerData data = TConPlanner.DATA;
            try {
                data.firstLoad();
                buttonScreen = (TinkerStationButtonsScreen)buttonsScreenField.get(screen);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            updateLayout(screen, true);
            forceNextUpdate = true;
            int x = screen.cornerX + Config.CONFIG.buttonX.get(), y = screen.cornerY + Config.CONFIG.buttonY.get();
            e.addWidget(new ExtIconButton(x, y, plannerIcon, TranslationUtil.createComponent("plannerbutton"), action -> {
                mc.setScreen(new PlannerScreen(screen));
            }, screen));

            boolean isAnvil = Objects.equals(screen.getTileEntity().getBlockState().getBlock().getRegistryName(), new ResourceLocation("tconstruct:tinkers_anvil"));
            int importX = screen.cornerX, importY = screen.cornerY;
            importX += (isAnvil ? Config.CONFIG.importButtonXAnvil : Config.CONFIG.importButtonXStation).get();
            importY += (isAnvil ? Config.CONFIG.importButtonYAnvil : Config.CONFIG.importButtonYStation).get();
            e.addWidget(new ExtIconButton(importX, importY, importIcon, TranslationUtil.createComponent("importtool"), action -> {
                Slot slot = screen.getContainer().getSlot(0);
                if (!slot.getItem().isEmpty()) {
                    mc.setScreen(new PlannerScreen(screen, ToolStack.from(slot.getItem())));
                }
            }, screen).withEnabledFunc(() -> {
                if (!layout.isMain()) return false;
                Slot slot = screen.getContainer().getSlot(0);
                return !slot.getItem().isEmpty() && ToolStack.isInitialized(slot.getItem());
            }));
            if (data.starred != null) {
                List<ITextComponent> tooltip = new ArrayList<>();
                tooltip.add(new StringTextComponent("---------").withStyle(TextFormatting.GRAY));
                tooltip.add(TranslationUtil.createComponent("star.move").withStyle(TextFormatting.GOLD));
                tooltip.add(TranslationUtil.createComponent("star.ext_remove").withStyle(TextFormatting.RED));
                e.addWidget(new ExtItemStackButton(screen.cornerX + 83, screen.cornerY + 58, data.starred.createOutput(), tooltip, btn -> {
                    if (Screen.hasShiftDown()) {
                        btn.visible = btn.active = false;
                        starredLayout = false;
                        data.starred = null;
                        try {
                            data.save();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }else{
                        movePartsToSlots(screen, mc, data.starred);
                    }
                }, screen));
            }
        }
    }

    @SubscribeEvent
    public static void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (e.getGui() instanceof TinkerStationScreen) {
            TinkerStationScreen screen = (TinkerStationScreen) e.getGui();
            MatrixStack ms = e.getMatrixStack();
            if (starredLayout) {
                Blueprint starred = TConPlanner.DATA.starred;
                ItemStack carried = Objects.requireNonNull(screen.getMinecraft().player).inventory.getCarried();
                for (int i = 0; i < layout.getInputSlots().size(); i++) {
                    LayoutSlot slot = layout.getInputSlots().get(i);
                    int slotX = slot.getX() + screen.cornerX, slotY = slot.getY() + screen.cornerY;
                    IToolPart part = starred.parts[i];
                    boolean hovered = e.getMouseX() > slotX && e.getMouseY() > slotY && e.getMouseX() < slotX + 16 && e.getMouseY() < slotY + 16;
                    ItemStack stack = screen.getContainer().getSlot(i + 1).getItem();
                    MaterialId material = starred.materials[i].getIdentifier();
                    if (stack.isEmpty()) {
                        ms.pushPose();
                        ms.translate(0, 0, 101);
                        int color = carried.isEmpty() ? 0x5a000050 : isValidToolPart(carried, part, material) ? 0x5ae8b641 : 0x5aff0000;
                        Screen.fill(ms, slotX, slotY, slotX + 16, slotY + 16, color);
                        ms.popPose();
                        if (hovered) {
                            screen.renderComponentTooltip(ms, Lists.newArrayList(TranslationUtil.createComponent("star.slot.missing").withStyle(TextFormatting.DARK_RED), part.withMaterialForDisplay(material).getDisplayName()), e.getMouseX(), e.getMouseY());
                        }
                    } else if (!material.equals(part.getMaterialId(stack).orElse(null))) {
                        ms.pushPose();
                        ms.translate(0, 0, 101);
                        Screen.fill(ms, slotX, slotY, slotX + 16, slotY + 16, 0x7aff0000);
                        ms.popPose();
                        if (hovered) {
                            screen.renderComponentTooltip(ms, Lists.newArrayList(TranslationUtil.createComponent("star.slot.incorrect").withStyle(TextFormatting.DARK_RED), part.withMaterialForDisplay(material).getDisplayName()), e.getMouseX(), e.getMouseY() - 30);
                        }
                    }
                }
            }
            if(starredButton != null){
                ms.pushPose();
                ms.translate(starredButton.x + 10, starredButton.y + 10, 105);
                ms.scale(0.5f, 0.5f, 1);
                BookmarkedButton.STAR_ICON.render(screen, ms, 0, 0);
                ms.popPose();
            }
            while(postRenderQueue.size() > 0) {
                postRenderQueue.poll().run();
            }
        }
    }

    @SubscribeEvent
    public static void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Pre e) {
        if(e.getGui() instanceof TinkerStationScreen){
            postRenderQueue.clear();
            updateLayout((TinkerStationScreen) e.getGui(), forceNextUpdate);
        }
    }

    private static void updateLayout(TinkerStationScreen screen, boolean force) {
        try {
            StationSlotLayout newLayout = (StationSlotLayout) currentLayoutField.get(screen);
            if(!force && newLayout == layout)return;
            forceNextUpdate = false;
            layout = newLayout;
            PlannerData data = TConPlanner.DATA;
            boolean foundButton = false;
            if(data.starred != null){
                StationSlotLayout starredSlotLayout = data.starred.tool.getLayout();
                starredLayout = layout == starredSlotLayout;
                for (Widget widget : buttonScreen.getButtons()) {
                    if(widget instanceof SlotButtonItem){
                        SlotButtonItem button = (SlotButtonItem) widget;
                        if(starredSlotLayout == button.getLayout()){
                            starredButton = button;
                            foundButton = true;
                        }
                    }
                }
            }
            if(!foundButton){
                starredLayout = false;
                starredButton = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void movePartsToSlots(TinkerStationScreen screen, Minecraft mc, Blueprint starred){
        if(starred.tool.getLayout() != layout){
            screen.onToolSelection(starred.tool.getLayout());
            updateLayout(screen, true);
        }
        PlayerEntity player = mc.player;
        PlayerController pc = mc.gameMode;
        assert player != null && pc != null;
        for (int i = 0; i < layout.getInputSlots().size(); i++) {
            MaterialId material = starred.materials[i].getIdentifier();
            Container container = screen.getContainer();
            Slot tconSlot = container.getSlot(i + 1);
            if(tconSlot.getItem().isEmpty() && mc.player != null){
                for(int j = 0; j < container.slots.size(); j++){
                    Slot loopSlot = container.slots.get(j);
                    if(!(loopSlot.container instanceof PlayerInventory))continue;
                    ItemStack stackInInv = loopSlot.getItem();
                    if(isValidToolPart(stackInInv, starred.parts[i], material)){
                        handleMouseClick(pc, player, container, j, 0, ClickType.PICKUP);
                        handleMouseClick(pc, player, container, i + 1, 1, ClickType.PICKUP);
                        break;
                    }
                }
            }
        }
    }

    private static void handleMouseClick(PlayerController pc, PlayerEntity player, Container container, int slot, int mouseButton, ClickType clickType){
        pc.handleInventoryMouseClick(container.containerId, slot, mouseButton, clickType, player);
    }

    private static boolean isValidToolPart(ItemStack stack, IToolPart part, MaterialId material){
        if(stack.getItem() instanceof ToolPartItem){
            ToolPartItem toolPart = (ToolPartItem)stack.getItem();
            if(part.asItem() == toolPart && material.equals(toolPart.getMaterialId(stack).orElse(null))){
                return true;
            }
        }
        return false;
    }
}
