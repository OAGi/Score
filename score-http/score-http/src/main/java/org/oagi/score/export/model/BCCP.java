package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;

public class BCCP implements Component {

    private BccpManifestRecord bccpManifest;
    private BccpRecord bccp;
    private DtManifestRecord bdtManifest;
    private DtRecord bdt;

    public BCCP(BccpManifestRecord bccpManifest, BccpRecord bccp,
                DtManifestRecord bdtManifest, DtRecord bdt) {
        this.bccpManifest = bccpManifest;
        this.bccp = bccp;
        this.bdtManifest = bdtManifest;
        this.bdt = bdt;
    }

    public String getGuid() {
        return bccp.getGuid();
    }

    public String getName() {
        String propertyTerm = bccp.getPropertyTerm();
        return propertyTerm.replaceAll(" ", "").replace("Identifier", "ID");
    }

    public String getTypeName() {
        return ModelUtils.getTypeName(bdtManifest, bdt);
    }

    public boolean isNillable() {
        return bccp.getIsNillable() == 1;
    }

    public String getDefaultValue() {
        return bccp.getDefaultValue();
    }

    public String getDefinition() {
        return bccp.getDefinition();
    }

    public String getDefinitionSource() {
        return bccp.getDefinitionSource();
    }

    public ULong getNamespaceId() {
        return bccp.getNamespaceId();
    }

    public ULong getTypeNamespaceId() {
        return bdt.getNamespaceId();
    }
}
