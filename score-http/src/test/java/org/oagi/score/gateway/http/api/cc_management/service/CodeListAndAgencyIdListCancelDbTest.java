package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListCommandService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListCommandService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * Cancelling the revision of a Code List and of an Agency ID List: the rows of the list, and of what it holds, are as they
 * were before the revision, whatever was done to it, including the Agency ID of a Code List and the value an Agency ID List
 * takes as its own, which the manifest holds.
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CodeListAndAgencyIdListCancelDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 */
class CodeListAndAgencyIdListCancelDbTest extends CcReviseCancelFixture {

    CodeListCommandService codeLists;
    AgencyIdListCommandService agencyLists;

    @BeforeEach
    void services() {
        codeLists = new CodeListCommandService();
        ReflectionTestUtils.setField(codeLists, "repositoryFactory", factory);
        ReflectionTestUtils.setField(codeLists, "stateChangeEventPublisher", mock(ComponentStateChangeEventPublisher.class));
        agencyLists = new AgencyIdListCommandService();
        ReflectionTestUtils.setField(agencyLists, "repositoryFactory", factory);
        ReflectionTestUtils.setField(agencyLists, "stateChangeEventPublisher", mock(ComponentStateChangeEventPublisher.class));
    }

    // ---- dumps ----

    String dump(String sql, Object... bindings) {
        return dsl.fetch(sql, bindings).stream().map(r -> r.intoMap().toString()).collect(Collectors.joining("\n"));
    }

    String codeListState(String guid) {
        return dump("select * from code_list where guid = ? order by code_list_id", guid) + "\n--\n"
                + dump("select * from code_list_value where code_list_id in (select code_list_id from code_list where guid = ?) order by code_list_value_id", guid) + "\n--\n"
                + dump("select * from code_list_manifest where code_list_id in (select code_list_id from code_list where guid = ?) order by code_list_manifest_id", guid) + "\n--\n"
                + dump("select * from code_list_value_manifest where code_list_manifest_id in (select code_list_manifest_id from code_list_manifest where code_list_id in (select code_list_id from code_list where guid = ?)) order by code_list_value_manifest_id", guid);
    }

    String agencyListState(String guid) {
        return dump("select * from agency_id_list where guid = ? order by agency_id_list_id", guid) + "\n--\n"
                + dump("select * from agency_id_list_value where owner_list_id in (select agency_id_list_id from agency_id_list where guid = ?) order by agency_id_list_value_id", guid) + "\n--\n"
                + dump("select * from agency_id_list_manifest where agency_id_list_id in (select agency_id_list_id from agency_id_list where guid = ?) order by agency_id_list_manifest_id", guid) + "\n--\n"
                + dump("select * from agency_id_list_value_manifest where agency_id_list_manifest_id in (select agency_id_list_manifest_id from agency_id_list_manifest where agency_id_list_id in (select agency_id_list_id from agency_id_list where guid = ?)) order by agency_id_list_value_manifest_id", guid);
    }

    long codeListWithValues(int min, int max) {
        return ids("select m.code_list_manifest_id from code_list_manifest m join code_list c on c.code_list_id = m.code_list_id "
                + "where m.release_id = ? and c.state = 'Published' and c.next_code_list_id is null and m.based_code_list_manifest_id is null "
                + "and (select count(*) from code_list_value_manifest v where v.code_list_manifest_id = m.code_list_manifest_id) between ? and ? "
                + "order by 1 limit 1", release, min, max).get(0);
    }

    String guidOfCodeList(long m) {
        return dsl.fetchOne("select c.guid from code_list c join code_list_manifest m on m.code_list_id = c.code_list_id where m.code_list_manifest_id = ?", m).get(0, String.class);
    }

    long codeListRecord(long m) {
        return one("select code_list_id from code_list_manifest where code_list_manifest_id = ?", m);
    }

    CodeListManifestId cl(long m) {
        return new CodeListManifestId(n(m));
    }

    void reviseCl(long m) {
        codeLists.revise(requester, cl(m));
    }

    void cancelCl(long m) {
        codeLists.cancel(requester, cl(m), null, null);
    }

    void assertSame(String before, String after) {
        if (before.equals(after)) {
            return;
        }
        List<String> b = List.of(before.split("\n"));
        List<String> a = List.of(after.split("\n"));
        StringBuilder diff = new StringBuilder("DIFF\n");
        for (String line : b) {
            if (!a.contains(line)) {
                diff.append("  - ").append(line).append('\n');
            }
        }
        for (String line : a) {
            if (!b.contains(line)) {
                diff.append("  + ").append(line).append('\n');
            }
        }
        fail(diff.toString());
    }

    // ---- Code List ----

    @Test
    void codeListPlain() {
        useBranch(Branch.DEVELOPER);
        long m = codeListWithValues(2, 20);
        String guid = guidOfCodeList(m);
        String before = codeListState(guid);
        reviseCl(m);
        cancelCl(m);
        assertSame(before, codeListState(guid));
    }

    @Test
    void codeListValueAddedAndEdited() {
        useBranch(Branch.DEVELOPER);
        long m = codeListWithValues(2, 20);
        String guid = guidOfCodeList(m);
        String before = codeListState(guid);
        reviseCl(m);
        var repo = factory.codeListCommandRepository(requester);
        var details = factory.codeListQueryRepository(requester).getCodeListDetails(cl(m));
        repo.createValue(cl(m), details.codeListId(), details.release().releaseId(), "ZZNEW", "A new value", null, false);
        long existing = ids("select code_list_value_manifest_id from code_list_value_manifest where code_list_manifest_id = ? order by 1 limit 1", m).get(0);
        repo.updateValue(new CodeListValueManifestId(n(existing)), "EDITED", "Edited meaning", null, false);
        cancelCl(m);
        assertSame(before, codeListState(guid));
    }

    @Test
    void codeListRevisedTwice() {
        useBranch(Branch.DEVELOPER);
        long m = codeListWithValues(2, 20);
        String guid = guidOfCodeList(m);
        String before = codeListState(guid);
        reviseCl(m);
        cancelCl(m);
        reviseCl(m);
        cancelCl(m);
        assertSame(before, codeListState(guid));
    }


    // ---- the Code List of an end-user: there is no manifest of a previous release, the log is what is left ----

    /** An end-user makes a Code List in a release, as the Agency ID {@code first}, and it is Production. */
    long endUserCodeList(Long first) {
        useBranch(Branch.END_USER);
        release = one("select r.release_id from `release` r join library l on l.library_id = r.library_id where l.name = 'connectSpec' and r.release_num = '10.8.8'");
        library = one("select library_id from `release` where release_id = ?", release);
        long m = codeLists.create(requester, new org.oagi.score.gateway.http.api.code_list_management.controller.payload.CreateCodeListRequest(
                new org.oagi.score.gateway.http.api.library_management.model.LibraryId(n(library)), new ReleaseId(n(release)), null)).value().longValue();
        updateCl(m, first, "1");
        dsl.execute("update code_list set state = 'Production' where code_list_id = (select code_list_id from code_list_manifest where code_list_manifest_id = ?)", m);
        return m;
    }

    void updateCl(long m, Long agency, String version) {
        var details = factory.codeListQueryRepository(requester).getCodeListDetails(cl(m));
        long namespace = one("select namespace_id from namespace where library_id = ? order by 1 limit 1", library);
        codeLists.update(requester, new org.oagi.score.gateway.http.api.code_list_management.controller.payload.UpdateCodeListRequest(
                cl(m), "An end-user Code List", version, "end-user-list",
                agency == null ? null : new AgencyIdListValueManifestId(n(agency)), null, null,
                new org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId(n(namespace)), false, true, List.of()));
    }

    String valueGuid(long valueManifestId) {
        return dsl.fetchOne("select v.guid from agency_id_list_value v join agency_id_list_value_manifest m on m.agency_id_list_value_id = v.agency_id_list_value_id "
                + "where m.agency_id_list_value_manifest_id = ?", valueManifestId).get(0, String.class);
    }

    Long agencyOf(long m) {
        return dsl.fetchOne("select agency_id_list_value_manifest_id from code_list_manifest where code_list_manifest_id = ?", m).get(0, Long.class);
    }

    @Test
    void endUserCodeListAgencyIdIsBroughtBackFromTheLog() {
        List<Long> agencies = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest order by 1 limit 2");
        long m = endUserCodeList(agencies.get(0));
        reviseCl(m);
        updateCl(m, agencies.get(1), "2");
        assertEquals(agencies.get(1), agencyOf(m));
        cancelCl(m);
        assertEquals(valueGuid(agencies.get(0)), valueGuid(agencyOf(m)),
                "the Agency ID is the one of the revision before (its manifest is the one of this release)");
    }

    @Test
    void endUserCodeListThatHadNoAgencyIdHasNoneAfterTheCancel() {
        List<Long> agencies = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest order by 1 limit 1");
        long m = endUserCodeList(null);
        reviseCl(m);
        updateCl(m, agencies.get(0), "2");
        cancelCl(m);
        assertEquals(null, agencyOf(m));
    }

    @Test
    void endUserCodeListAgencyIdStaysWhenTheLogHoldsNothing() {
        List<Long> agencies = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest order by 1 limit 2");
        long m = endUserCodeList(agencies.get(0));
        long stable = one("select log_id from code_list_manifest where code_list_manifest_id = ?", m);
        reviseCl(m);
        updateCl(m, agencies.get(1), "2");
        dsl.execute("update log set snapshot = '{}' where log_id = ?", stable);
        cancelCl(m);
        assertEquals(agencies.get(1), agencyOf(m), "nothing to go back to: it is as it is");
    }

    // ---- Agency ID List ----

    long agencyList() {
        release = one("select r.release_id from `release` r join library l on l.library_id = r.library_id where l.name = 'connectSpec' and r.release_num = 'Working'");
        return ids("select m.agency_id_list_manifest_id from agency_id_list_manifest m join agency_id_list a on a.agency_id_list_id = m.agency_id_list_id "
                + "where m.release_id = ? and a.state = 'Published' and a.next_agency_id_list_id is null order by 1 limit 1", release).get(0);
    }

    String guidOfAgencyList(long m) {
        return dsl.fetchOne("select a.guid from agency_id_list a join agency_id_list_manifest m on m.agency_id_list_id = a.agency_id_list_id where m.agency_id_list_manifest_id = ?", m).get(0, String.class);
    }

    AgencyIdListManifestId al(long m) {
        return new AgencyIdListManifestId(n(m));
    }

    @Test
    void agencyIdListPlain() {
        useBranch(Branch.DEVELOPER);
        long m = agencyList();
        String guid = guidOfAgencyList(m);
        String before = agencyListState(guid);
        agencyLists.revise(requester, al(m));
        agencyLists.cancel(requester, al(m), null, null);
        assertSame(before, agencyListState(guid));
    }

    @Test
    void agencyIdListValueAddedAndEdited() {
        useBranch(Branch.DEVELOPER);
        long m = agencyList();
        String guid = guidOfAgencyList(m);
        String before = agencyListState(guid);
        agencyLists.revise(requester, al(m));
        var repo = factory.agencyIdListCommandRepository(requester);
        var details = factory.agencyIdListQueryRepository(requester).getAgencyIdListDetails(al(m));
        repo.createValue(al(m), details.agencyIdListId(), details.release().releaseId(), "ZZNEW", "A new value", null, false, false, false);
        List<Long> existing = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest where agency_id_list_manifest_id = ? order by 1 limit 1", m);
        if (!existing.isEmpty()) {
            repo.updateValue(new AgencyIdListValueManifestId(n(existing.get(0))), "EDITED", "Edited", null, false, false, false);
        }
        agencyLists.cancel(requester, al(m), null, null);
        assertSame(before, agencyListState(guid));
    }

    @Test
    void codeListAgencyIdChangedInTheRevision() {
        useBranch(Branch.DEVELOPER);
        long m = codeListWithValues(2, 20);
        String guid = guidOfCodeList(m);
        String before = codeListState(guid);
        reviseCl(m);
        var details = factory.codeListQueryRepository(requester).getCodeListDetails(cl(m));
        List<Long> values = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest order by 1 limit 2");
        Long current = dsl.fetchOne("select agency_id_list_value_manifest_id from code_list_manifest where code_list_manifest_id = ?", m).get(0, Long.class);
        long other = values.get(0).equals(current) ? values.get(1) : values.get(0);
        factory.codeListCommandRepository(requester).update(cl(m), details.name(), details.versionId(), details.listId(),
                new AgencyIdListValueManifestId(n(other)), details.definition(), details.remark(), details.namespace().namespaceId(),
                details.deprecated(), details.extensible());
        cancelCl(m);
        assertSame(before, codeListState(guid));
    }

    @Test
    void codeListAgencyIdIsBroughtBack() {
        useBranch(Branch.DEVELOPER);
        release = one("select r.release_id from `release` r join library l on l.library_id = r.library_id where l.name = 'connectSpec' and r.release_num = 'Working'");
        long m = ids("select m.code_list_manifest_id from code_list_manifest m join code_list c on c.code_list_id = m.code_list_id "
                + "where m.release_id = ? and c.state = 'Published' and c.next_code_list_id is null and m.agency_id_list_value_manifest_id is not null "
                + "and m.prev_code_list_manifest_id is not null order by 1 limit 1", release).get(0);
        String guid = guidOfCodeList(m);
        String before = codeListState(guid);
        reviseCl(m);
        var details = factory.codeListQueryRepository(requester).getCodeListDetails(cl(m));
        Long current = dsl.fetchOne("select agency_id_list_value_manifest_id from code_list_manifest where code_list_manifest_id = ?", m).get(0, Long.class);
        long other = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest where agency_id_list_value_manifest_id <> ? order by 1 limit 1", current).get(0);
        factory.codeListCommandRepository(requester).update(cl(m), details.name(), details.versionId(), details.listId(),
                new AgencyIdListValueManifestId(n(other)), details.definition(), details.remark(), details.namespace().namespaceId(),
                details.deprecated(), details.extensible());
        cancelCl(m);
        assertSame(before, codeListState(guid));
    }

    @Test
    void agencyIdListOwnValueChangedInTheRevision() {
        useBranch(Branch.DEVELOPER);
        long m = agencyList();
        String guid = guidOfAgencyList(m);
        String before = agencyListState(guid);
        agencyLists.revise(requester, al(m));
        var repo = factory.agencyIdListCommandRepository(requester);
        var details = factory.agencyIdListQueryRepository(requester).getAgencyIdListDetails(al(m));
        // the list takes a value of its own as the agency identifier
        long own = ids("select agency_id_list_value_manifest_id from agency_id_list_value_manifest where agency_id_list_manifest_id = ? order by 1 limit 1", m).get(0);
        repo.update(al(m), details.name(), details.versionId(), details.listId(), new AgencyIdListValueManifestId(n(own)),
                details.definition(), details.remark(), details.namespace().namespaceId(), details.deprecated());
        agencyLists.cancel(requester, al(m), null, null);
        assertSame(before, agencyListState(guid));
    }

    // ---- a list based on a list that is revised and cancelled (as it is for a Data Type) ----

    long deriveCl(long base) {
        return codeLists.create(requester, new org.oagi.score.gateway.http.api.code_list_management.controller.payload.CreateCodeListRequest(
                new org.oagi.score.gateway.http.api.library_management.model.LibraryId(n(library)), new ReleaseId(n(release)), cl(base)))
                .value().longValue();
    }

    long count(String sql, Object... bindings) {
        return one("select count(*) from (" + sql + ") x", bindings);
    }

    @Test
    void codeListBasedOnTheRevisedListGoesBackToThePreviousRecord() {
        useBranch(Branch.DEVELOPER);
        long base = codeListWithValues(2, 20);
        String guid = guidOfCodeList(base);
        long previous = codeListRecord(base);
        reviseCl(base);
        long derived = deriveCl(base);
        assertEquals(codeListRecord(base), one("select c.based_code_list_id from code_list c join code_list_manifest m on m.code_list_id = c.code_list_id where m.code_list_manifest_id = ?", derived));

        cancelCl(base);

        assertEquals(previous, one("select c.based_code_list_id from code_list c join code_list_manifest m on m.code_list_id = c.code_list_id where m.code_list_manifest_id = ?", derived),
                "the record the list is based on is the one that is back");
        assertEquals(0L, count("select 1 from code_list_value_manifest d join code_list_value dv on dv.code_list_value_id = d.code_list_value_id "
                + "where d.code_list_manifest_id = ? and dv.based_code_list_value_id not in (select code_list_value_id from code_list_value where code_list_id = ?)", derived, previous),
                "every value is based on a value of the record that is back");
        assertEquals(0L, count("select 1 from code_list_manifest where code_list_manifest_id = ? and conflict = 1", derived));
        assertEquals(0L, count("select 1 from code_list_value_manifest where code_list_manifest_id = ? and conflict = 1", derived));
        assertEquals(0L, one("select count(*) from code_list where prev_code_list_id = ?", previous), "the record of the revision is gone");
    }

    @Test
    void codeListCannotBeCancelledWhileAListIsBasedOnAValueAddedInTheRevision() {
        useBranch(Branch.DEVELOPER);
        long base = codeListWithValues(2, 20);
        String guid = guidOfCodeList(base);
        String before = codeListState(guid);
        reviseCl(base);
        var details = factory.codeListQueryRepository(requester).getCodeListDetails(cl(base));
        factory.codeListCommandRepository(requester).createValue(cl(base), details.codeListId(), details.release().releaseId(), "ZZNEW", "A new value", null, false);
        long derived = deriveCl(base);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> cancelCl(base));

        // when the list that is based on it is gone, the cancel goes through
        factory.codeListCommandRepository(requester).delete(cl(derived));
        cancelCl(base);
        assertSame(before, codeListState(guid));
    }

    @Test
    void codeListBasedOnAListIsRevisedWithTheLatestBase() {
        useBranch(Branch.DEVELOPER);
        long base = codeListWithValues(2, 20);
        long derived = deriveCl(base);
        dsl.execute("update code_list set state = 'Published' where code_list_id = (select code_list_id from code_list_manifest where code_list_manifest_id = ?)", derived);

        reviseCl(base);
        assertEquals(1L, count("select 1 from code_list_manifest where code_list_manifest_id = ? and conflict = 1", derived),
                "the list refers to the record that was revised, which the marker says");

        reviseCl(derived);
        assertEquals(codeListRecord(base), one("select c.based_code_list_id from code_list c join code_list_manifest m on m.code_list_id = c.code_list_id where m.code_list_manifest_id = ?", derived),
                "the revision is made with the latest base");
        assertEquals(0L, count("select 1 from code_list_value_manifest d join code_list_value dv on dv.code_list_value_id = d.code_list_value_id "
                + "where d.code_list_manifest_id = ? and dv.based_code_list_value_id not in (select code_list_value_id from code_list_value where code_list_id = ?)", derived, codeListRecord(base)),
                "its values are based on the values of the latest base");
        assertEquals(0L, count("select 1 from code_list_manifest where code_list_manifest_id = ? and conflict = 1", derived));
    }

    long deriveAl(long base) {
        return agencyLists.create(requester, new org.oagi.score.gateway.http.api.agency_id_management.controller.payload.CreateAgencyIdListRequest(
                new org.oagi.score.gateway.http.api.library_management.model.LibraryId(n(library)), new ReleaseId(n(release)), al(base)))
                .value().longValue();
    }

    long agencyRecord(long m) {
        return one("select agency_id_list_id from agency_id_list_manifest where agency_id_list_manifest_id = ?", m);
    }

    @Test
    void agencyIdListBasedOnTheRevisedListGoesBackToThePreviousRecord() {
        useBranch(Branch.DEVELOPER);
        long base = agencyList();
        library = one("select library_id from `release` where release_id = ?", release);
        String guid = guidOfAgencyList(base);
        long previous = agencyRecord(base);
        agencyLists.revise(requester, al(base));
        long derived = deriveAl(base);
        assertEquals(agencyRecord(base), one("select a.based_agency_id_list_id from agency_id_list a join agency_id_list_manifest m on m.agency_id_list_id = a.agency_id_list_id where m.agency_id_list_manifest_id = ?", derived));

        agencyLists.cancel(requester, al(base), null, null);

        assertEquals(previous, one("select a.based_agency_id_list_id from agency_id_list a join agency_id_list_manifest m on m.agency_id_list_id = a.agency_id_list_id where m.agency_id_list_manifest_id = ?", derived),
                "the record the list is based on is the one that is back");
        assertEquals(0L, count("select 1 from agency_id_list_value_manifest d join agency_id_list_value dv on dv.agency_id_list_value_id = d.agency_id_list_value_id "
                + "where d.agency_id_list_manifest_id = ? and dv.based_agency_id_list_value_id not in (select agency_id_list_value_id from agency_id_list_value where owner_list_id = ?)", derived, previous),
                "every value is based on a value of the record that is back");
        assertEquals(0L, count("select 1 from agency_id_list_manifest where agency_id_list_manifest_id = ? and conflict = 1", derived));
        assertEquals(0L, one("select count(*) from agency_id_list where prev_agency_id_list_id = ?", previous), "the record of the revision is gone");
    }

    @Test
    void agencyIdListCannotBeCancelledWhileAListIsBasedOnAValueAddedInTheRevision() {
        useBranch(Branch.DEVELOPER);
        long base = agencyList();
        library = one("select library_id from `release` where release_id = ?", release);
        String guid = guidOfAgencyList(base);
        String before = agencyListState(guid);
        agencyLists.revise(requester, al(base));
        var details = factory.agencyIdListQueryRepository(requester).getAgencyIdListDetails(al(base));
        factory.agencyIdListCommandRepository(requester).createValue(al(base), details.agencyIdListId(), details.release().releaseId(), "ZZNEW", "A new value", null, false, false, false);
        long derived = deriveAl(base);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> agencyLists.cancel(requester, al(base), null, null));

        factory.agencyIdListCommandRepository(requester).delete(al(derived));
        agencyLists.cancel(requester, al(base), null, null);
        assertSame(before, agencyListState(guid));
    }

    @Test
    void agencyIdListBasedOnAListIsRevisedWithTheLatestBase() {
        useBranch(Branch.DEVELOPER);
        long base = agencyList();
        library = one("select library_id from `release` where release_id = ?", release);
        long derived = deriveAl(base);
        dsl.execute("update agency_id_list set state = 'Published' where agency_id_list_id = (select agency_id_list_id from agency_id_list_manifest where agency_id_list_manifest_id = ?)", derived);

        agencyLists.revise(requester, al(base));
        assertEquals(1L, count("select 1 from agency_id_list_manifest where agency_id_list_manifest_id = ? and conflict = 1", derived));

        agencyLists.revise(requester, al(derived));
        assertEquals(agencyRecord(base), one("select a.based_agency_id_list_id from agency_id_list a join agency_id_list_manifest m on m.agency_id_list_id = a.agency_id_list_id where m.agency_id_list_manifest_id = ?", derived),
                "the revision is made with the latest base");
        assertEquals(0L, count("select 1 from agency_id_list_value_manifest d join agency_id_list_value dv on dv.agency_id_list_value_id = d.agency_id_list_value_id "
                + "where d.agency_id_list_manifest_id = ? and dv.based_agency_id_list_value_id not in (select agency_id_list_value_id from agency_id_list_value where owner_list_id = ?)", derived, agencyRecord(base)));
        assertEquals(0L, count("select 1 from agency_id_list_manifest where agency_id_list_manifest_id = ? and conflict = 1", derived));
    }

    // ---- the log is gone: the user is asked, and a new log is written when it is let to ----

    static java.util.stream.Stream<String> lists() {
        return java.util.stream.Stream.of("code_list", "agency_id_list");
    }

    long pickList(String kind) {
        useBranch(Branch.DEVELOPER);
        return kind.equals("code_list") ? codeListWithValues(2, 20) : agencyList();
    }

    void reviseList(String kind, long m, boolean recreateLog) {
        if (kind.equals("code_list")) {
            codeLists.revise(requester, cl(m), recreateLog);
        } else {
            agencyLists.revise(requester, al(m), recreateLog);
        }
    }

    void cancelList(String kind, long m, boolean recreateLog) {
        if (kind.equals("code_list")) {
            codeLists.cancel(requester, cl(m), null, null, recreateLog);
        } else {
            agencyLists.cancel(requester, al(m), null, null, recreateLog);
        }
    }

    String guidOfList(String kind, long m) {
        return dsl.fetchOne("select r.guid from `" + kind + "` r join `" + kind + "_manifest` m on m.`" + kind + "_id` = r.`" + kind + "_id` where m.`"
                + kind + "_manifest_id` = ?", m).get(0, String.class);
    }

    long recordOfList(String kind, long m) {
        return one("select `" + kind + "_id` from `" + kind + "_manifest` where `" + kind + "_manifest_id` = ?", m);
    }

    Long logOfList(String kind, long m) {
        return dsl.fetchOne("select log_id from `" + kind + "_manifest` where `" + kind + "_manifest_id` = ?", m).get(0, Long.class);
    }

    List<Long> logsOfList(String guid) {
        return ids("select log_id from log where reference = ? order by log_id", guid);
    }

    /** What an operator does: the logs go, and what refers to them lets go first. */
    void removeLogsOfList(String kind, String guid, String where) {
        List<Long> gone = ids("select log_id from log where reference = ? and " + where, guid);
        if (gone.isEmpty()) {
            return;
        }
        String list = gone.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        dsl.execute("update `" + kind + "_manifest` set log_id = null where log_id in (" + list + ")");
        dsl.execute("update log set prev_log_id = null where prev_log_id in (" + list + ")");
        dsl.execute("update log set next_log_id = null where next_log_id in (" + list + ")");
        dsl.execute("delete from log where log_id in (" + list + ")");
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "{0}")
    @org.junit.jupiter.params.provider.MethodSource("lists")
    void cancelWithoutTheLogToGoBackToIsRefusedAndWritesNewHistoryWhenLetTo(String kind) throws Exception {
        long m = pickList(kind);
        String guid = guidOfList(kind, m);
        long record = recordOfList(kind, m);
        reviseList(kind, m, false);
        long revised = recordOfList(kind, m);
        removeLogsOfList(kind, guid, "revision_num < (select revision_num from (select revision_num from log where log_id = " + logOfList(kind, m) + ") r)");

        java.sql.Savepoint beforeCancel = connection.setSavepoint();
        org.junit.jupiter.api.Assertions.assertThrows(org.oagi.score.gateway.http.api.log_management.model.LogNotFoundException.class,
                () -> cancelList(kind, m, false));
        connection.rollback(beforeCancel);
        assertEquals(revised, recordOfList(kind, m), "the revision is still there");

        cancelList(kind, m, true);
        assertEquals(record, recordOfList(kind, m), "the list is back");
        List<Long> logs = logsOfList(guid);
        assertEquals(1, logs.size(), "one log is left");
        assertEquals(logs.get(0), logOfList(kind, m), "the manifest is in it");
        assertEquals(1L, one("select revision_num from log where log_id = ?", logs.get(0)), "revision 1");

        reviseList(kind, m, false);
        assertEquals(2L, one("select revision_num from log where log_id = ?", logOfList(kind, m)));
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "{0}")
    @org.junit.jupiter.params.provider.MethodSource("lists")
    void aListWithoutALogIsRevisedWhenLetToWriteNewHistory(String kind) throws Exception {
        long m = pickList(kind);
        String guid = guidOfList(kind, m);
        long record = recordOfList(kind, m);
        removeLogsOfList(kind, guid, "1 = 1");
        dsl.execute("update `" + kind + "_manifest` set log_id = null where `" + kind + "_manifest_id` = ?", m);

        org.junit.jupiter.api.Assertions.assertThrows(org.oagi.score.gateway.http.api.log_management.model.LogNotFoundException.class,
                () -> reviseList(kind, m, false));
        assertEquals(record, recordOfList(kind, m), "nothing is revised");
        assertEquals(0, logsOfList(guid).size(), "nothing is written");

        reviseList(kind, m, true);
        org.junit.jupiter.api.Assertions.assertNotEquals(record, recordOfList(kind, m), "the revision is made");
        assertEquals(List.of(1L, 2L), ids("select revision_num from log where reference = ? order by log_id", guid),
                "revision 1 is the list as it was, and revision 2 the revision");

        cancelList(kind, m, false);
        assertEquals(record, recordOfList(kind, m));
        assertEquals(List.of(1L), ids("select revision_num from log where reference = ?", guid));
    }
}
