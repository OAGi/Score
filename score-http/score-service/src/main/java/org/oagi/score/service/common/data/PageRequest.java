package org.oagi.score.service.common.data;

import lombok.Data;

@Data
public class PageRequest {

    public static final PageRequest EMPTY_INSTANCE = new PageRequest();

    private String sortActive;
    private String sortDirection;
    private int pageIndex = -1;
    private int pageSize = -1;

    public int getOffset() {
        int offset = this.pageIndex * this.pageSize;
        if (offset <= 0) {
            return 0;
        }
        return offset;
    }
}
