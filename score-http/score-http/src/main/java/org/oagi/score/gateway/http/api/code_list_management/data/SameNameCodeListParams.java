package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;

@Data
public class SameNameCodeListParams {

    private long releaseId;
    private Long codeListManifestId;
    private String codeListName;
}
