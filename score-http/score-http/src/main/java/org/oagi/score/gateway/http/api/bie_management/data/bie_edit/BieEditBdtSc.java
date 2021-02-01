package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.helper.Utility;

import java.math.BigInteger;

@Data
public class BieEditBdtSc {

    private BigInteger dtScManifestId;
    private String guid;
    private String propertyTerm;
    private String representationTerm;
    private BigInteger ownerDtId;

    public String getName() {
        String name;
        if (getRepresentationTerm().equalsIgnoreCase("Text") ||
                getPropertyTerm().contains(getRepresentationTerm())) {
            name = Utility.spaceSeparator(getPropertyTerm());
        } else {
            name = Utility.spaceSeparator(getPropertyTerm().concat(getRepresentationTerm()));
        }
        return name;
    }
}
