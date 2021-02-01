package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CreateExtensionResponse {
    public boolean canEdit;
    public boolean canView;
    public BigInteger extensionId = BigInteger.ZERO;
}
