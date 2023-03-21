package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class CodeListObject {

    private BigInteger codeListManifestId;

    private BigInteger codeListId;

    private BigInteger basedCodeListManifestId;

    private BigInteger agencyIdListValueManifestId;

    private BigInteger releaseId;

    private String guid;

    private String enumTypeGuid;

    private String name;

    private String listId;

    private String versionId;

    private String definition;

    private String definitionSource;

    private String remark;

    private BigInteger namespaceId;

    private boolean extensibleIndicator;

    private boolean deprecated;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static CodeListObject createRandomCodeList(AppUserObject creator, NamespaceObject namespace,
                                                      String state) {
        CodeListObject codeList = new CodeListObject();
        codeList.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        codeList.setName("cl_" + randomAlphanumeric(5, 10));
        codeList.setListId("cl_list_id_" + randomAlphanumeric(5, 10));
        codeList.setVersionId("cl_version_id_" + randomAlphanumeric(5, 10));
        codeList.setDefinition(randomPrint(50, 100).trim());
        codeList.setDefinitionSource("cl_description_source_" + randomAlphanumeric(5, 10));
        codeList.setRemark(randomPrint(50, 100).trim());
        codeList.setNamespaceId(namespace.getNamespaceId());
        codeList.setExtensibleIndicator(true);
        codeList.setDeprecated(false);
        codeList.setState(state);
        codeList.setOwnerUserId(creator.getAppUserId());
        codeList.setCreatedBy(creator.getAppUserId());
        codeList.setLastUpdatedBy(creator.getAppUserId());
        codeList.setCreationTimestamp(LocalDateTime.now());
        codeList.setLastUpdateTimestamp(LocalDateTime.now());
        return codeList;
    }

    public static CodeListObject createDerivedCodeList(CodeListObject baseCodeList,
                                                       AppUserObject creator, NamespaceObject namespace,
                                                       String state) {
        CodeListObject codeList = new CodeListObject();
        codeList.setBasedCodeListManifestId(baseCodeList.getCodeListManifestId());
        codeList.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        codeList.setName(baseCodeList.getName() + "_derived");
        codeList.setListId(baseCodeList.getListId());
        codeList.setVersionId(baseCodeList.getVersionId());
        codeList.setDefinition(baseCodeList.getDefinition());
        codeList.setDefinitionSource(baseCodeList.getDefinitionSource());
        codeList.setRemark(baseCodeList.getRemark());
        codeList.setNamespaceId(namespace.getNamespaceId());
        if (creator.isDeveloper()) {
            codeList.setExtensibleIndicator(baseCodeList.isExtensibleIndicator());
        } else {
            codeList.setExtensibleIndicator(false);
        }
        codeList.setDeprecated(false);
        codeList.setState(state);
        codeList.setOwnerUserId(creator.getAppUserId());
        codeList.setCreatedBy(creator.getAppUserId());
        codeList.setLastUpdatedBy(creator.getAppUserId());
        codeList.setCreationTimestamp(LocalDateTime.now());
        codeList.setLastUpdateTimestamp(LocalDateTime.now());
        return codeList;
    }

}
