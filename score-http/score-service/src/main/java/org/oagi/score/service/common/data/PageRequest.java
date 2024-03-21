package org.oagi.score.service.common.data;

import lombok.Data;

@Data
public class PageRequest {

    public static final PageRequest EMPTY_INSTANCE = new PageRequest();

    private String sortActive;
    private String sortDirection;
    private int pageIndex = -1;
    private int pageSize = -1;

    public void setSortActive(String sortActive) {
        if (sortActive != null && !"undefined".equals(sortActive)) {
            this.sortActive = sortActive;
        }
    }

    public void setSortDirection(String sortDirection) {
        if (sortDirection != null && !"undefined".equals(sortDirection)) {
            this.sortDirection = sortDirection;
        }
    }

    public int getOffset() {
        int offset = this.pageIndex * this.pageSize;
        if (offset <= 0) {
            return 0;
        }
        return offset;
    }
}
