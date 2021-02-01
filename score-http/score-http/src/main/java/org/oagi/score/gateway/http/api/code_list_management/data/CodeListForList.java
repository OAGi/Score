package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CodeListForList {

    private BigInteger codeListManifestId;
    private String codeListName;
    private String definition;
    private String definitionSource;
    private String modulePath;
    private String guid;
    private BigInteger basedCodeListManifestId;
    private String basedCodeListName;
    private BigInteger agencyId;
    private String listId;
    private String agencyIdName;
    private String versionId;
    private boolean extensible;
    private boolean deprecated;
    private String revision;
    private String state;
    private AccessPrivilege access;
    private BigInteger ownerId;
    private String owner;
    private String lastUpdateUser;
    private Date lastUpdateTimestamp;

}
