package org.oagi.score.repo;

import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.ScoreHttpApplication;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = ScoreHttpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class CoreComponentRepositoryTest {

    @Autowired
    private CoreComponentRepository ccRepository;

    @Test
    public void getAsccpManifestByManifestIdTestWithValidManifestId() {
        ULong manifestId = ULong.valueOf(1L);

        AsccpManifestRecord asccpManifest =
                ccRepository.getAsccpManifestByManifestId(manifestId);

        assertNotNull(asccpManifest);
        assertEquals(manifestId, asccpManifest.getAsccpManifestId());
    }

    @Test
    public void getAsccpManifestByManifestIdTestWithInvalidManifestId() {
        ULong manifestId = ULong.valueOf(0L);

        AsccpManifestRecord asccpManifest =
                ccRepository.getAsccpManifestByManifestId(manifestId);

        assertNull(asccpManifest);
    }
}
