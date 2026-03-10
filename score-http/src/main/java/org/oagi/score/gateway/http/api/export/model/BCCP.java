package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import static org.oagi.score.gateway.http.api.export.model.ConnectSpecNameResolvers.dtNameResolver;

public class BCCP implements Component {

    private BccpSummaryRecord bccp;
    private DtSummaryRecord dt;

    public BCCP(BccpSummaryRecord bccp, DtSummaryRecord dt) {
        this.bccp = bccp;
        this.dt = dt;
    }

    public String getGuid() {
        return bccp.guid().value();
    }

    public String getName() {
        String propertyTerm = bccp.propertyTerm();
        return propertyTerm.replaceAll(" ", "").replace("Identifier", "ID");
    }

    public String getPropertyTerm() {
        return bccp.propertyTerm();
    }

    public String getTypeName() {
        return dtNameResolver.apply(dt);
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
