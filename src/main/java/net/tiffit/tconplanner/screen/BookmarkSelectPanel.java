package net.tiffit.tconplanner.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.tiffit.tconplanner.PlannerScreen;
import net.tiffit.tconplanner.buttons.BannerWidget;
import net.tiffit.tconplanner.buttons.BookmarkedButton;
import net.tiffit.tconplanner.buttons.PaginatedPanel;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.util.TranslationUtil;

public class BookmarkSelectPanel extends PlannerPanel {

    public BookmarkSelectPanel(int x, int y, int width, int height, PlannerData data, PlannerScreen parent) {
        super(x, y, width, height, parent);
        addChild(new BannerWidget(5, 0, TranslationUtil.createComponent("banner.bookmarked"), parent));
        PaginatedPanel<BookmarkedButton> bookmarkGroup = new PaginatedPanel<>(0, 23, 18, 18, 5, 5, 2, "bookmarkedgroup", parent);
        addChild(bookmarkGroup);
        for (int i = 0; i < data.saved.size(); i++) {
            Blueprint bookmarked = data.saved.get(i);
            bookmarkGroup.addChild(new BookmarkedButton(i, bookmarked, parent));
        }
        bookmarkGroup.refresh();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float p_230430_4_) {
        //Screen.fill(stack, x, y, x + width, y + height, 0xff_ff_ff_ff);
        super.render(stack, mouseX, mouseY, p_230430_4_);
    }
}
