package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieEditCodeList {

    private BigInteger codeListManifestId;
    private BigInteger basedCodeListManifestId;
    private BigInteger codeListId;
    private boolean isDefault;
    private String codeListName;

}
