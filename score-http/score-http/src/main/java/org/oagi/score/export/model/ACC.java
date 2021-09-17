package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.common.util.OagisComponentType;
import org.oagi.score.common.util.Utility;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;

public abstract class ACC implements Component {

    private AccRecord acc;
    private ACC basedAcc;

    private ImportedDataProvider importedDataProvider;
    private Integer oagisComponentType;

    private ModuleCCID moduleCCID;

    ACC(AccRecord acc, ACC basedAcc,
        ImportedDataProvider importedDataProvider) {
        this.acc = acc;
        this.basedAcc = basedAcc;
        this.importedDataProvider = importedDataProvider;
        this.oagisComponentType = acc.getOagisComponentType();
        this.moduleCCID = this.importedDataProvider.findModuleAcc(this.acc.getAccId());
    }

    public static ACC newInstance(AccRecord acc, AccManifestRecord accManifest,
                                  ImportedDataProvider importedDataProvider) {
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
                    AccManifestRecord basedAccManifest = importedDataProvider.findACCManifest(accManifest.getBasedAccManifestId());
                    if (basedAccManifest == null) {
                        throw new IllegalStateException();
                    }
                    AccRecord basedAcc = importedDataProvider.findACC(basedAccManifest.getAccId());
                    basedACC = newInstance(basedAcc, basedAccManifest, importedDataProvider);
                }
                return new ACCComplexType(acc, basedACC, accManifest, importedDataProvider);
            case 4: // UEG
                return new ACCGroup(acc, null, importedDataProvider);
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

    public boolean isGroup() {
        AsccpRecord asccp = importedDataProvider.findASCCPByGuid(acc.getGuid());
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
}
