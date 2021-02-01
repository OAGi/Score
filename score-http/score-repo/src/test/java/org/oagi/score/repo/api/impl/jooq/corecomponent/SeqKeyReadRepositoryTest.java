package org.oagi.score.repo.api.impl.jooq.corecomponent;

import org.jooq.types.ULong;
import org.junit.jupiter.api.*;
import org.oagi.score.repo.api.corecomponent.model.EntityType;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.model.GetSeqKeyRequest;
import org.oagi.score.repo.api.corecomponent.seqkey.model.GetSeqKeyResponse;
import org.oagi.score.repo.api.corecomponent.seqkey.model.SeqKey;
import org.oagi.score.repo.api.impl.jooq.AbstractJooqScoreRepositoryTest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeqKeyReadRepositoryTest
        extends AbstractJooqScoreRepositoryTest {

    private SeqKeyReadRepository repository;
    private ScoreUser requester;

    @BeforeAll
    void setUp() {
        repository = scoreRepositoryFactory().createSeqKeyReadRepository();
        requester = new ScoreUser(BigInteger.ONE, "oagis", DEVELOPER);
    }

    @Test
    @Order(1)
    public void getSeqKeyOfBusinessObjectDocumentTest() {
        GetSeqKeyRequest request = new GetSeqKeyRequest(requester)
                .withFromAccManifestId(getAccManifestIdByObjectClassTerm("Business Object Document"));
        GetSeqKeyResponse response = repository.getSeqKey(request);
        assertNotNull(response.getSeqKey());

        List<SeqKey> seqKeys = new ArrayList();
        for (SeqKey seqKey : response.getSeqKey()) {
            seqKeys.add(seqKey);
        }

        assertTrue(seqKeys.size() == 5);

        assertNotNull(seqKeys.get(0).getBccManifestId());
        assertEquals("Release Identifier", getBccpPropertyTerm(seqKeys.get(0).getBccManifestId()));
        assertEquals(EntityType.Attribute, seqKeys.get(0).getEntityType());

        assertNotNull(seqKeys.get(1).getBccManifestId());
        assertEquals("Version Identifier", getBccpPropertyTerm(seqKeys.get(1).getBccManifestId()));
        assertEquals(EntityType.Attribute, seqKeys.get(1).getEntityType());

        assertNotNull(seqKeys.get(2).getBccManifestId());
        assertEquals("System Environment Code", getBccpPropertyTerm(seqKeys.get(2).getBccManifestId()));
        assertEquals(EntityType.Attribute, seqKeys.get(2).getEntityType());

        assertNotNull(seqKeys.get(3).getBccManifestId());
        assertEquals("Language Code", getBccpPropertyTerm(seqKeys.get(3).getBccManifestId()));
        assertEquals(EntityType.Attribute, seqKeys.get(3).getEntityType());

        assertNotNull(seqKeys.get(4).getAsccManifestId());
        assertEquals("Application Area", getAsccpPropertyTerm(seqKeys.get(4).getAsccManifestId()));
        assertEquals(null, seqKeys.get(4).getEntityType());
    }

    private BigInteger getAccManifestIdByObjectClassTerm(String objectClassTerm) {
        return dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        ACC.OBJECT_CLASS_TERM.eq(objectClassTerm),
                        RELEASE.RELEASE_NUM.eq("10.6")
                ))
                .fetchOneInto(BigInteger.class);
    }

    private String getAsccpPropertyTerm(BigInteger asccManifestId) {
        return dslContext()
                .select(ASCCP.PROPERTY_TERM)
                .from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(ASCC_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOneInto(String.class);
    }

    private String getBccpPropertyTerm(BigInteger bccManifestId) {
        return dslContext()
                .select(BCCP.PROPERTY_TERM)
                .from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(BCC_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCC_MANIFEST.TO_BCCP_MANIFEST_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchOneInto(String.class);
    }

}
