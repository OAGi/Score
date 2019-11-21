package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.srt.gateway.http.helper.Utility;

@Data
public class BieEditBdtSc {

    private long dtScId;
    private String guid;
    private String propertyTerm;
    private String representationTerm;
    private long ownerDtId;

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
