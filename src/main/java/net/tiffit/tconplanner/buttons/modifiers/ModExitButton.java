package net.tiffit.tconplanner.buttons.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;

public class ModExitButton  extends Button {

    private final PlannerScreen parent;

    public ModExitButton(int x, int y, int width, int height, PlannerScreen parent) {
        super(x, y, width, height, TranslationUtil.createComponent("modifiers.exit"), e -> {});
        this.parent = parent;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        RenderSystem.enableBlend();
        Screen.fill(stack, x, y, x + width, y + height, 0xa0_ffffff);
        Screen.drawString(stack, Minecraft.getInstance().font, getMessage(), x, y, 0xffffffff);

    }

    @Override
    public void onPress() {
        parent.selectedModifier = null;
        parent.refresh();
    }
}
