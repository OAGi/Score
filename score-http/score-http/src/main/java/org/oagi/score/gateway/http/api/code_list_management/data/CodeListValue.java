package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CodeListValue {

    private BigInteger codeListValueManifestId;
    private String guid;
    private String value;
    private String meaning;
    private String definition;
    private String definitionSource;

    private boolean used;
    private boolean locked;
    private boolean extension;
    private boolean deprecated;
    private boolean derived;

}
