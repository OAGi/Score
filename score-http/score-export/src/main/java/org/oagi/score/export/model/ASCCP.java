package org.oagi.score.export.model;

import org.oagi.score.common.util.Utility;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;

public abstract class ASCCP implements Component {

    private AsccpRecord asccp;
    private AccRecord roleOfAcc;

    ASCCP(AsccpRecord asccp, AccRecord roleOfAcc) {
        this.asccp = asccp;
        this.roleOfAcc = roleOfAcc;
    }

    public static ASCCP newInstance(AsccpRecord asccp, AsccpManifestRecord asccpManifest,
                                    ImportedDataProvider importedDataProvider) {
        AccManifestRecord roleOfAccManifest = importedDataProvider.findACCManifest(asccpManifest.getRoleOfAccManifestId());
        AccRecord roleOfAcc = importedDataProvider.findACC(roleOfAccManifest.getAccId());
        if (roleOfAcc == null) {
            throw new IllegalStateException();
        }
        switch (roleOfAcc.getOagisComponentType()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 7:
                return new ASCCPComplexType(asccp, roleOfAcc);
            case 4:
                return new ASCCPGroup(asccp, roleOfAcc);
            default:
                throw new IllegalStateException();
        }
    }

    public String getName() {
        return Utility.toCamelCase(asccp.getPropertyTerm());
    }

    public String getTypeName() {
        String den = asccp.getDen();
        String propertyTerm = asccp.getPropertyTerm();

        return Utility.toCamelCase(den.substring((propertyTerm + ". ").length())) + "Type";
    }

    public String getGuid() {
        return asccp.getGuid();
    }

    public boolean isGroup() {
        return roleOfAcc.getGuid().equals(asccp.getGuid());
    }

    public boolean isReusableIndicator() {
        return asccp.getReusableIndicator() == 1;
    }

    public boolean isNillable() {
        return asccp.getIsNillable() == 1;
    }

    public String getDefinition() {
        return asccp.getDefinition();
    }

    public String getDefinitionSource() {
        return asccp.getDefinitionSource();
    }
}
