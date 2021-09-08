package net.tiffit.tconplanner.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.buttons.IconButton;
import net.tiffit.tconplanner.buttons.OutputToolWidget;
import net.tiffit.tconplanner.buttons.ToolPartButton;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotPosition;

import java.util.List;

public class ToolTopPanel extends PlannerPanel{

    private static final int partsOffsetX = 13, partsOffsetY = 15;

    public ToolTopPanel(int x, int y, int width, int height, ItemStack result, ToolStack tool, PlannerData data, PlannerScreen parent) {
        super(x, y, width, height, parent);
        //Add the tool part buttons
        Blueprint blueprint = parent.blueprint;
        List<SlotPosition> positions = blueprint.toolSlotInfo.getPoints();
        for(int i = 0; i < blueprint.parts.length; i++){
            SlotPosition pos = positions.get(i);
            IToolPart part = blueprint.parts[i];
            addChild(new ToolPartButton(i, pos.getX() + partsOffsetX, pos.getY() + partsOffsetY, part, blueprint.materials[i], parent));
        }

        //Add randomize tool button
        addChild(new IconButton(parent.guiWidth - 70, 88, 176, 104,
                TranslationUtil.createComponent("randomize"), parent, e -> parent.randomize())
                .withSound(SoundEvents.ENDERMAN_TELEPORT));

        if(tool != null){
            addChild(new OutputToolWidget(parent.guiWidth - 34, 58, result, parent));
            boolean bookmarked = data.isBookmarked(blueprint);
            addChild(new IconButton(parent.guiWidth - 33, 88, 190 + (bookmarked ? 12 : 0), 78,
                    TranslationUtil.createComponent(bookmarked ? "bookmark.remove" : "bookmark.add"), parent, e -> {if(bookmarked) parent.unbookmarkCurrent(); else parent.bookmarkCurrent();})
                    .withSound(bookmarked ? SoundEvents.UI_STONECUTTER_TAKE_RESULT : SoundEvents.BOOK_PAGE_TURN));
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float p_230430_4_) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(x + partsOffsetX + 7, y + partsOffsetY + 22, -100);
        RenderSystem.scalef(3.7F, 3.7F, 1.0F);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(parent.blueprint.toolStack, 0, 0);
        RenderSystem.popMatrix();
        PlannerScreen.bindTexture();
        RenderSystem.pushMatrix();
        RenderSystem.translated(0, 0, 1);
        int boxX = 13, boxY = 24, boxL = 81;
        if(mouseX > boxX + x && mouseY > boxY + y && mouseX < boxX + x + boxL && mouseY < boxY + y + boxL)
            RenderSystem.color4f(1f, 1f, 1f, 0.75f);
        else RenderSystem.color4f(1f, 1f, 1f, 0.5f);
        RenderSystem.enableBlend();
        this.blit(stack, x + boxX, y + boxY, boxX, boxY, boxL, boxL);
        RenderSystem.popMatrix();
        super.render(stack, mouseX, mouseY, p_230430_4_);
    }
}
