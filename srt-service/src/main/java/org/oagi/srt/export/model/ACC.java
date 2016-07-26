package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public abstract class ACC implements Component {

    private AggregateCoreComponent acc;
    private ACC basedAcc;

    private ImportedDataProvider importedDataProvider;
    private int oagisComponentType;

    ACC(AggregateCoreComponent acc, ACC basedAcc,
        ImportedDataProvider importedDataProvider) {
        this.acc = acc;
        this.basedAcc = basedAcc;
        this.importedDataProvider = importedDataProvider;
        this.oagisComponentType = acc.getOagisComponentType();
    }

    public static ACC newInstance(AggregateCoreComponent acc,
                                  ImportedDataProvider importedDataProvider) {
        switch (acc.getOagisComponentType()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
                ACC basedACC = null;
                if (acc.getBasedAccId() > 0) {
                    AggregateCoreComponent basedAcc = importedDataProvider.findACC(acc.getBasedAccId());
                    basedACC = newInstance(basedAcc, importedDataProvider);
                }
                return new ACCComplexType(acc, basedACC, importedDataProvider);
            case 4: // @TODO: Not yet handled
                return new ACCGroup(acc, null, importedDataProvider);
            default:
                throw new IllegalStateException();
        }
    }

    public int getRawId() {
        return acc.getAccId();
    }

    public String getName() {
        return Utility.toCamelCase(acc.getObjectClassTerm());
    }

    public boolean isAbstract() {
        return acc.isAbstract();
    }

    public String getGuid() {
        return acc.getGuid();
    }

    public String getTypeName() {
        return getName() + "Type";
    }

    public ACC getBasedACC() {
        return basedAcc;
    }

    public boolean isGroup() {
        AssociationCoreComponentProperty asccp = importedDataProvider.findASCCPByGuid(getGuid());
        return (asccp != null) && asccp.getRoleOfAccId() == getRawId();
    }

    public int getOagisComponentType() {
        return this.oagisComponentType;
    }
}
