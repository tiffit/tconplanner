package net.tiffit.tconplanner.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import net.tiffit.tconplanner.PlannerScreen;

import java.util.ArrayList;
import java.util.List;

public class PaginatedButtonGroup extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private final String cachePrefix;
    private final PlannerScreen parent;
    private final int childWidth, childHeight, spacing, columns, rows, pageSize;
    private List<Widget> onScreen = new ArrayList<>();
    private int totalPages;
    private float scrollPageWidth;

    public PaginatedButtonGroup(int x, int y, int childWidth, int childHeight, int columns, int rows, int spacing, String cachePrefix, PlannerScreen parent) {
        super(x, y, (childWidth+spacing) * columns - spacing, (childHeight+spacing) * rows - spacing + 4, new StringTextComponent(""));
        this.parent = parent;
        this.childWidth = childWidth;
        this.childHeight = childHeight;
        this.spacing = spacing;
        this.columns = columns;
        this.rows = rows;
        this.pageSize = columns  * rows;
        this.cachePrefix = cachePrefix;
    }

    public void addChild(Widget widget){
        children.add(widget);
    }

    public void refresh(){
        refresh(parent.getCacheValue(cachePrefix + ".page", 0));
    }

    public void refresh(int page){
        totalPages = (int)Math.ceil(children.size() / (float)pageSize);
        if(page >= totalPages){
            setPage(totalPages - 1);
            return;
        }
        onScreen = children.subList(page*pageSize, Math.min(children.size(), (page+1)*pageSize));
        scrollPageWidth = width/(float)totalPages;
        for (int i = 0; i < onScreen.size(); i++) {
            Widget widget = onScreen.get(i);
            widget.x = x + (i % columns) * (childWidth+spacing);
            widget.y = y + (i / columns) * (childHeight+spacing);
        }
    }

    private void setPage(int page){
        parent.setCacheValue(cachePrefix + ".page", page);
        refresh(page);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float p_230430_4_) {
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        for (Widget child : onScreen) {
            child.render(stack, mouseX, mouseY, p_230430_4_);
        }
        if(totalPages > 1) {
            int scrollY = y + height - 3;
            int page = parent.getCacheValue(cachePrefix + ".page", 0);
            Screen.fill(stack, x, scrollY, x + width, scrollY + 3, 0x0f_ffffff + (isHovered ? 0x0a_000000 : 0));
            Screen.fill(stack, x + (int)(scrollPageWidth*page), scrollY, x + (int)(scrollPageWidth*(page+1)), scrollY + 3, 0x0f_ffffff + (isHovered ? 0x0f_000000 : 0));
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(mouseX >= x && mouseX <= x + width && mouseY >= y + height - 3 && mouseY <= y + height){
            int clickedPage = (int)Math.min(((mouseX - x)/width) * totalPages, totalPages - 1);
            setPage(clickedPage);
            return true;
        }
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.mouseClicked(mouseX, mouseY, button))result = true;
        }
        return result;
    }

    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_))result = true;
        }
        return result;
    }

    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_))result = true;
        }
        return result;
    }

    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_))result = true;
        }
        return result;
    }

    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_))result = true;
        }
        return result;
    }

    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_))result = true;
        }
        return result;
    }

    public boolean charTyped(char p_231042_1_, int p_231042_2_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.charTyped(p_231042_1_, p_231042_2_))result = true;
        }
        return result;
    }

    public boolean changeFocus(boolean p_231049_1_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.changeFocus(p_231049_1_))result = true;
        }
        return result;
    }

    public boolean isMouseOver(double p_231047_1_, double p_231047_3_) {
        boolean result = false;
        for (Widget child : onScreen) {
            if(child.isMouseOver(p_231047_1_, p_231047_3_))result = true;
        }
        return result;
    }
}
