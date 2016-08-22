package org.oagi.srt.provider;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;

import java.util.List;

public interface CoreComponentProvider {

    public List<BasicCoreComponent> getBCCs(long accId);

    public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId);

    public List<AssociationCoreComponent> getASCCs(long accId);

}
