package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class MatPageButton extends Button {
    private final boolean right;
    private final PlannerScreen parent;
    public MatPageButton(int x, int y, int change, PlannerScreen parent) {
        super(x, y, 38, 20, new StringTextComponent(""), button -> {parent.materialPage += change; parent.refresh();});
        right = change > 0;
        this.parent = parent;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        parent.blit(stack, x, y, right ? 176 : 214, active ? 20 : 0, width, height);
    }

}
