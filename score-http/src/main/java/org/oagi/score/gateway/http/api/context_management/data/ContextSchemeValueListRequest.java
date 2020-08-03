package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.common.data.PageRequest;

@Data
public class ContextSchemeValueListRequest {

    private String value;

    private PageRequest pageRequest;

}
