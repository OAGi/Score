package org.oagi.score.e2e.impl.api;

import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.SeqKeyRecord;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DSLContextAppUserAPIImplTest {

    @Test
    void findsSurvivingNeighborAfterAdjacentDeletedSeqKeys() {
        SeqKeyRecord parent = seqKey(1, null, 2);
        SeqKeyRecord firstDeleted = seqKey(2, 1, 3);
        SeqKeyRecord secondDeleted = seqKey(3, 2, 4);
        SeqKeyRecord next = seqKey(4, 3, null);
        Map<ULong, SeqKeyRecord> sequenceKeyById = Map.of(
                id(1), parent,
                id(2), firstDeleted,
                id(3), secondDeleted,
                id(4), next
        );

        assertEquals(id(1), DSLContextAppUserAPIImpl.findSurvivingNeighbor(
                sequenceKeyById, Set.of(id(2), id(3)), firstDeleted.getPrevSeqKeyId(), false));
        assertEquals(id(4), DSLContextAppUserAPIImpl.findSurvivingNeighbor(
                sequenceKeyById, Set.of(id(2), id(3)), firstDeleted.getNextSeqKeyId(), true));
    }

    @Test
    void doesNotRestoreADeletedSeqKeyWhenTheExistingLinkIsAlreadyDangling() {
        SeqKeyRecord target = seqKey(2, 1, 999);
        Map<ULong, SeqKeyRecord> sequenceKeyById = Map.of(id(2), target);

        assertNull(DSLContextAppUserAPIImpl.findSurvivingNeighbor(
                sequenceKeyById, Set.of(id(2)), target.getNextSeqKeyId(), true));
    }

    private static SeqKeyRecord seqKey(long id, Integer previousId, Integer nextId) {
        return new SeqKeyRecord(
                id(id),
                id(100),
                null,
                null,
                previousId == null ? null : id(previousId),
                nextId == null ? null : id(nextId));
    }

    private static ULong id(long value) {
        return ULong.valueOf(value);
    }
}
