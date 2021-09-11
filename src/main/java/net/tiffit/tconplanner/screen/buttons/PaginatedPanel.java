package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.tiffit.tconplanner.Config;
import net.tiffit.tconplanner.screen.PlannerPanel;
import net.tiffit.tconplanner.screen.PlannerScreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaginatedPanel<T extends Widget> extends PlannerPanel {

    private final List<T> allChildren = new ArrayList<>();
    private final String cachePrefix;
    private final int childWidth, childHeight, spacing, columns, rows, pageSize;
    private int totalPages;
    private float scrollPageWidth;

    public PaginatedPanel(int x, int y, int childWidth, int childHeight, int columns, int rows, int spacing, String cachePrefix, PlannerScreen parent) {
        super(x, y, (childWidth+spacing) * columns - spacing, (childHeight+spacing) * rows - spacing + 4, parent);
        this.childWidth = childWidth;
        this.childHeight = childHeight;
        this.spacing = spacing;
        this.columns = columns;
        this.rows = rows;
        this.pageSize = columns  * rows;
        this.cachePrefix = cachePrefix;
    }


    public void addChild(Widget widget){
        allChildren.add((T)widget);
    }

    public void sort(Comparator<T> comparator){allChildren.sort(comparator);}

    public void refresh(){
        refresh(parent.getCacheValue(cachePrefix + ".page", 0));
    }

    public void refresh(int page){
        totalPages = Math.max((int)Math.ceil(allChildren.size() / (float)pageSize), 1);
        if(page >= totalPages){
            setPage(totalPages - 1);
            return;
        }
        children.clear();
        children.addAll(allChildren.subList(page*pageSize, Math.min(allChildren.size(), (page+1)*pageSize)));
        scrollPageWidth = width/(float)totalPages;
        for (int i = 0; i < children.size(); i++) {
            Widget widget = children.get(i);
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
        super.render(stack, mouseX, mouseY, p_230430_4_);
        if(totalPages > 1) {
            int scrollY = y + height - 3;
            int page = parent.getCacheValue(cachePrefix + ".page", 0);
            Screen.fill(stack, x, scrollY, x + width, scrollY + 3, 0x0f_ffffff + (isHovered ? 0x0a_000000 : 0));
            Screen.fill(stack, x + (int)(scrollPageWidth*page), scrollY, x + (int)(scrollPageWidth*(page+1)), scrollY + 3, 0x0f_ffffff + (isHovered ? 0x0f_000000 : 0));
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(totalPages > 1) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + height - 3 && mouseY <= y + height) {
                int clickedPage = (int) Math.min(((mouseX - x) / width) * totalPages, totalPages - 1);
                setPage(clickedPage);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        boolean result = false;
        double scrollAmount = scroll * Config.CONFIG.scrollDirection.get().mult;
        int currentPage = parent.getCacheValue(cachePrefix + ".page", 0);
        if(scrollAmount > 0 && currentPage < totalPages){
            setPage(currentPage + 1);
            result = true;
        }else if(scrollAmount < 0 && currentPage > 0){
            setPage(currentPage - 1);
            result = true;
        }
        if(super.mouseScrolled(mouseX, mouseY, scroll))result = true;
        return result;
    }
}
