package net.tiffit.tconplanner.screen.ext;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.tiffit.tconplanner.EventListener;
import net.tiffit.tconplanner.TConPlanner;
import net.tiffit.tconplanner.screen.buttons.BookmarkedButton;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtItemStackButton extends Button {
    public static ResourceLocation BACKGROUND = new ResourceLocation("tconstruct", "textures/gui/tinker_station.png");

    private final ItemStack stack;
    private final Screen screen;
    private final List<ITextComponent> tooltips;

    public ExtItemStackButton(int x, int y, ItemStack stack, List<ITextComponent> tooltips, IPressable action, Screen screen) {
        super(x, y, 16, 16, new StringTextComponent(""), action, (btn, ms, mx, my) -> {});
        this.stack = stack;
        this.screen = screen;
        this.tooltips = tooltips == null ? Collections.emptyList() : tooltips;
    }

    @Override
    public void renderButton(MatrixStack ms, int mouseX, int mouseY, float p_230431_4_) {
        Minecraft mc = screen.getMinecraft();
        mc.getTextureManager().bind(BACKGROUND);
        screen.blit(ms, x - 1, y - 1, 194, 0, 18, 18);
        if(!isHovered()){
            AbstractGui.fill(ms, x, y, x + 16, y + 16, 0xff_a29b81);
        }
        mc.getItemRenderer().renderGuiItem(stack, x, y);
        ms.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.color4f(1f, 1f, 1f, 0.6f);
        BookmarkedButton.STAR_ICON.render(screen, ms, x + 2, y + 2);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        ms.popPose();
        if (this.isHovered()) {
            EventListener.postRenderQueue.offer(() -> {
                List<ITextComponent> result = Stream.concat(screen.getTooltipFromItem(stack).stream(), tooltips.stream()).collect(Collectors.toList());
                screen.renderWrappedToolTip(ms, result, mouseX, mouseY, mc.font);
            });
        }
    }
}

