package org.oagi.score.service.bie;

import lombok.Data;
import org.oagi.score.service.bie.model.BieUpliftingValidation;

import java.util.List;

@Data
public class UpliftValidationResponse {
    private List<BieUpliftingValidation> validations;
}
