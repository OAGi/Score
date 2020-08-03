package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class DeleteContextSchemeRequest {

    private List<Long> ctxSchemeIds = Collections.emptyList();
}
