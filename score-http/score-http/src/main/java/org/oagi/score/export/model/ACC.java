package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.common.util.OagisComponentType;
import org.oagi.score.common.util.Utility;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;
import org.oagi.score.repository.provider.DataProvider;
import org.oagi.score.repository.provider.ModuleProvider;

public abstract class ACC implements Component {

    private AccManifestRecord accManifest;
    private AccRecord acc;
    private ACC basedAcc;

    private DataProvider dataProvider;
    private Integer oagisComponentType;

    private ModuleCCID moduleCCID;

    ACC(AccManifestRecord accManifest, AccRecord acc,
        ACC basedAcc, DataProvider dataProvider) {
        this.accManifest = accManifest;
        this.acc = acc;
        this.basedAcc = basedAcc;
        this.dataProvider = dataProvider;
        this.oagisComponentType = acc.getOagisComponentType();
        if (dataProvider instanceof ModuleProvider) {
            this.moduleCCID = ((ModuleProvider) this.dataProvider).findModuleAcc(this.accManifest.getAccManifestId());
        }
    }

    public static ACC newInstance(AccRecord acc, AccManifestRecord accManifest,
                                  DataProvider dataProvider) {
        switch (acc.getOagisComponentType()) {
            case 0: //Base
            case 1: //Semantics
            case 2: //Extension
            case 3: //SemanticGroup
            case 5: //Embedded
            case 6: //OAGIS10Nouns
            case 7: //OAGIS10BODs
                ACC basedACC = null;
                if (accManifest.getBasedAccManifestId() != null) {
                    AccManifestRecord basedAccManifest = dataProvider.findACCManifest(accManifest.getBasedAccManifestId());
                    if (basedAccManifest == null) {
                        throw new IllegalStateException();
                    }
                    AccRecord basedAcc = dataProvider.findACC(basedAccManifest.getAccId());
                    basedACC = newInstance(basedAcc, basedAccManifest, dataProvider);
                }
                return new ACCComplexType(accManifest, acc, basedACC, dataProvider);
            case 4: // UEG
                return new ACCGroup(accManifest, acc, null, dataProvider);
            default:
                throw new IllegalStateException();
        }
    }

    public ULong getRawId() {
        return acc.getAccId();
    }

    public String getName() {
        return Utility.toCamelCase(acc.getObjectClassTerm());
    }

    public boolean isAbstract() {
        return acc.getIsAbstract() == 1;
    }

    public String getGuid() {
        return GUID_PREFIX + acc.getGuid();
    }

    public String getTypeName() {
        return getName() + "Type";
    }

    public ACC getBasedACC() {
        return basedAcc;
    }

    public ULong getNamespaceId() {
        return acc.getNamespaceId();
    }

    public ULong getTypeNamespaceId() {
        return this.getNamespaceId();
    }

    public boolean isGroup() {
        AsccpRecord asccp = dataProvider.findASCCPByGuid(acc.getGuid());
        return asccp != null;
    }

    public OagisComponentType getOagisComponentType() {
        return OagisComponentType.valueOf(this.oagisComponentType);
    }

    public String getDefinition() {
        return acc.getDefinition();
    }

    public String getDefinitionSource() {
        return acc.getDefinitionSource();
    }

    public ModuleCCID getModuleCCID() {
        return moduleCCID;
    }

    public void setModuleCCID(ModuleCCID moduleCCID) {
        this.moduleCCID = moduleCCID;
    }

    public AccManifestRecord getAccManifest() {
        return accManifest;
    }
}
