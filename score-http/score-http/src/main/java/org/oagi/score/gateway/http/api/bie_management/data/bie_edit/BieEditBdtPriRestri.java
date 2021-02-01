package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class BieEditBdtPriRestri {

    private List<BieEditXbt> xbtList = Collections.emptyList();
    private List<BieEditCodeList> codeLists = Collections.emptyList();
    private List<BieEditAgencyIdList> agencyIdLists = Collections.emptyList();

}
