package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CreateInheritedBieRequest {

    private BigInteger basedTopLevelAsbiepId;

}
