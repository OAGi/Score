package org.oagi.score.repo;

import java.util.List;

public class PaginationResponse<E> {

    private final int pageCount;
    private final List<E> result;

    public PaginationResponse(int pageCount, List<E> result) {
        this.pageCount = pageCount;
        this.result = result;
    }

    public int getPageCount() {
        return pageCount;
    }

    public List<E> getResult() {
        return result;
    }
}
