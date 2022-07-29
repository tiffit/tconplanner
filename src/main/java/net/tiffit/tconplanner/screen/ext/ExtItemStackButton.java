package net.tiffit.tconplanner.screen.ext;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtItemStackButton extends Button {

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
        screen.getMinecraft().getItemRenderer().renderGuiItem(stack, x, y);
        if (this.isHovered()) {
            List<ITextComponent> result = Stream.concat(screen.getTooltipFromItem(stack).stream(), tooltips.stream()).collect(Collectors.toList());
            screen.renderWrappedToolTip(ms, result, mouseX, mouseY, screen.getMinecraft().font);
        }
    }
}

