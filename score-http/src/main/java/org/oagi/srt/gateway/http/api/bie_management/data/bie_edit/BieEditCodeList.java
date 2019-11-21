package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditCodeList {

    private long codeListId;
    private Long basedCodeListId;
    private boolean isDefault;
    private String codeListName;

}
