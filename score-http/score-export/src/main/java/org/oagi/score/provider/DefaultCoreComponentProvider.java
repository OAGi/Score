package org.oagi.score.provider;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.SeqKeyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BCC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.SEQ_KEY;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Ascc.ASCC;

@Component
public class DefaultCoreComponentProvider implements CoreComponentProvider {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<BccRecord> getBCCs(long accId) {
        return dslContext.selectFrom(BCC).where(BCC.FROM_ACC_ID.eq(ULong.valueOf(accId))).fetchInto(BccRecord.class);
    }

    @Override
    public List<BccRecord> getBCCsWithoutAttributes(long accId) {
        return getBCCs(accId).stream()
                .filter(e -> e.getSeqKey() != 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsccRecord> getASCCs(long accId) {
        return dslContext.selectFrom(ASCC).where(ASCC.FROM_ACC_ID.eq(ULong.valueOf(accId))).fetchInto(AsccRecord.class);
    }

    @Override
    public List<SeqKeyRecord> getSeqKeys(long accManifestId) {
        return dslContext.selectFrom(SEQ_KEY).where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId))).fetchInto(SeqKeyRecord.class);
    }
}
