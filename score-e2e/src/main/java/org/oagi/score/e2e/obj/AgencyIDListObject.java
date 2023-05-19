package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.*;

@Data
public class AgencyIDListObject {

    private BigInteger agencyIDListManifestId;

    private BigInteger agencyIDListId;

    private BigInteger basedAgencyIDListManifestId;

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

    private boolean deprecated;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static AgencyIDListObject createRandomAgencyIDList(AppUserObject creator, NamespaceObject namespace, String state) {
        AgencyIDListObject agencyIDList = new AgencyIDListObject();
        agencyIDList.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        agencyIDList.setEnumTypeGuid("oagis-id-" + UUID.randomUUID().toString().replaceAll("-", ""));
        agencyIDList.setListId(randomNumeric(5, 10));
        agencyIDList.setVersionId(randomAlphanumeric(5, 10));
        agencyIDList.setName("clm" + agencyIDList.getListId() + agencyIDList.getVersionId() + "_AgencyIdentification");
        agencyIDList.setDefinition(randomPrint(50, 100).trim());
        agencyIDList.setDefinitionSource(randomAlphanumeric(5, 10));
        agencyIDList.setRemark(randomPrint(50, 100).trim());
        agencyIDList.setNamespaceId(namespace.getNamespaceId());
        agencyIDList.setDeprecated(false);
        agencyIDList.setState(state);
        agencyIDList.setOwnerUserId(creator.getAppUserId());
        agencyIDList.setCreatedBy(creator.getAppUserId());
        agencyIDList.setLastUpdatedBy(creator.getAppUserId());
        agencyIDList.setCreationTimestamp(LocalDateTime.now());
        agencyIDList.setLastUpdateTimestamp(LocalDateTime.now());
        return agencyIDList;
    }

}
