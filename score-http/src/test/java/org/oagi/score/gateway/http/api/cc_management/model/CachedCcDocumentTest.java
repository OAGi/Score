package org.oagi.score.gateway.http.api.cc_management.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.SpringCcCache;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.CcQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.SeqKeyQueryRepository;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.redisson.api.RedissonClient;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CachedCcDocumentTest {

    private static final ReleaseId RELEASE = new ReleaseId(BigInteger.valueOf(76));
    private static final ReleaseId OTHER_RELEASE = new ReleaseId(BigInteger.valueOf(99));

    private final ScoreUser requester = mock(ScoreUser.class);
    private final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
    private final AccQueryRepository accQuery = mock(AccQueryRepository.class);
    private final AsccpQueryRepository asccpQuery = mock(AsccpQueryRepository.class);
    private final BccpQueryRepository bccpQuery = mock(BccpQueryRepository.class);
    private final CcQueryRepository ccQuery = mock(CcQueryRepository.class);
    private final DtQueryRepository dtQuery = mock(DtQueryRepository.class);
    private final SeqKeyQueryRepository seqKeyQuery = mock(SeqKeyQueryRepository.class);
    private final TagQueryRepository tagQuery = mock(TagQueryRepository.class);
    private final ReleaseQueryRepository releaseQuery = mock(ReleaseQueryRepository.class);
    private SpringCcCache cache;

    @BeforeEach
    void setUp() {
        cache = new SpringCcCache(new ConcurrentMapCacheManager(), org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady());
        when(repositoryFactory.ccCache()).thenReturn(cache);
        when(repositoryFactory.accQueryRepository(requester)).thenReturn(accQuery);
        when(repositoryFactory.asccpQueryRepository(requester)).thenReturn(asccpQuery);
        when(repositoryFactory.bccpQueryRepository(requester)).thenReturn(bccpQuery);
        when(repositoryFactory.ccQueryRepository(requester)).thenReturn(ccQuery);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
        when(repositoryFactory.seqKeyQueryRepository(requester)).thenReturn(seqKeyQuery);
        when(seqKeyQuery.getSeqKeySummaryListByFromAccManifestIds(any())).thenReturn(List.of());
        when(repositoryFactory.tagQueryRepository(requester)).thenReturn(tagQuery);
        when(repositoryFactory.releaseQueryRepository(requester)).thenReturn(releaseQuery);
        ReleaseSummaryRecord release = release(RELEASE);
        when(releaseQuery.getIncludedReleaseSummaryList(RELEASE)).thenReturn(Set.of(release));
    }

    private static ReleaseSummaryRecord release(ReleaseId releaseId) {
        ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);
        when(release.releaseId()).thenReturn(releaseId);
        return release;
    }

    private static AccManifestId accId(long value) {
        return new AccManifestId(BigInteger.valueOf(value));
    }

    private static AccSummaryRecord acc(long manifestId, ReleaseId releaseId, Long basedManifestId) {
        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        ReleaseSummaryRecord release = release(releaseId);
        when(acc.accManifestId()).thenReturn(accId(manifestId));
        when(acc.release()).thenReturn(release);
        when(acc.basedAccManifestId()).thenReturn(basedManifestId == null ? null : accId(basedManifestId));
        return acc;
    }

    private CachedCcDocument document() {
        return new CachedCcDocument(requester, repositoryFactory, RELEASE);
    }

    @Test
    void aLookupIsReadOnceAndThenServedToEveryDocument() {
        AccSummaryRecord acc1 = acc(1, RELEASE, null);
        when(accQuery.getAccSummary(accId(1))).thenReturn(acc1);

        assertNotNull(document().getAcc(accId(1)));
        assertNotNull(document().getAcc(accId(1)));
        assertNotNull(document().getAcc(accId(1)));

        verify(accQuery, times(1)).getAccSummary(accId(1));
    }

    @Test
    void aComponentOfAReleaseOutOfScopeIsNotFound() {
        AccSummaryRecord acc2 = acc(2, OTHER_RELEASE, null);
        when(accQuery.getAccSummary(accId(2))).thenReturn(acc2);

        assertNull(document().getAcc(accId(2)));
        assertNull(document().getAcc(null));
    }

    @Test
    void aMissingComponentIsNotFoundAndIsNotCached() {
        assertNull(document().getAcc(accId(3)));
        assertNull(document().getAcc(accId(3)));

        verify(accQuery, times(2)).getAccSummary(accId(3));
    }

    @Test
    void theListUnderAParentIsCachedEvenWhenEmpty() {
        when(accQuery.getAsccSummaryList(accId(4))).thenReturn(List.of());

        assertTrue(document().getAsccListByFromAccManifestId(accId(4)).isEmpty());
        assertTrue(document().getAsccListByFromAccManifestId(accId(4)).isEmpty());

        verify(accQuery, times(1)).getAsccSummaryList(accId(4));
    }

    @Test
    void aDocumentThatKeepsFindingNothingReadsTheGraphOfTheFirstComponentItLookedUp() {
        AccSummaryRecord first = acc(100, RELEASE, null);
        when(accQuery.getAccSummary(any())).thenAnswer(invocation -> {
            AccManifestId id = invocation.getArgument(0);
            return id.equals(accId(100)) ? first : acc(id.value().longValue(), RELEASE, null);
        });
        when(ccQuery.getGraphManifestIds(CcType.ACC, accId(100))).thenReturn(new GraphManifestIds(
                Set.of(accId(100), accId(200)), Set.of(), Set.of(), Set.of()));
        AccSummaryRecord reached = acc(200, RELEASE, null);
        when(accQuery.getAccSummaryListByManifestIds(any())).thenReturn(List.of(reached));
        when(accQuery.getAsccSummaryListByFromAccManifestIds(any())).thenReturn(List.of());
        when(accQuery.getBccSummaryListByFromAccManifestIds(any())).thenReturn(List.of());
        when(asccpQuery.getAsccpSummaryListByManifestIds(any())).thenReturn(List.of());
        when(tagQuery.getTagSummariesByPairsOfTypeAndManifestId(any())).thenReturn(Map.of());

        CachedCcDocument document = document();
        for (int i = 0; i < 15; i++) {
            document.getAcc(accId(100 + i));
        }
        verify(ccQuery, never()).getGraphManifestIds(any(), any());

        document.getAcc(accId(115));

        verify(ccQuery, times(1)).getGraphManifestIds(CcType.ACC, accId(100));
        verify(accQuery, times(1)).getAccSummaryListByManifestIds(any());
        assertSame(reached, document.getAcc(accId(200)));
        verify(accQuery, never()).getAccSummary(accId(200));
    }

    @Test
    void aTransactionThatHasWrittenReadsTheDatabaseAndCachesNothing() {
        when(repositoryFactory.ccCache()).thenReturn(CcCache.DISABLED);
        CachedCcDocument uncached = new CachedCcDocument(requester, repositoryFactory, RELEASE);
        AccSummaryRecord acc5 = acc(5, RELEASE, null);
        when(accQuery.getAccSummary(accId(5))).thenReturn(acc5);

        assertNotNull(uncached.getAcc(accId(5)));
        assertNotNull(uncached.getAcc(accId(5)));

        verify(accQuery, times(2)).getAccSummary(accId(5));
        assertNull(cache.get("acc:5"));
    }

    @Test
    void theTagsOfAComponentOutOfScopeAreNotGivenAndTheOthersComeInTagIdOrder() {
        AccSummaryRecord inScope = acc(30, RELEASE, null);
        AccSummaryRecord outOfScope = acc(31, OTHER_RELEASE, null);
        when(accQuery.getAccSummary(accId(30))).thenReturn(inScope);
        when(accQuery.getAccSummary(accId(31))).thenReturn(outOfScope);
        TagSummaryRecord first = new TagSummaryRecord(new TagId(BigInteger.valueOf(2)), "Noun", "#fff", "#000");
        TagSummaryRecord second = new TagSummaryRecord(new TagId(BigInteger.valueOf(3)), "Verb", "#fff", "#000");
        when(tagQuery.getTagSummaryList(accId(30))).thenReturn(List.of(second, first));
        when(tagQuery.getTagSummaryList(accId(31))).thenReturn(List.of(first));

        assertEquals(List.of(first, second), document().getTagListByAccManifestId(accId(30)));
        assertTrue(document().getTagListByAccManifestId(accId(31)).isEmpty());
        assertTrue(document().getTagListByAccManifestId(accId(32)).isEmpty());
    }

    @Test
    void prefetchingAGraphLoadsWhatItReachesOnceAndLeavesNoLookupToTheDatabase() {
        ReleaseSummaryRecord release = release(RELEASE);
        AccSummaryRecord root = acc(40, RELEASE, null);
        AsccSummaryRecord ascc = mock(AsccSummaryRecord.class);
        when(ascc.fromAccManifestId()).thenReturn(accId(40));
        when(ascc.toAsccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(41)));
        when(ascc.release()).thenReturn(release);
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.asccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(41)));
        when(asccp.roleOfAccManifestId()).thenReturn(accId(40));
        when(asccp.release()).thenReturn(release);

        when(ccQuery.getGraphManifestIds(CcType.ACC, accId(40))).thenReturn(new GraphManifestIds(
                Set.of(accId(40)), Set.of(new AsccpManifestId(BigInteger.valueOf(41))), Set.of(), Set.of()));
        when(accQuery.getAccSummaryListByManifestIds(any())).thenReturn(List.of(root));
        when(accQuery.getAsccSummaryListByFromAccManifestIds(any())).thenReturn(List.of(ascc));
        when(accQuery.getBccSummaryListByFromAccManifestIds(any())).thenReturn(List.of());
        when(asccpQuery.getAsccpSummaryListByManifestIds(any())).thenReturn(List.of(asccp));
        when(tagQuery.getTagSummariesByPairsOfTypeAndManifestId(any())).thenReturn(Map.of());

        CachedCcDocument document = document();
        document.prefetchGraph(CcType.ACC, accId(40));
        document().prefetchGraph(CcType.ACC, accId(40));

        verify(ccQuery, times(1)).getGraphManifestIds(CcType.ACC, accId(40));
        assertSame(root, document.getAcc(accId(40)));
        assertEquals(List.of(ascc), document.getAsccListByFromAccManifestId(accId(40)));
        assertSame(asccp, document.getAsccp(new AsccpManifestId(BigInteger.valueOf(41))));
        verify(accQuery, never()).getAccSummary(any());
        verify(accQuery, never()).getAsccSummaryList(any(AccManifestId.class));
        verify(asccpQuery, never()).getAsccpSummary(any(AsccpManifestId.class));
    }

    @Test
    void prefetchingTheChildrenLoadsTheWholeLevelInBatches() {
        AccSummaryRecord root = acc(10, RELEASE, 11L);
        AccSummaryRecord based = acc(11, RELEASE, null);
        ReleaseSummaryRecord release = release(RELEASE);
        AsccSummaryRecord ascc = mock(AsccSummaryRecord.class);
        when(ascc.fromAccManifestId()).thenReturn(accId(10));
        when(ascc.toAsccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(20)));
        when(ascc.release()).thenReturn(release);
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.asccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(20)));
        when(asccp.release()).thenReturn(release);

        when(accQuery.getAccSummaryListByManifestIds(any())).thenAnswer(invocation -> {
            Collection<AccManifestId> ids = invocation.getArgument(0);
            return ids.contains(accId(10)) ? List.of(root) : List.of(based);
        });
        when(accQuery.getAsccSummaryListByFromAccManifestIds(any())).thenReturn(List.of(ascc));
        when(accQuery.getBccSummaryListByFromAccManifestIds(any())).thenReturn(List.of());
        when(asccpQuery.getAsccpSummaryListByManifestIds(any())).thenReturn(List.of(asccp));
        when(tagQuery.getTagSummariesByPairsOfTypeAndManifestId(any())).thenReturn(Map.of());

        CachedCcDocument document = document();
        document.prefetchChildren(CcType.ACC, List.of(accId(10)));

        assertSame(root, document.getAcc(accId(10)));
        assertSame(based, document.getAcc(accId(11)));
        assertEquals(List.of(ascc), document.getAsccListByFromAccManifestId(accId(10)));
        assertTrue(document.getBccListByFromAccManifestId(accId(10)).isEmpty());
        assertSame(asccp, document.getAsccp(new AsccpManifestId(BigInteger.valueOf(20))));
        assertTrue(document.getTagListByAsccpManifestId(new AsccpManifestId(BigInteger.valueOf(20))).isEmpty());

        verify(accQuery, never()).getAccSummary(any());
        verify(accQuery, never()).getAsccSummaryList(any(AccManifestId.class));
        verify(asccpQuery, never()).getAsccpSummary(any(AsccpManifestId.class));
        verify(tagQuery, never()).getTagSummaryList(any(AccManifestId.class));
    }

}
