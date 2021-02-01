package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class CodeList {

    private BigInteger codeListManifestId;
    private String codeListName;
    private String guid;
    private BigInteger basedCodeListManifestId;
    private String basedCodeListName;
    private BigInteger namespaceId;
    private String namespaceUri;

    private BigInteger agencyId;
    private String agencyIdName;
    private String versionId;

    private String listId;
    private String definition;
    private String definitionSource;
    private String remark;

    private boolean extensible;
    private boolean deprecated;
    private BigInteger ownerId;
    private String state;
    private AccessPrivilege access;

    private String owner;
    private BigInteger releaseId;
    private String releaseNum;
    private BigInteger logId;
    private int revisionNum;
    private int revisionTrackingNum;

    private List<CodeListValue> codeListValues = Collections.emptyList();

}
