package org.oagi.score.gateway.http.common.model;

import java.util.List;

public record PageRequest(int pageIndex, int pageSize, List<Sort> sorts) {

    public static int DEFAULT_PAGE_INDEX = 0;
    public static int DEFAULT_PAGE_SIZE = 10;

    public PageRequest() {
        this(DEFAULT_PAGE_INDEX, DEFAULT_PAGE_SIZE, List.of());
    }

    public PageRequest(int pageIndex, int pageSize, List<Sort> sorts) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.sorts = sorts;
    }

    public int pageOffset() {
        if (pageIndex < 0 || pageSize < 0) {
            return -1;
        }
        return pageIndex() * pageSize();
    }

    public boolean isPagination() {
        return this.pageOffset() > -1;
    }

}
