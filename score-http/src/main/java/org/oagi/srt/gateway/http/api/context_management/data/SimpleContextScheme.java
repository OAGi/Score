package org.oagi.srt.gateway.http.api.context_management.data;

import lombok.Data;

@Data
public class SimpleContextScheme {

    private long ctxSchemeId;
    private String schemeName;
    private String schemeId;
    private String schemeAgencyId;
    private String schemeVersionId;
    private long codeListId;

}
