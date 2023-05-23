package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class DTObject {

    private BigInteger dtManifestId;

    private BigInteger releaseId;

    private BigInteger dtId;

    private BigInteger basedDtManifestId;

    private BigInteger basedDtId;

    private String guid;

    private String dataTypeTerm;

    private String representationTerm;

    private String den;

    private String qualifier;

    private BigInteger namespaceId;

    private String definition;

    private String definitionSource;

    private boolean deprecated;

    private String state;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;
    private String contentComponentDefinition;

    public static DTObject createRandomDT(DTObject baseDataType, AppUserObject creator, NamespaceObject namespace, String state) {
        DTObject bdt = new DTObject();
        bdt.setReleaseId(baseDataType.getReleaseId());
        bdt.setBasedDtManifestId(baseDataType.getDtManifestId());
        bdt.setBasedDtId(baseDataType.getDtId());
        bdt.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        String randomQualifier = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomQualifier = Character.toUpperCase(randomQualifier.charAt(0)) + randomQualifier.substring(1).toLowerCase();
        bdt.setDataTypeTerm(baseDataType.getDataTypeTerm());
        bdt.setRepresentationTerm(baseDataType.getRepresentationTerm());
        bdt.setQualifier(randomQualifier);
        bdt.setDen(bdt.getQualifier() + "_ " + bdt.getDataTypeTerm() + ". Type");
        bdt.setDefinition(randomPrint(50, 100).trim());
        bdt.setDefinitionSource(randomPrint(50, 100).trim());
        bdt.setNamespaceId(namespace.getNamespaceId());
        bdt.setDeprecated(false);
        bdt.setState(state);
        bdt.setOwnerUserId(creator.getAppUserId());
        bdt.setCreatedBy(creator.getAppUserId());
        bdt.setLastUpdatedBy(creator.getAppUserId());
        bdt.setCreationTimestamp(LocalDateTime.now());
        bdt.setLastUpdateTimestamp(LocalDateTime.now());
        return bdt;
    }

}
