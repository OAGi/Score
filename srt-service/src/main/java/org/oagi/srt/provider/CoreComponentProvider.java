package org.oagi.srt.provider;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;

import java.util.List;

public interface CoreComponentProvider {

    public List<BasicCoreComponent> getBCCs(int accId);

    public List<BasicCoreComponent> getBCCsWithoutAttributes(int accId);

    public List<AssociationCoreComponent> getASCCs(int accId);

}
