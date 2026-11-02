package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.*;
import org.oagi.score.gateway.http.api.bie_management.repository.AsbiepQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Package-level lists of the BIE Package manifest (new / removed / changed BIEs against the prior package version).
 *
 * <p>The repositories and the BIE documents are mocked: every BIE has an empty tree of its own, so the tests are about
 * how the BIEs of a package are paired with the BIEs of the prior package version and sorted into the lists.
 */
class BiePackageManifestServiceTest {

    private static final BiePackageId V1 = new BiePackageId(BigInteger.valueOf(1));
    private static final BiePackageId V2 = new BiePackageId(BigInteger.valueOf(2));

    private static final Guid ORDER = new Guid("a".repeat(32));
    private static final Guid INVOICE = new Guid("b".repeat(32));

    private final ScoreUser requester = mock(ScoreUser.class);
    private final LibraryId libraryId = new LibraryId(BigInteger.ONE);
    private final ReleaseId releaseId = new ReleaseId(BigInteger.ONE);
    private final LibrarySummaryRecord library = mock(LibrarySummaryRecord.class);
    private final ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);

    private final BiePackageQueryRepository biePackageQueryRepository = mock(BiePackageQueryRepository.class);
    private final TopLevelAsbiepQueryRepository topLevelAsbiepQueryRepository = mock(TopLevelAsbiepQueryRepository.class);
    private final BieReadService bieReadService = mock(BieReadService.class);
    private final ReleaseQueryRepository releaseQueryRepository = mock(ReleaseQueryRepository.class);

    private final BiePackageManifestService service = new BiePackageManifestService();

    @BeforeEach
    void wireRepositories() {
        when(library.libraryId()).thenReturn(libraryId);
        when(release.releaseId()).thenReturn(releaseId);

        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.biePackageQueryRepository(any())).thenReturn(biePackageQueryRepository);
        when(repositoryFactory.topLevelAsbiepQueryRepository(any())).thenReturn(topLevelAsbiepQueryRepository);
        when(repositoryFactory.asbiepQueryRepository(any())).thenReturn(mock(AsbiepQueryRepository.class));
        when(repositoryFactory.businessContextQueryRepository(any())).thenReturn(mock(BusinessContextQueryRepository.class));
        when(repositoryFactory.contextSchemeQueryRepository(any())).thenReturn(mock(ContextSchemeQueryRepository.class));

        LibraryQueryRepository libraryQueryRepository = mock(LibraryQueryRepository.class);
        LibraryDetailsRecord libraryDetails = mock(LibraryDetailsRecord.class);
        when(libraryDetails.name()).thenReturn("connectSpec");
        when(libraryQueryRepository.getLibraryDetails(libraryId)).thenReturn(libraryDetails);
        when(repositoryFactory.libraryQueryRepository(any())).thenReturn(libraryQueryRepository);

        ReleaseDetailsRecord releaseDetails = mock(ReleaseDetailsRecord.class);
        when(releaseDetails.releaseId()).thenReturn(releaseId);
        when(releaseQueryRepository.getReleaseSummaryList(libraryId)).thenReturn(List.of(release));
        when(releaseQueryRepository.getReleaseDetails(releaseId)).thenReturn(releaseDetails);
        when(repositoryFactory.releaseQueryRepository(any())).thenReturn(releaseQueryRepository);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "bieReadService", bieReadService);
    }

    private TopLevelAsbiepId id(long value) {
        return new TopLevelAsbiepId(BigInteger.valueOf(value));
    }

    private BiePackageSummaryRecord pkg(BiePackageId id, BiePackageId prev, String versionId) {
        BiePackageSummaryRecord record = mock(BiePackageSummaryRecord.class);
        when(record.biePackageId()).thenReturn(id);
        when(record.prevBiePackageId()).thenReturn(prev);
        when(record.guid()).thenReturn(new Guid("c".repeat(32)));
        when(record.name()).thenReturn("XYZ Core");
        when(record.versionId()).thenReturn(versionId);
        when(record.versionName()).thenReturn("v" + versionId);
        when(record.versionGuid()).thenReturn(new Guid("d".repeat(32)));
        return record;
    }

    /** A BIE about the given ASCCP, with an empty tree. */
    private TopLevelAsbiepSummaryRecord bie(long id, Long source, Guid asccpGuid) {
        TopLevelAsbiepSummaryRecord record = mock(TopLevelAsbiepSummaryRecord.class);
        when(record.library()).thenReturn(library);
        when(record.release()).thenReturn(release);
        when(record.topLevelAsbiepId()).thenReturn(id(id));
        when(record.sourceTopLevelAsbiepId()).thenReturn(source == null ? null : id(source));
        when(record.guid()).thenReturn(new Guid(String.valueOf(id % 10).repeat(32)));
        when(record.den()).thenReturn("BIE " + id);
        when(record.version()).thenReturn("1.0");
        when(topLevelAsbiepQueryRepository.getTopLevelAsbiepSummary(id(id))).thenReturn(record);

        BieDocument document = mock(BieDocument.class);
        Asbiep root = mock(Asbiep.class);
        AsccpManifestId asccpManifestId = new AsccpManifestId(BigInteger.valueOf(id));
        when(root.getBasedAsccpManifestId()).thenReturn(asccpManifestId);
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.guid()).thenReturn(asccpGuid);
        when(asccp.propertyTerm()).thenReturn("Term");
        CcDocument ccDocument = mock(CcDocument.class);
        when(ccDocument.getAsccp(asccpManifestId)).thenReturn(asccp);
        when(document.getRootAsbiep()).thenReturn(root);
        when(document.getCcDocument()).thenReturn(ccDocument);
        when(document.getAssociations(any())).thenReturn(Collections.emptyList());
        when(bieReadService.getBieDocument(any(), Mockito.eq(id(id)))).thenReturn(document);
        return record;
    }

    private void packageHolds(BiePackageId packageId, TopLevelAsbiepSummaryRecord... bies) {
        List<TopLevelAsbiepId> ids = Arrays.stream(bies).map(TopLevelAsbiepSummaryRecord::topLevelAsbiepId).toList();
        when(biePackageQueryRepository.getTopLevelAsbiepIdListInBiePackage(packageId)).thenReturn(ids);
    }

    private BiePackageManifest manifestOf(BiePackageSummaryRecord current, BiePackageSummaryRecord prior) {
        when(biePackageQueryRepository.getBiePackageSummary(current.biePackageId())).thenReturn(current);
        if (prior != null) {
            when(biePackageQueryRepository.getBiePackageSummary(prior.biePackageId())).thenReturn(prior);
        }
        return service.getBiePackageManifest(requester, current.biePackageId(), "/").biePackage();
    }

    private static List<Guid> uuids(Collection<BieManifestSummary> summaries) {
        return summaries.stream().map(BieManifestSummary::uuid).toList();
    }

    private static BiePackageManifestEntry entryOf(BiePackageManifest manifest, TopLevelAsbiepSummaryRecord bie) {
        return manifest.bieList().stream().filter(e -> e.bie().uuid().equals(bie.guid())).findFirst().orElseThrow();
    }

    private ReleaseSummaryRecord releaseSummary(long id, String num) {
        ReleaseId releaseId = new ReleaseId(BigInteger.valueOf(id));
        ReleaseSummaryRecord summary = mock(ReleaseSummaryRecord.class);
        when(summary.releaseId()).thenReturn(releaseId);
        when(summary.releaseNum()).thenReturn(num);
        return summary;
    }

    private void chain(ReleaseSummaryRecord from, ReleaseSummaryRecord next) {
        ReleaseId fromId = from.releaseId();
        ReleaseDetailsRecord details = mock(ReleaseDetailsRecord.class);
        when(details.releaseId()).thenReturn(fromId);
        when(details.next()).thenReturn(next);
        when(releaseQueryRepository.getReleaseDetails(fromId)).thenReturn(details);
    }

    private TopLevelAsbiepSummaryRecord bieIn(ReleaseSummaryRecord in, long id, Guid asccpGuid) {
        TopLevelAsbiepSummaryRecord record = bie(id, null, asccpGuid);
        when(record.release()).thenReturn(in);
        return record;
    }

    @Test
    void the_latest_release_is_found_by_following_next_and_not_by_release_id() {
        // The later release in the chain deliberately has the smaller release_id.
        ReleaseSummaryRecord earlier = releaseSummary(9, "10.11");
        ReleaseSummaryRecord later = releaseSummary(3, "10.12");
        ReleaseSummaryRecord working = releaseSummary(4, "Working");
        chain(earlier, later);
        chain(later, working);
        chain(working, null);

        packageHolds(V1, bieIn(earlier, 11, ORDER), bieIn(later, 12, INVOICE));
        BiePackageManifest manifest = manifestOf(pkg(V1, null, "1"), null);

        assertThat(manifest.libraryCompatibility())
                .extracting(LibraryCompatibility::minimumLibraryReleaseVersion).containsExactly("10.12");
    }

    @Test
    void a_release_outside_the_release_chain_does_not_affect_the_latest_release() {
        // An Initialized release has neither prev nor next, so it is not reachable from the chain.
        ReleaseSummaryRecord initialized = releaseSummary(2, "Initialized");
        chain(initialized, null);
        when(releaseQueryRepository.getReleaseSummaryList(libraryId)).thenReturn(List.of(initialized, release));
        when(release.releaseNum()).thenReturn("10.12");
        chain(release, null);

        packageHolds(V1, bie(11, null, ORDER));
        BiePackageManifest manifest = manifestOf(pkg(V1, null, "1"), null);

        assertThat(manifest.libraryCompatibility())
                .extracting(LibraryCompatibility::minimumLibraryReleaseVersion).containsExactly("10.12");
    }

    @Test
    void releases_on_different_release_chains_are_not_supported() {
        ReleaseSummaryRecord onFirstChain = releaseSummary(5, "1.0");
        ReleaseSummaryRecord onSecondChain = releaseSummary(6, "2.0");
        chain(onFirstChain, null);
        chain(onSecondChain, null);

        packageHolds(V1, bieIn(onFirstChain, 11, ORDER), bieIn(onSecondChain, 12, INVOICE));

        assertThatThrownBy(() -> manifestOf(pkg(V1, null, "1"), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void a_bie_added_in_the_revised_package_is_a_new_bie() {
        var a = bie(1, null, ORDER);
        var b = bie(2, null, INVOICE);
        packageHolds(V1, a);
        packageHolds(V2, a, b);

        var manifest = manifestOf(pkg(V2, V1, "2"), pkg(V1, null, "1"));

        assertThat(uuids(manifest.newBiesFromPriorPackageVersion())).containsExactly(b.guid());
        assertThat(manifest.removedBiesFromPriorPackageVersion()).isEmpty();
        assertThat(entryOf(manifest, b).includedInPriorPackageVersion()).isFalse();
        assertThat(entryOf(manifest, a).includedInPriorPackageVersion()).isTrue();
    }

    @Test
    void a_bie_kept_without_a_change_is_neither_new_nor_removed() {
        var a = bie(1, null, ORDER);
        packageHolds(V1, a);
        packageHolds(V2, a);

        var manifest = manifestOf(pkg(V2, V1, "2"), pkg(V1, null, "1"));

        assertThat(manifest.newBiesFromPriorPackageVersion()).isEmpty();
        assertThat(manifest.removedBiesFromPriorPackageVersion()).isEmpty();
        assertThat(manifest.changedBiesFromPriorPackageVersion()).isEmpty();
    }

    @Test
    void a_bie_dropped_from_the_revised_package_is_a_removed_bie() {
        var a = bie(1, null, ORDER);
        var c = bie(3, null, INVOICE);
        packageHolds(V1, a, c);
        packageHolds(V2, a);

        var manifest = manifestOf(pkg(V2, V1, "2"), pkg(V1, null, "1"));

        assertThat(uuids(manifest.removedBiesFromPriorPackageVersion())).containsExactly(c.guid());
        assertThat(manifest.newBiesFromPriorPackageVersion()).isEmpty();
    }

    @Test
    void the_first_version_of_a_package_has_no_new_bies_because_there_is_nothing_to_compare_with() {
        var a = bie(1, null, ORDER);
        packageHolds(V1, a);

        var manifest = manifestOf(pkg(V1, null, "1"), null);

        assertThat(manifest.newBiesFromPriorPackageVersion()).isEmpty();
        assertThat(manifest.removedBiesFromPriorPackageVersion()).isEmpty();
        assertThat(entryOf(manifest, a).includedInPriorPackageVersion()).isFalse();
    }

    @Test
    void a_bie_about_another_asccp_than_the_prior_bie_it_was_made_from_is_new_and_the_prior_bie_is_removed() {
        // 5 was made from 4 but is about another ASCCP: the two cannot be compared, so they are not paired: 5 is
        // new and 4 is removed
        var four = bie(4, null, ORDER);
        var five = bie(5, 4L, INVOICE);
        packageHolds(V1, four);
        packageHolds(V2, five);

        var manifest = manifestOf(pkg(V2, V1, "2"), pkg(V1, null, "1"));

        assertThat(uuids(manifest.newBiesFromPriorPackageVersion())).containsExactly(five.guid());
        assertThat(uuids(manifest.removedBiesFromPriorPackageVersion())).containsExactly(four.guid());
        assertThat(entryOf(manifest, five).includedInPriorPackageVersion()).isFalse();
    }
}
