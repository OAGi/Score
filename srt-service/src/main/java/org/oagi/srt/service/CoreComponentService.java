package org.oagi.srt.service;

import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.CoreComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CoreComponentService {

    @Autowired
    private CoreComponentProvider coreComponentProvider;

    public List<CoreComponent> getCoreComponents(int accId) {
        return getCoreComponents(accId, coreComponentProvider);
    }

    public List<CoreComponent> getCoreComponents(AggregateCoreComponent acc) {
        return getCoreComponents(acc, coreComponentProvider);
    }

    public List<CoreComponent> getCoreComponents(
            AggregateCoreComponent acc, CoreComponentProvider coreComponentProvider) {
        int accId = acc.getAccId();
        return getCoreComponents(accId, coreComponentProvider);
    }

    public List<CoreComponent> getCoreComponents(
            int accId, CoreComponentProvider coreComponentProvider) {
        List<BasicCoreComponent> bcc_tmp_assoc = coreComponentProvider.getBCCs(accId);
        List<AssociationCoreComponent> ascc_tmp_assoc = coreComponentProvider.getASCCs(accId);

        List<CoreComponent> coreComponents = gatheringBySeqKey(bcc_tmp_assoc, ascc_tmp_assoc);
        return Collections.unmodifiableList(coreComponents);
    }

    public List<CoreComponent> getCoreComponentsWithoutAttributes(
            AggregateCoreComponent acc, CoreComponentProvider coreComponentProvider) {
        int accId = acc.getAccId();
        return getCoreComponentsWithoutAttributes(accId, coreComponentProvider);
    }

    public List<CoreComponent> getCoreComponentsWithoutAttributes(
            int accId, CoreComponentProvider coreComponentProvider) {
        List<BasicCoreComponent> bcc_tmp_assoc = coreComponentProvider.getBCCsWithoutAttributes(accId);
        List<AssociationCoreComponent> ascc_tmp_assoc = coreComponentProvider.getASCCs(accId);

        List<CoreComponent> coreComponents = gatheringBySeqKey(bcc_tmp_assoc, ascc_tmp_assoc);
        return Collections.unmodifiableList(coreComponents);
    }

    private List<CoreComponent> gatheringBySeqKey(
            List<BasicCoreComponent> bccList, List<AssociationCoreComponent> asccList
    ) {
        int size = bccList.size() + asccList.size();
        List<CoreComponent> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bccList);
        tmp_assoc.addAll(asccList);

        ArrayList<CoreComponent> coreComponents = new ArrayList(size);
        CoreComponent a = new CoreComponent();
        for (int i = 0; i < size; i++)
            coreComponents.add(a);

        int attribute_cnt = 0;
        for (BasicCoreComponent basicCoreComponent : bccList) {
            if (basicCoreComponent.getSeqKey() == 0) {
                coreComponents.set(attribute_cnt, basicCoreComponent);
                attribute_cnt++;
            }
        }

        for (CoreComponent coreComponent : tmp_assoc) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent basicCoreComponent = (BasicCoreComponent) coreComponent;
                if (basicCoreComponent.getSeqKey() > 0) {
                    coreComponents.set(basicCoreComponent.getSeqKey() - 1 + attribute_cnt, basicCoreComponent);
                }
            } else {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                coreComponents.set(associationCoreComponent.getSeqKey() - 1 + attribute_cnt, associationCoreComponent);
            }
        }

        coreComponents.trimToSize();

        return coreComponents;
    }
}
