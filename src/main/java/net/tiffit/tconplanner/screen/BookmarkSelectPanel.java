package net.tiffit.tconplanner.screen;

import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.screen.buttons.BannerWidget;
import net.tiffit.tconplanner.screen.buttons.BookmarkedButton;
import net.tiffit.tconplanner.screen.buttons.PaginatedPanel;
import net.tiffit.tconplanner.util.TranslationUtil;

public class BookmarkSelectPanel extends PlannerPanel {

    public BookmarkSelectPanel(int x, int y, int width, int height, PlannerData data, PlannerScreen parent) {
        super(x, y, width, height, parent);
        addChild(new BannerWidget(5, 0, TranslationUtil.createComponent("banner.bookmarked"), parent));
        PaginatedPanel<BookmarkedButton> bookmarkGroup = new PaginatedPanel<>(0, 23, 18, 18, 5, 5, 2, "bookmarkedgroup", parent);
        addChild(bookmarkGroup);
        for (int i = 0; i < data.saved.size(); i++) {
            Blueprint bookmarked = data.saved.get(i);
            boolean starred = bookmarked.equals(data.starred);
            bookmarkGroup.addChild(new BookmarkedButton(i, bookmarked, starred, parent));
        }
        bookmarkGroup.refresh();
    }
}
