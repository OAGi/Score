package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class CodeList {

    private long codeListId;
    private String codeListName;
    private String guid;
    private Long basedCodeListId;
    private String basedCodeListName;

    private long agencyId;
    private String agencyIdName;
    private String versionId;

    private String listId;
    private String definition;
    private String definitionSource;
    private String remark;

    private boolean extensible;
    private String state;

    private List<CodeListValue> codeListValues = Collections.emptyList();

}
