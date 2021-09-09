package net.tiffit.tconplanner.screen.buttons.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;

public class ModExitButton  extends Button {

    private final PlannerScreen parent;

    public ModExitButton(int x, int y, PlannerScreen parent) {
        super(x, y, 58, 18, TranslationUtil.createComponent("modifiers.exit"), e -> {});
        this.parent = parent;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        RenderSystem.enableBlend();
        PlannerScreen.bindTexture();
        parent.blit(stack, x, y, 176, 183, width, height);
        Screen.drawCenteredString(stack, Minecraft.getInstance().font, getMessage(), x + width/2, y + 5, isHovered ? 0xffffffff : 0xa0ffffff);

    }

    @Override
    public void onPress() {
        parent.selectedModifier = null;
        parent.refresh();
    }
}
