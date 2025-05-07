package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.util.Utility;

public abstract class ASCCP implements Component {

    private AsccpSummaryRecord asccp;
    private AccSummaryRecord roleOfAcc;

    ASCCP(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc) {
        this.asccp = asccp;
        this.roleOfAcc = roleOfAcc;
    }

    public static ASCCP newInstance(AsccpSummaryRecord asccp, CcDocument ccDocument) {
        AccSummaryRecord roleOfAcc = ccDocument.getAcc(asccp.roleOfAccManifestId());
        if (roleOfAcc == null) {
            throw new IllegalStateException();
        }
        switch (roleOfAcc.componentType().getValue()) {
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
        return Utility.toCamelCase(asccp.propertyTerm());
    }

    public String getTypeName() {
        String den = asccp.den();
        String propertyTerm = asccp.propertyTerm();

        return Utility.toCamelCase(den.substring((propertyTerm + ". ").length())) + "Type";
    }

    public String getGuid() {
        return asccp.guid().value();
    }

    public boolean isGroup() {
        return roleOfAcc.isGroup();
    }

    public boolean isReusableIndicator() {
        return asccp.reusable();
    }

    public boolean isNillable() {
        return asccp.nillable();
    }

    public String getDefinition() {
        return (asccp.definition() != null) ? asccp.definition().content() : null;
    }

    public String getDefinitionSource() {
        return (asccp.definition() != null) ? asccp.definition().source() : null;
    }

    public NamespaceId getNamespaceId() {
        return asccp.namespaceId();
    }

    public NamespaceId getTypeNamespaceId() {
        return roleOfAcc.namespaceId();
    }

}
