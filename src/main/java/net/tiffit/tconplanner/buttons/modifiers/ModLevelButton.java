package net.tiffit.tconplanner.buttons.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.data.ModifierInfo;

import java.util.HashMap;

public class ModLevelButton  extends Button {

    private final PlannerScreen parent;
    private final int change;
    private boolean disabled = false;
    private ITextComponent tooltip;

    public ModLevelButton(int x, int y, int change, PlannerScreen parent) {
        super(x, y, 18, 18, new StringTextComponent(""), e -> {});
        this.parent = parent;
        this.change = change;
    }

    public void disable(ITextComponent tooltip){
        this.tooltip = tooltip;
        disabled = true;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float p_230431_4_) {
        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        parent.blit(stack, x, y, change > 0 ? 205 : 223, disabled  ? 135 : 117, width, height);
        if(isHovered()){
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        if(!disabled) {
            HashMap<ModifierInfo, Integer> mods = parent.blueprint.modifiers;
            mods.put(parent.selectedModifier, mods.getOrDefault(parent.selectedModifier, 1) + change);
            parent.refresh();
        }
    }

    @Override
    public void renderToolTip(MatrixStack stack, int mouseX, int mouseY) {
        if(disabled) {
            parent.postRenderTasks.add(() -> parent.renderTooltip(stack, tooltip, mouseX, mouseY));
        }
    }

    @Override
    public void playDownSound(SoundHandler handler) {
        if(!disabled)super.playDownSound(handler);
    }
}
