package org.oagi.score.service.common.data;

import lombok.Data;
import org.oagi.score.repo.api.base.SortDirection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class PageRequest {

    public static final PageRequest EMPTY_INSTANCE = new PageRequest();

    private List<String> sortActives = new ArrayList<>();
    private List<SortDirection> sortDirections = new ArrayList<>();
    private int pageIndex = -1;
    private int pageSize = -1;

    public void setSortActive(String sortActive) {
        if (sortActive != null && !"undefined".equals(sortActive)) {
            setSortActives(Arrays.asList(sortActive));
        }
    }

    public String getSortActive() {
        if (!this.sortActives.isEmpty()) {
            return this.sortActives.get(0);
        }
        return null;
    }

    public void setSortDirection(String sortDirection) {
        if (sortDirection != null && !"undefined".equals(sortDirection)) {
            setSortDirections(Arrays.asList(SortDirection.valueOf(sortDirection.toUpperCase())));
        }
    }

    public String getSortDirection() {
        if (!this.sortDirections.isEmpty()) {
            return this.sortDirections.get(0).name().toLowerCase();
        }
        return null;
    }

    public List<String> getSortActives() {
        return sortActives;
    }

    public void setSortActives(List<String> sortActives) {
        this.sortActives = sortActives;
    }

    public List<SortDirection> getSortDirections() {
        return sortDirections;
    }

    public void setSortDirections(List<SortDirection> sortDirections) {
        this.sortDirections = sortDirections;
    }

    public int getOffset() {
        int offset = this.pageIndex * this.pageSize;
        if (offset <= 0) {
            return 0;
        }
        return offset;
    }
}
