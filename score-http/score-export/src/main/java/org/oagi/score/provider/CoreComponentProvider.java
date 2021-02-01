package org.oagi.score.provider;

import org.oagi.score.repo.api.impl.jooq.entity.tables.SeqKey;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.SeqKeyRecord;

import java.util.List;

public interface CoreComponentProvider {

    public List<BccRecord> getBCCs(long accId);

    public List<BccRecord> getBCCsWithoutAttributes(long accId);

    public List<AsccRecord> getASCCs(long accId);

    public List<SeqKeyRecord> getSeqKeys(long accManifestId);

}
