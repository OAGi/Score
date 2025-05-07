package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingValidation;

import java.util.List;

@Data
public class UpliftValidationResponse {
    private List<BieUpliftingValidation> validations;
}
