package org.oagi.srt.gateway.http.api.context_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class DeleteContextCategoryRequest {

    private List<Long> ctxCategoryIds = Collections.emptyList();
}
