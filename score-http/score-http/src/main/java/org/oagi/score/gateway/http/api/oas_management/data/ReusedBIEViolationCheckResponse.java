package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;
import org.oagi.score.repo.api.base.Response;

import java.util.List;

@Data
public class ReusedBIEViolationCheckResponse extends Response {
    private List<String> errorMessages;

}
