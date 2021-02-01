package org.oagi.score.service.bie;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.repo.api.release.model.GetReleaseRequest;
import org.oagi.score.repo.api.user.model.GetScoreUserRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.AbstractServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.jooq.impl.DSL.and;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@ExtendWith(SpringExtension.class)
public class BieUpliftingServiceTest extends AbstractServiceTest {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private BieUpliftingService bieUpliftingService;

    @Autowired
    private DSLContext dslContext;

    private TopLevelAsbiepRecord topLevelAsbiepRecord(String propertyTerm, String release) {
        return dslContext.select(TOP_LEVEL_ASBIEP.fields())
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(RELEASE).on(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(and(
                        ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID),
                        ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID)))
                .where(and(
                        ASCCP.PROPERTY_TERM.eq(propertyTerm),
                        RELEASE.RELEASE_NUM.eq(release)
                ))
                .limit(1)
                .fetchOptionalInto(TopLevelAsbiepRecord.class).orElse(null);
    }

    @Test
    @EnabledIfBiePresent(propertyTerm = "Enterprise Unit", release = "10.6")
    public void testAnalysisBieUpliftingWithSameRelease() {
        TopLevelAsbiepRecord topLevelAsbiepRecord =
                topLevelAsbiepRecord("Enterprise Unit", "10.6");

        ScoreUser requester = scoreRepositoryFactory.createScoreUserReadRepository()
                .getScoreUser(new GetScoreUserRequest()
                        .withUserId(topLevelAsbiepRecord.getOwnerUserId().toBigInteger())
                )
                .getUser();

        AnalysisBieUpliftingRequest request = new AnalysisBieUpliftingRequest();
        request.setRequester(requester);
        request.setTopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger());
        request.setTargetReleaseId(topLevelAsbiepRecord.getReleaseId().toBigInteger());

        assertThrows(IllegalArgumentException.class, () -> {
            bieUpliftingService.analysisBieUplifting(request);
        });
    }

    @Test
    public void testAnalysisBieUplifting() {
        TopLevelAsbiepRecord topLevelAsbiepRecord =
                topLevelAsbiepRecord("Enterprise Unit", "10.6");

        AnalysisBieUpliftingRequest request = new AnalysisBieUpliftingRequest();
        request.setRequester(scoreRepositoryFactory.createScoreUserReadRepository()
                .getScoreUser(new GetScoreUserRequest()
                        .withUserId(topLevelAsbiepRecord.getOwnerUserId().toBigInteger())
                )
                .getUser());
        request.setTopLevelAsbiepId(topLevelAsbiepRecord.getTopLevelAsbiepId().toBigInteger());
        request.setTargetReleaseId(scoreRepositoryFactory.createReleaseReadRepository()
                .getRelease(new GetReleaseRequest(request.getRequester())
                        .withReleaseNum("10.7")
                )
                .getRelease().getReleaseId());

        AnalysisBieUpliftingResponse response =
                bieUpliftingService.analysisBieUplifting(request);

        assertNotNull(response);
    }
}
