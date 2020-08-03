package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.common.data.PageRequest;

@Data
public class ContextCategoryListRequest {

    private String name;
    private String description;

    private PageRequest pageRequest;

}
