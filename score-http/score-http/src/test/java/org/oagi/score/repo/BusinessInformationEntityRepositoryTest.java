package org.oagi.score.repo;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.ScoreHttpApplication;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AbieRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsbiepRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@SpringBootTest(
        classes = ScoreHttpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class BusinessInformationEntityRepositoryTest {

    @Autowired
    private BusinessInformationEntityRepository bieRepository;

    @Autowired
    private DSLContext dslContext;

    /*
     * prerequisites
     */
    private ULong userId = ULong.valueOf(1L);
    private ULong releaseId = ULong.valueOf(1L);
    private LocalDateTime timestamp = LocalDateTime.now();

    @Test
    public void insertTopLevelAsbiepTest() {
        ULong topLevelAsbiepId = insertTopLevelAsbiep();

        TopLevelAsbiepRecord topLevelAsbiep = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId))
                .fetchOptional().orElse(null);

        assertNotNull(topLevelAsbiep);
        assertEquals(userId, topLevelAsbiep.getOwnerUserId());
        assertEquals(timestamp, topLevelAsbiep.getLastUpdateTimestamp());
        assertEquals(releaseId, topLevelAsbiep.getReleaseId());
    }

    private ULong insertTopLevelAsbiep() {
        return bieRepository.insertTopLevelAsbiep()
                .setUserId(userId)
                .setReleaseId(releaseId)
                .setTimestamp(timestamp)
                .execute();
    }

    @Test
    public void insertAbieTest() {
        // tested by #insertTopLevelAsbiepTest
        ULong topLevelAsbiepId = insertTopLevelAsbiep();

        ULong roleOfAccManifestId = ULong.valueOf(1L);
        ULong abieId = insertAbie(topLevelAsbiepId, roleOfAccManifestId);

        AbieRecord abie = dslContext.selectFrom(ABIE)
                .where(ABIE.ABIE_ID.eq(abieId))
                .fetchOptional().orElse(null);

        assertNotNull(abie);
        assertEquals(userId, abie.getCreatedBy());
        assertEquals(timestamp, abie.getCreationTimestamp());
        assertEquals(topLevelAsbiepId, abie.getOwnerTopLevelAsbiepId());
        assertEquals(roleOfAccManifestId, abie.getBasedAccManifestId());
    }

    private ULong insertAbie(ULong topLevelAsbiepId, ULong roleOfAccManifestId) {
        return bieRepository.insertAbie()
                .setUserId(userId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setAccManifestId(roleOfAccManifestId)
                .setTimestamp(timestamp)
                .execute();
    }

    private ULong insertAbie(ULong topLevelAsbiepId) {
        return insertAbie(topLevelAsbiepId, ULong.valueOf(1L));
    }

    @Test
    public void insertAsbiepTest() {
        // tested by #insertTopLevelAsbiepTest
        ULong topLevelAsbiepId = insertTopLevelAsbiep();
        // tested by #insertAbieTest
        ULong abieId = insertAbie(topLevelAsbiepId);

        ULong asccpManifestId = ULong.valueOf(1L);
        ULong asbiepId = bieRepository.insertAsbiep()
                .setAsccpManifestId(asccpManifestId)
                .setRoleOfAbieId(abieId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setUserId(userId)
                .setTimestamp(timestamp)
                .execute();

        AsbiepRecord asbiep = dslContext.selectFrom(ASBIEP)
                .where(ASBIEP.ASBIEP_ID.eq(asbiepId))
                .fetchOptional().orElse(null);

        assertNotNull(asbiep);
        assertEquals(userId, asbiep.getCreatedBy());
        assertEquals(timestamp, asbiep.getCreationTimestamp());
        assertEquals(topLevelAsbiepId, asbiep.getOwnerTopLevelAsbiepId());
        assertEquals(abieId, asbiep.getRoleOfAbieId());
        assertEquals(asccpManifestId, asbiep.getBasedAsccpManifestId());
    }
}
