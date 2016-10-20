package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public abstract class ASCCP implements Component {

    private AssociationCoreComponentProperty asccp;
    private AggregateCoreComponent roleOfAcc;

    ASCCP(AssociationCoreComponentProperty asccp,
          AggregateCoreComponent roleOfAcc) {
        this.asccp = asccp;
        this.roleOfAcc = roleOfAcc;
    }

    public static ASCCP newInstance(AssociationCoreComponentProperty asccp,
                                    ImportedDataProvider importedDataProvider) {
        AggregateCoreComponent roleOfAcc = importedDataProvider.findACC(asccp.getRoleOfAccId());
        switch (roleOfAcc.getOagisComponentType()) {
            case Base:
            case Semantics:
            case Extension:
            case SemanticGroup:
            case Embedded:
            case OAGIS10Nouns:
            case OAGIS10BODs:
                return new ASCCPComplexType(asccp, roleOfAcc);
            case UserExtensionGroup:
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
        return asccp.isReusableIndicator();
    }

    public boolean isNillable() {
        return asccp.isNillable();
    }
}
