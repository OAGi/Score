package org.oagi.srt.export.model;

import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.AggregateCoreComponent;

public class ACCComplexType extends ACC {

    ACCComplexType(AggregateCoreComponent acc, ACC basedAcc,
                   ImportedDataProvider importedDataProvider) {
        super(acc, basedAcc, importedDataProvider);
    }
}
