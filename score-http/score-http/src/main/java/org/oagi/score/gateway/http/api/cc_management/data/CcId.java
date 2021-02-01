package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CcId {
    private String type;
    private BigInteger manifestId;

    public CcId(CcType type, BigInteger manifestId) {
        this.type = type.name().toLowerCase();
        this.manifestId = manifestId;
    }
}
