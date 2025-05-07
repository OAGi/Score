package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.base.Response;

import java.util.List;

@Data
public class ReusedBIEViolationCheckResponse extends Response {

    private List<String> errorMessages;

}
