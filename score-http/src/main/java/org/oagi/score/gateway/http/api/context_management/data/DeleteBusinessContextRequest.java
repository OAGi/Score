package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class DeleteBusinessContextRequest {

    private List<Long> bizCtxIds = Collections.emptyList();
}
