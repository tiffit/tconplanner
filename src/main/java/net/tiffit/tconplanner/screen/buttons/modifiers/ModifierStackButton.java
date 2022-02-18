package net.tiffit.tconplanner.screen.buttons.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import java.util.ArrayList;
import java.util.List;

public class ModifierStackButton extends Button {

    private final Modifier modifier;
    private final IDisplayModifierRecipe recipe;
    private final ModifierInfo modifierInfo;
    private final PlannerScreen parent;
    private final ITextComponent displayName;
    private final ItemStack display;
    private final int index;

    public ModifierStackButton(ModifierInfo modifierInfo, int index, int level, ItemStack display, PlannerScreen parent) {
        super(0, 0, 100, 18, new StringTextComponent(""), e -> {
        });
        this.modifierInfo = modifierInfo;
        this.parent = parent;
        this.modifier = modifierInfo.modifier;
        this.recipe = modifierInfo.recipe;
        this.display = display;
        this.index = index;
        displayName = modifier.getDisplayName(level);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        if(parent.selectedModifierStackIndex == index){
            RenderSystem.color4f(255/255f, 200/255f, 0f, 1f);
        }
        parent.blit(stack, x, y, 0, 224, 100, 18);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(display, x + 1, y + 1);
        FontRenderer font = Minecraft.getInstance().font;
        stack.pushPose();
        stack.translate(x + 20, y + 2, 0);
        float nameWidth = font.width(displayName);
        int maxWidth = width - 22;
        if (nameWidth > maxWidth) {
            float scale = maxWidth / nameWidth;
            stack.scale(scale, scale, 1);
        }
        Screen.drawString(stack, font, displayName, 0, 0, 0xff_ff_ff_ff);
        stack.popPose();

        stack.pushPose();
        stack.translate(x + 20, y + 11, 0);
        stack.scale(0.5f, 0.5f, 1);
        if (recipe.getSlots() != null) {
            SlotType.SlotCount count = recipe.getSlots();
            IFormattableTextComponent text = count.getCount() == 1 ? TranslationUtil.createComponent("modifiers.usedslot", count.getType().getDisplayName()) :
                    TranslationUtil.createComponent("modifiers.usedslots", count.getCount(), count.getType().getDisplayName());
            Screen.drawString(stack, font, text, 0, 0, 0xff_ff_ff_ff);
        }
        stack.popPose();
        if (isHovered) {
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> {
            List<ITextComponent> tooltips = new ArrayList<>(modifier.getDescriptionList());
            parent.renderComponentTooltip(stack, tooltips, mouseX, mouseY);
        });
    }

    @Override
    public void onPress() {
        parent.selectedModifierStackIndex = index;
        parent.refresh();
    }
}
