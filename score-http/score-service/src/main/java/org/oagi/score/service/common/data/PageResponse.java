package org.oagi.score.service.common.data;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> list;
    private int page;
    private int size;
    private int length;

}
