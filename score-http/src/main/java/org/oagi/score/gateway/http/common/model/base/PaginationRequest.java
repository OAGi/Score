package org.oagi.score.gateway.http.common.model.base;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.SortDirection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PaginationRequest<T> extends Request {

    public static int DEFAULT_PAGE_INDEX = 0;
    public static int DEFAULT_PAGE_SIZE = 10;

    private final Class<T> type;
    private List<String> sortActives;
    private List<SortDirection> sortDirections;
    private int pageIndex = DEFAULT_PAGE_INDEX;
    private int pageSize = DEFAULT_PAGE_SIZE;

    public PaginationRequest(ScoreUser requester, Class<T> type) {
        super(requester);
        this.type = type;
    }

    public Class<T> getType() {
        return this.type;
    }

    public List<String> getSortActives() {
        if (this.sortActives != null && !this.sortActives.isEmpty()) {
            return this.sortActives;
        }
        return Collections.emptyList();
    }

    public void setSortActives(List<String> sortActives) {
        this.sortActives = sortActives;
    }

    public String getSortActive() {
        List<String> sortActives = getSortActives();
        return (!sortActives.isEmpty()) ? sortActives.get(0) : null;
    }

    public void setSortActive(String sortActive) {
        setSortActives(Arrays.asList(sortActive));
    }

    public List<SortDirection> getSortDirections() {
        if (this.sortDirections != null && !this.sortDirections.isEmpty()) {
            return this.sortDirections;
        }
        return Collections.emptyList();
    }

    public void setSortDirections(List<SortDirection> sortDirections) {
        this.sortDirections = sortDirections;
    }

    public SortDirection getSortDirection() {
        List<SortDirection> sortDirections = getSortDirections();
        return (!sortDirections.isEmpty()) ? sortDirections.get(0) : SortDirection.ASC;
    }

    public void setSortDirection(SortDirection sortDirection) {
        setSortDirections(Arrays.asList(sortDirection));
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public final int getPageOffset() {
        if (this.getPageIndex() == -1 && this.getPageSize() == -1) {
            return -1;
        }
        return this.getPageIndex() * this.getPageSize();
    }

    public final boolean isPagination() {
        return this.getPageOffset() > -1;
    }
}
