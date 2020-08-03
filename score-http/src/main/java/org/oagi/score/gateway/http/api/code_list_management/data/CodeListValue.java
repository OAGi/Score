package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;

@Data
public class CodeListValue {

    long codeListValueId;
    String guid;
    String value;
    String name;
    String definition;
    String definitionSource;

    boolean used;
    boolean locked;
    boolean extension;

}
