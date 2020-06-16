package me.gimme.gimmecore.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Pageifier {

    /**
     * Takes a list and returns a sublist (page) of it at the specified page number (first page = 1).
     * After splitting it into sections of the specified page size, returns the requested page (first page = 1) with
     * content, page number and total pages. If page number <= 0 or if page number > total pages,
     * an empty content list is returned.
     *
     * @param list     the list to get the page from
     * @param pageSize objects per page or <= 0 for unlimited
     * @param page     the page number to get the page at
     * @param <E>      the type of the content
     * @return the sublist (page) with page number and total pages with the specified page size
     */
    @NotNull
    public static <E> Pageifier.PageResult<E> getPage(@NotNull List<E> list, int pageSize, int page) {
        if (pageSize <= 0) pageSize = list.size();

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        int totalPages = list.size() / pageSize + ((list.size() % pageSize == 0) ? 0 : 1);

        List<E> content;
        if (startIndex < 0) {
            content = new ArrayList<>();
        } else {
            content = list.subList(startIndex, endIndex);
        }

        return new PageResult<>(content, page, totalPages);
    }

    public static class PageResult<E> {
        public final List<E> content;
        public final int page;
        public final int totalPages;

        private PageResult(@NotNull List<E> content, int page, int totalPages) {
            this.content = content;
            this.page = page;
            this.totalPages = totalPages;
        }
    }

}
