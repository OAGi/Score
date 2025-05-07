package org.oagi.score.gateway.http.api.log_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.PageRequest;

@Data
public class LogListRequest {

    private String reference;
    private PageRequest pageRequest;

}
