package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public class BCCP implements Component {

    private final BccpSummaryRecord bccp;
    private final DtSummaryRecord dt;
    private final SchemaNamingStrategy namingStrategy;

    public BCCP(BccpSummaryRecord bccp, DtSummaryRecord dt) {
        this(bccp, dt, new XmlSchemaNamingStrategy());
    }

    public BCCP(BccpSummaryRecord bccp, DtSummaryRecord dt, SchemaNamingStrategy namingStrategy) {
        this.bccp = bccp;
        this.dt = dt;
        this.namingStrategy = namingStrategy;
    }

    public String getGuid() {
        return bccp.guid().value();
    }

    public String getName() {
        return namingStrategy.bccpName(bccp);
    }

    public String getPropertyTerm() {
        return bccp.propertyTerm();
    }

    public String getTypeName() {
        return namingStrategy.dtName(dt);
    }

    public BccpManifestId bccpManifestId() {
        return bccp.bccpManifestId();
    }

    public DtManifestId dtManifestId() {
        return dt.dtManifestId();
    }

    public boolean isNillable() {
        return bccp.nillable();
    }

    public String getDefaultValue() {
        return (bccp.valueConstraint() != null) ? bccp.valueConstraint().defaultValue() : null;
    }

    public String getDefinition() {
        return (bccp.definition() != null) ? bccp.definition().content() : null;
    }

    public String getDefinitionSource() {
        return (bccp.definition() != null) ? bccp.definition().source() : null;
    }

    public NamespaceId getNamespaceId() {
        return bccp.namespaceId();
    }

    public NamespaceId getTypeNamespaceId() {
        return dt.namespaceId();
    }
}
