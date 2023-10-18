package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.common.util.Utility;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;
import org.oagi.score.repository.provider.DataProvider;

public abstract class ASCCP implements Component {

    private AsccpManifestRecord asccpManifest;
    private AsccpRecord asccp;
    private AccManifestRecord rolfOfAccManifest;
    private AccRecord roleOfAcc;

    ASCCP(AsccpManifestRecord asccpManifest, AsccpRecord asccp,
          AccManifestRecord rolfOfAccManifest, AccRecord roleOfAcc) {
        this.asccpManifest = asccpManifest;
        this.asccp = asccp;
        this.rolfOfAccManifest = rolfOfAccManifest;
        this.roleOfAcc = roleOfAcc;
    }

    public static ASCCP newInstance(AsccpRecord asccp, AsccpManifestRecord asccpManifest,
                                    DataProvider dataProvider) {
        AccManifestRecord roleOfAccManifest = dataProvider.findACCManifest(asccpManifest.getRoleOfAccManifestId());
        AccRecord roleOfAcc = dataProvider.findACC(roleOfAccManifest.getAccId());
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
                return new ASCCPComplexType(asccpManifest, asccp, roleOfAccManifest, roleOfAcc);
            case 4:
                return new ASCCPGroup(asccpManifest, asccp, roleOfAccManifest, roleOfAcc);
            default:
                throw new IllegalStateException();
        }
    }

    public String getName() {
        return Utility.toCamelCase(asccp.getPropertyTerm());
    }

    public String getTypeName() {
        String den = asccpManifest.getDen();
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

    public ULong getNamespaceId() {
        return asccp.getNamespaceId();
    }

    public ULong getTypeNamespaceId() {
        return roleOfAcc.getNamespaceId();
    }

}
