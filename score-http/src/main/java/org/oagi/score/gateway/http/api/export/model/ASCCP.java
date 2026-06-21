package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public abstract class ASCCP implements Component {

    private final AsccpSummaryRecord asccp;
    private final AccSummaryRecord roleOfAcc;
    private final SchemaNamingStrategy namingStrategy;

    ASCCP(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc, SchemaNamingStrategy namingStrategy) {
        this.asccp = asccp;
        this.roleOfAcc = roleOfAcc;
        this.namingStrategy = namingStrategy;
    }

    public static ASCCP newInstance(AsccpSummaryRecord asccp, CcDocument ccDocument) {
        return newInstance(asccp, ccDocument, new XmlSchemaNamingStrategy());
    }

    public static ASCCP newInstance(AsccpSummaryRecord asccp, CcDocument ccDocument, SchemaNamingStrategy namingStrategy) {
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
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return new ASCCPComplexType(asccp, roleOfAcc, namingStrategy);
            case 4:
                return new ASCCPGroup(asccp, roleOfAcc, namingStrategy);
            default:
                throw new IllegalStateException();
        }
    }

    public String getName() {
        return namingStrategy.asccpName(asccp);
    }

    public String getPropertyTerm() {
        return asccp.propertyTerm();
    }

    public String getTypeName() {
        return namingStrategy.asccpTypeName(asccp, roleOfAcc);
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

    public AsccpManifestId asccpManifestId() {
        return asccp.asccpManifestId();
    }

    public AccManifestId roleOfAccManifestId() {
        return asccp.roleOfAccManifestId();
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
