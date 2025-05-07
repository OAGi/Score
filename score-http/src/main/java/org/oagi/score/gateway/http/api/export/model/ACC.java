package org.oagi.score.gateway.http.api.export.model;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.util.Utility;

public abstract class ACC implements Component {

    private AccSummaryRecord acc;
    private ACC basedAcc;

    private CcDocument ccDocument;
    private Integer oagisComponentType;

    ACC(AccSummaryRecord acc, ACC basedAcc,
        CcDocument ccDocument) {
        this.acc = acc;
        this.basedAcc = basedAcc;
        this.ccDocument = ccDocument;
        this.oagisComponentType = acc.componentType().getValue();
    }

    public static ACC newInstance(AccSummaryRecord acc, CcDocument ccDocument) {
        switch (acc.componentType().getValue()) {
            case 0: //Base
            case 1: //Semantics
            case 2: //Extension
            case 3: //SemanticGroup
            case 5: //Embedded
            case 6: //OAGIS10Nouns
            case 7: //OAGIS10BODs
                ACC basedACC = null;
                if (acc.basedAccManifestId() != null) {
                    AccSummaryRecord basedAcc = ccDocument.getAcc(acc.basedAccManifestId());
                    if (basedAcc == null) {
                        throw new IllegalStateException();
                    }
                    basedACC = newInstance(basedAcc, ccDocument);
                }
                return new ACCComplexType(acc, basedACC, ccDocument);
            case 4: // UEG
                return new ACCGroup(acc, null, ccDocument);
            default:
                throw new IllegalStateException();
        }
    }

    public ULong getRawId() {
        return ULong.valueOf(acc.accId().value());
    }

    public String getName() {
        return Utility.toCamelCase(acc.objectClassTerm());
    }

    public boolean isAbstract() {
        return acc.isAbstract();
    }

    public String getGuid() {
        return acc.guid().value();
    }

    public String getTypeName() {
        return getName() + "Type";
    }

    public ACC getBasedACC() {
        return basedAcc;
    }

    public NamespaceId getNamespaceId() {
        return acc.namespaceId();
    }

    public NamespaceId getTypeNamespaceId() {
        return this.getNamespaceId();
    }

    public boolean isGroup() {
        return acc.isGroup();
    }

    public OagisComponentType getOagisComponentType() {
        return OagisComponentType.valueOf(this.oagisComponentType);
    }

    public String getDefinition() {
        return (acc.definition() != null) ? acc.definition().content() : null;
    }

    public String getDefinitionSource() {
        return (acc.definition() != null) ? acc.definition().source() : null;
    }

    public AccManifestId accManifestId() {
        return acc.accManifestId();
    }

}
