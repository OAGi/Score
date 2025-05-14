package org.oagi.score.gateway.http.common.model.base;

import java.util.List;

public class PaginationResponse<T> extends Response {

    private List<T> results;
    private int page;
    private int size;
    private int length;

    public PaginationResponse(List<T> results, int page, int size, int length) {
        this.results = results;
        this.page = page;
        this.size = size;
        this.length = length;
    }

    public List<T> getResults() {
        return results;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getLength() {
        return length;
    }

}
