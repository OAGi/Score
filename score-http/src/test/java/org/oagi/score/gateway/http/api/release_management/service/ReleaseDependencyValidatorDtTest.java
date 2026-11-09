package org.oagi.score.gateway.http.api.release_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Candidate;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.Published;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.WIP;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Error;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Warning;

/**
 * What the release validation says about the base and the allowed primitives of a Data Type: the dependency is in the
 * 'Working' release of the library, or in a release the library depends on.
 */
class ReleaseDependencyValidatorDtTest {

    private static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    /** One Data Type {@code A} (with a supplementary component {@code A.SC}) and what it depends on. */
    private static class Fixture {
        final List<DtSummaryRecord> dts = new ArrayList<>();
        final Map<DtManifestId, DtSummaryRecord> dependencyDts = new HashMap<>();
        final List<DtAwdPriSummaryRecord> awdPris = new ArrayList<>();
        final List<DtScSummaryRecord> scs = new ArrayList<>();
        final List<DtScAwdPriSummaryRecord> scAwdPris = new ArrayList<>();
        final Map<CodeListManifestId, CodeListSummaryRecord> codeLists = new HashMap<>();
        final Map<CodeListManifestId, CodeListSummaryRecord> dependencyCodeLists = new HashMap<>();
        final Map<AgencyIdListManifestId, AgencyIdListSummaryRecord> agencyIdLists = new HashMap<>();
        final Map<AgencyIdListManifestId, AgencyIdListSummaryRecord> dependencyAgencyIdLists = new HashMap<>();
        final Map<XbtManifestId, XbtSummaryRecord> xbts = new HashMap<>();
        final Set<XbtId> workingXbtIds = new HashSet<>();
        final Set<DtManifestId> assignedDts = new HashSet<>();
        final Set<CodeListManifestId> assignedCodeLists = new HashSet<>();
        final Set<AgencyIdListManifestId> assignedAgencyIdLists = new HashSet<>();
        final ReleaseValidationResponse response = new ReleaseValidationResponse();

        final DtManifestId dtA = DtManifestId.from(n(1));

        Fixture() {
            xbt(1, true);
        }

        /** An XBT manifest; {@code inWorking}: 'Working' holds the XBT. */
        XbtManifestId xbt(long id, boolean inWorking) {
            XbtManifestId manifest = XbtManifestId.from(n(id));
            xbts.put(manifest, new XbtSummaryRecord(manifest, XbtId.from(n(id)), null, null, "xbt" + id,
                    null, null, null, null, null, null, null));
            if (inWorking) {
                workingXbtIds.add(XbtId.from(n(id)));
            }
            return manifest;
        }

        DtSummaryRecord dt(long id, String den, CcState state, DtManifestId based, DtManifestId prev) {
            return new DtSummaryRecord(null, null, DtManifestId.from(n(id)), DtId.from(n(id)), null, based, den,
                    null, null, null, null, false, false, state, null, null, null, 1, null, prev, null);
        }

        /** Data Type A of the release: assigned and Candidate. */
        Fixture dtA(DtManifestId based) {
            DtSummaryRecord a = dt(1, "A. Type", Candidate, based, null);
            dts.add(a);
            assignedDts.add(a.dtManifestId());
            return this;
        }

        Fixture awdPri(XbtManifestId xbt, CodeListManifestId codeList, AgencyIdListManifestId agencyIdList,
                       boolean isDefault) {
            awdPris.add(new DtAwdPriSummaryRecord(DtAwdPriId.from(n(awdPris.size() + 1)), null, DtId.from(n(1)),
                    null, xbt, null, codeList, null, agencyIdList, null, isDefault));
            return this;
        }

        Fixture sc(CodeListManifestId codeList) {
            return sc(codeList, true);
        }

        Fixture sc(CodeListManifestId codeList, boolean isDefault) {
            scs.add(new DtScSummaryRecord(null, null, DtScManifestId.from(n(1)), DtScId.from(n(1)), null,
                    dtA, null, "A. Type. SC", null, null, null, null, false, WIP, null, null, 1, null, null, null));
            scAwdPris.add(new DtScAwdPriSummaryRecord(DtScAwdPriId.from(n(1)), null, DtScId.from(n(1)), null,
                    null, null, codeList, null, null, null, isDefault));
            return this;
        }

        CodeListSummaryRecord codeList(long id, String name, CcState state, boolean released) {
            return new CodeListSummaryRecord(CodeListManifestId.from(n(id)), CodeListId.from(n(id)), null, null, null,
                    null, name, null, null, null, null, false, state, null,
                    released ? CodeListManifestId.from(n(id + 1000)) : null, null, List.of());
        }

        AgencyIdListSummaryRecord agencyIdList(long id, String name, CcState state, boolean released) {
            return new AgencyIdListSummaryRecord(AgencyIdListManifestId.from(n(id)), AgencyIdListId.from(n(id)), null,
                    null, null, name, null, null, null, null, null, null, false, state, null,
                    released ? AgencyIdListManifestId.from(n(id + 1000)) : null, null, List.of());
        }

        Fixture workingCodeList(CodeListSummaryRecord c, boolean assigned) {
            codeLists.put(c.codeListManifestId(), c);
            if (assigned) {
                assignedCodeLists.add(c.codeListManifestId());
            }
            return this;
        }

        Fixture workingAgencyIdList(AgencyIdListSummaryRecord a, boolean assigned) {
            agencyIdLists.put(a.agencyIdListManifestId(), a);
            if (assigned) {
                assignedAgencyIdLists.add(a.agencyIdListManifestId());
            }
            return this;
        }

        Fixture validate() {
            var dependencies = new ReleaseDependencyValidator.Dependencies(Map.of(), Map.of(), Map.of(),
                    wrap(dependencyDts), wrap(dependencyCodeLists), wrap(dependencyAgencyIdLists));
            new ReleaseDependencyValidator(new ReleaseDependencyValidator.Data(List.of(), List.of(), List.of(),
                    List.of(), List.of(), dts, awdPris, scs, scAwdPris,
                    new ArrayList<>(codeLists.values()), new ArrayList<>(agencyIdLists.values()), xbts, workingXbtIds,
                    dependencies,
                    new ReleaseDependencyValidator.Assigned(List.of(), List.of(), List.of(), assignedDts,
                            assignedCodeLists, assignedAgencyIdLists)), response).validate();
            return this;
        }

        private static <I, T> Map<I, ReleaseDependencyValidator.Located<T>> wrap(Map<I, T> m) {
            Map<I, ReleaseDependencyValidator.Located<T>> r = new HashMap<>();
            m.forEach((k, v) -> r.put(k, new ReleaseDependencyValidator.Located<>(v, true)));
            return r;
        }

        List<String> messages(ValidationMessageLevel level) {
            List<String> result = new ArrayList<>();
            response.getStatusMapForDt().values().forEach(set -> set.stream()
                    .filter(m -> m.getLevel() == level).forEach(m -> result.add(m.getMessage())));
            response.getStatusMapForCodeList().values().forEach(set -> set.stream()
                    .filter(m -> m.getLevel() == level).forEach(m -> result.add(m.getMessage())));
            response.getStatusMapForAgencyIdList().values().forEach(set -> set.stream()
                    .filter(m -> m.getLevel() == level).forEach(m -> result.add(m.getMessage())));
            return result;
        }
    }

    private static final XbtManifestId XBT = XbtManifestId.from(n(1));
    private static final CodeListManifestId CL = CodeListManifestId.from(n(10));

    // --- Code List in 'Working' -------------------------------------------------------------------------------------

    @Test
    void aCodeListThatIsAssignedAndCandidateIsFine() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", Candidate, false), true)
                .awdPri(null, CL, null, true).validate();
        assertTrue(f.response.isSucceed());
        assertTrue(f.messages(Error).isEmpty());
    }

    private static CodeListSummaryRecord f0(long id, String name, CcState state, boolean released) {
        return new Fixture().codeList(id, name, state, released);
    }

    @Test
    void aCodeListThatIsPublishedIsFine() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", Published, true), false)
                .awdPri(null, CL, null, true).validate();
        assertTrue(f.messages(Error).isEmpty());
        assertTrue(f.messages(Warning).isEmpty());
    }

    @Test
    void anAssignedCodeListThatIsNotCandidateIsAnError() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", WIP, false), true)
                .awdPri(null, CL, null, true).validate();
        assertEquals(List.of("'CL' should be in 'Candidate'."), f.messages(Error));
    }

    @Test
    void aCodeListThatIsNotAssignedAndNeverReleasedIsAnErrorEvenWhenItIsTheDefault() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", Candidate, false), false)
                .awdPri(XBT, null, null, false).awdPri(null, CL, null, true).validate();
        assertFalse(f.response.isSucceed());
        assertEquals(List.of("'CL' is needed in the release assignment due to 'A. Type'."), f.messages(Error));
    }

    @Test
    void aCodeListThatIsNotAssignedAndNeverReleasedIsAnErrorWhenItIsNotTheDefault() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", Candidate, false), false)
                .awdPri(XBT, null, null, true).awdPri(null, CL, null, false).validate();
        assertEquals(List.of("'CL' is needed in the release assignment due to 'A. Type'."), f.messages(Error));
    }

    @Test
    void aCodeListThatIsNotAssignedButReleasedBeforeIsAWarning() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", WIP, true), false)
                .awdPri(null, CL, null, true).validate();
        assertTrue(f.response.isSucceed());
        assertEquals(List.of("'CL' has been revised but not included in the release assignment."),
                f.messages(Warning));
    }

    @Test
    void anAgencyIdListFollowsTheSameRule() {
        AgencyIdListManifestId ail = AgencyIdListManifestId.from(n(20));
        Fixture f = new Fixture().dtA(null);
        f.workingAgencyIdList(f.agencyIdList(20, "AIL", Candidate, false), false)
                .awdPri(null, null, ail, true).validate();
        assertEquals(List.of("'AIL' is needed in the release assignment due to 'A. Type'."), f.messages(Error));

        Fixture assigned = new Fixture().dtA(null);
        assigned.workingAgencyIdList(assigned.agencyIdList(20, "AIL", Candidate, false), true)
                .awdPri(null, null, ail, true).validate();
        assertTrue(assigned.messages(Error).isEmpty());
    }

    @Test
    void aSupplementaryComponentFollowsTheSameRule() {
        Fixture f = new Fixture().dtA(null).workingCodeList(f0(10, "CL", Candidate, false), false)
                .awdPri(XBT, null, null, true).sc(CL).validate();
        assertEquals(List.of("'CL' is needed in the release assignment due to 'A. Type. SC' of 'A. Type'."),
                f.messages(Error));
    }

    // --- Code List / Agency ID List in a release the library depends on ---------------------------------------------

    @Test
    void aCodeListOfADependencyReleaseMustBePublished() {
        Fixture published = new Fixture().dtA(null);
        published.dependencyCodeLists.put(CL, published.codeList(10, "CL", Published, true));
        published.awdPri(null, CL, null, true).validate();
        assertTrue(published.messages(Error).isEmpty());

        Fixture wip = new Fixture().dtA(null);
        wip.dependencyCodeLists.put(CL, wip.codeList(10, "CL", WIP, false));
        wip.awdPri(null, CL, null, true).validate();
        assertEquals(1, wip.messages(Error).size());
        assertTrue(wip.messages(Error).get(0).contains("'CL'"));
        assertTrue(wip.messages(Error).get(0).contains("not in 'Published'"));
    }

    @Test
    void anAssignmentDoesNotMakeACodeListOfADependencyReleaseFine() {
        Fixture f = new Fixture().dtA(null);
        f.dependencyCodeLists.put(CL, f.codeList(10, "CL", Candidate, false));
        f.assignedCodeLists.add(CL);
        f.awdPri(null, CL, null, true).validate();
        assertEquals(1, f.messages(Error).size());
    }

    @Test
    void anAgencyIdListOfADependencyReleaseMustBePublished() {
        AgencyIdListManifestId ail = AgencyIdListManifestId.from(n(20));
        Fixture f = new Fixture().dtA(null);
        f.dependencyAgencyIdLists.put(ail, f.agencyIdList(20, "AIL", WIP, false));
        f.awdPri(null, null, ail, true).validate();
        assertEquals(1, f.messages(Error).size());
    }

    @Test
    void aCodeListThatIsNowhereIsAnError() {
        Fixture f = new Fixture().dtA(null).awdPri(null, CL, null, true).validate();
        assertEquals(1, f.messages(Error).size());
        assertTrue(f.messages(Error).get(0).contains("neither 'Working' nor a release the library depends on"));
    }

    // --- XBT --------------------------------------------------------------------------------------------------------

    @Test
    void anXbtThatWorkingHoldsIsFine() {
        Fixture f = new Fixture().dtA(null).awdPri(XBT, null, null, true).validate();
        assertTrue(f.messages(Error).isEmpty());
    }

    @Test
    void anXbtOfADependencyReleaseIsFineWhenWorkingHoldsTheSameXbt() {
        // The row points to the manifest of the dependency release; the XBT record is the one 'Working' holds.
        Fixture f = new Fixture().dtA(null);
        XbtManifestId manifestOfDependency = XbtManifestId.from(n(99));
        f.xbts.put(manifestOfDependency, new XbtSummaryRecord(manifestOfDependency, XbtId.from(n(1)), null, null,
                "xbt1", null, null, null, null, null, null, null));
        f.awdPri(manifestOfDependency, null, null, true).validate();
        assertTrue(f.messages(Error).isEmpty());
    }

    @Test
    void anXbtThatWorkingDoesNotHoldIsAnError() {
        Fixture f = new Fixture().dtA(null);
        XbtManifestId custom = f.xbt(5, false);
        f.awdPri(custom, null, null, true).validate();
        assertEquals(List.of("'A. Type' uses the XBT 'xbt5', which the release does not hold."), f.messages(Error));
    }

    @Test
    void anXbtThatIsNowhereIsAnError() {
        Fixture f = new Fixture().dtA(null).awdPri(XbtManifestId.from(n(77)), null, null, true).validate();
        assertEquals(1, f.messages(Error).size());
    }

    // --- default ----------------------------------------------------------------------------------------------------

    @Test
    void aDataTypeMustHaveExactlyOneDefault() {
        Fixture none = new Fixture().dtA(null).awdPri(XBT, null, null, false).validate();
        assertEquals(List.of("'A. Type' must have exactly one default value constraint."), none.messages(Error));

        Fixture two = new Fixture().dtA(null).awdPri(XBT, null, null, true).awdPri(XBT, null, null, true).validate();
        assertEquals(1, two.messages(Error).size());
    }

    @Test
    void aSupplementaryComponentMustHaveExactlyOneDefault() {
        Fixture f = new Fixture().dtA(null).awdPri(XBT, null, null, true);
        f.sc(null, false).validate();
        assertEquals(List.of("'A. Type. SC' of 'A. Type' must have exactly one default value constraint."),
                f.messages(Error));
    }

    // --- base Data Type ---------------------------------------------------------------------------------------------

    @Test
    void aBaseThatIsNotAssignedAndNeverReleasedIsAnError() {
        Fixture f = new Fixture();
        DtSummaryRecord base = f.dt(2, "Base. Type", Candidate, null, null);
        f.dts.add(base);
        f.dtA(base.dtManifestId()).awdPri(XBT, null, null, true).validate();
        assertEquals(List.of("'Base. Type' is needed in the release assignment due to 'A. Type'."), f.messages(Error));
    }

    @Test
    void aBaseThatIsNotAssignedButReleasedBeforeIsAWarning() {
        Fixture f = new Fixture();
        DtSummaryRecord base = f.dt(2, "Base. Type", WIP, null, DtManifestId.from(n(1002)));
        f.dts.add(base);
        f.dtA(base.dtManifestId()).awdPri(XBT, null, null, true).validate();
        assertTrue(f.messages(Error).isEmpty());
        assertEquals(1, f.messages(Warning).size());
    }

    @Test
    void anAssignedBaseMustBeCandidate() {
        Fixture f = new Fixture();
        DtSummaryRecord base = f.dt(2, "Base. Type", WIP, null, null);
        f.dts.add(base);
        f.assignedDts.add(base.dtManifestId());
        f.dtA(base.dtManifestId()).awdPri(XBT, null, null, true).validate();
        assertTrue(f.messages(Error).contains("'Base. Type' should be in 'Candidate'."));
    }

    @Test
    void aBaseOfADependencyReleaseMustBePublished() {
        Fixture published = new Fixture();
        DtSummaryRecord catalogue = published.dt(50, "Code. Type", Published, null, null);
        published.dependencyDts.put(catalogue.dtManifestId(), catalogue);
        published.dtA(catalogue.dtManifestId()).awdPri(XBT, null, null, true).validate();
        assertTrue(published.messages(Error).isEmpty());

        Fixture wip = new Fixture();
        DtSummaryRecord notPublished = wip.dt(50, "Code. Type", Candidate, null, null);
        wip.dependencyDts.put(notPublished.dtManifestId(), notPublished);
        wip.dtA(notPublished.dtManifestId()).awdPri(XBT, null, null, true).validate();
        assertEquals(1, wip.messages(Error).size());
        assertTrue(wip.messages(Error).get(0).contains("'Code. Type'"));
    }

    @Test
    void aBaseThatIsNowhereIsAnError() {
        Fixture f = new Fixture().dtA(DtManifestId.from(n(404))).awdPri(XBT, null, null, true).validate();
        assertEquals(1, f.messages(Error).size());
    }

    // --- scope ------------------------------------------------------------------------------------------------------

    @Test
    void aDataTypeThatIsNeitherPublishedNorAssignedIsNotChecked() {
        Fixture f = new Fixture();
        f.dts.add(f.dt(1, "A. Type", WIP, null, null));
        f.awdPri(null, CL, null, true).validate();
        assertTrue(f.messages(Error).isEmpty());
        assertTrue(f.response.isSucceed());
    }
}
