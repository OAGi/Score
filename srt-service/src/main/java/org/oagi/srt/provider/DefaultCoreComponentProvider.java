package org.oagi.srt.provider;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultCoreComponentProvider implements CoreComponentProvider {

    @Autowired
    @Lazy
    private ImportedDataProvider importedDataProvider;

    @Override
    public List<BasicCoreComponent> getBCCs(long accId) {
        return importedDataProvider.findBCCByFromAccId(accId);
    }

    @Override
    public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
        return getBCCs(accId).stream()
                .filter(e -> e.getSeqKey() != 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssociationCoreComponent> getASCCs(long accId) {
        return importedDataProvider.findASCCByFromAccId(accId);
    }
}
