package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditAgencyIdList {

    private long agencyIdListId;
    private boolean isDefault;
    private String agencyIdListName;

}
