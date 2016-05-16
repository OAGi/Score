package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponent;

import java.util.List;

public interface BasicCoreComponentRepository {

    public List<BasicCoreComponent> findByFromAccId(int fromAccId);

    public BasicCoreComponent findOneByBccId(int bccId);
}
