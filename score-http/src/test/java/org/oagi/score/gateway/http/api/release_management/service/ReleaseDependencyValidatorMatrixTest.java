package org.oagi.score.gateway.http.api.release_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
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
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessage;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Error;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Warning;

/**
 * The release validation against every combination of what a component depends on, for every kind of dependency:
 * ASCCP → ACC, ASCC → ASCCP, BCC → BCCP, BCCP → DT, ACC → base ACC, DT → base DT, DT → Code List, DT → Agency ID List,
 * and the same for the supplementary components of a DT. For each of them, the combinations of
 * <ul>
 *   <li>the state of the dependent component and whether it is assigned to the release;</li>
 *   <li>where the dependency is: in 'Working' of the library, in a published release of another library, in a release of
 *   another library that is still in work, or nowhere;</li>
 *   <li>the state of the dependency, and, in 'Working', whether it is assigned and whether it has been released before.</li>
 * </ul>
 * The expectation is written once, as a table ({@link #expected}), not derived from the validator.
 */
class ReleaseDependencyValidatorMatrixTest {

    private static final EnumSet<CcState> STATES = EnumSet.complementOf(EnumSet.of(CcState.Deleted));

    enum Location {
        /** In 'Working' of the library. */
        WORKING,
        /** In a published release of another library, which the library depends on. */
        PUBLISHED_RELEASE,
        /** In the release of another library that is still in work (its 'Working' release). */
        RELEASE_IN_WORK,
        /** In neither. */
        NOWHERE
    }

    record Scenario(CcState dependentState, boolean dependentAssigned, Location location, CcState state,
                    boolean assigned, boolean released) {
        boolean dependentInRelease() {
            return dependentState == CcState.Published || dependentAssigned;
        }
    }

    /** Where a message is shown: on the component that depends, or on the one that is depended on. */
    record Outcome(Set<String> messages) {
    }

    private static final long DEPENDENT = 1;
    private static final long DEPENDENCY = 2;
    private static final long FILLER = 99;

    private static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    // --- the expectation --------------------------------------------------------------------------------------------

    /** What the rules say for the scenario: {@code "<level>@dependent"}, {@code "<level>@dependency"} or nothing. */
    static Set<String> expected(Scenario s) {
        if (!s.dependentInRelease()) {
            return Set.of();
        }
        switch (s.location()) {
            case WORKING:
                if (s.state() == CcState.Published) {
                    return Set.of();
                }
                if (s.assigned()) {
                    return s.state() == CcState.Candidate ? Set.of() : Set.of("Error@dependency");
                }
                return s.released() ? Set.of("Warning@dependency") : Set.of("Error@dependency");
            case PUBLISHED_RELEASE:
                return s.state() == CcState.Published ? Set.of() : Set.of("Error@dependent");
            case RELEASE_IN_WORK:
                return s.state() == CcState.Published ? Set.of("Warning@dependent") : Set.of("Error@dependent");
            default:
                return Set.of("Error@dependent");
        }
    }

    static List<Scenario> scenarios() {
        List<Scenario> result = new ArrayList<>();
        for (CcState dependentState : STATES) {
            for (boolean dependentAssigned : new boolean[]{false, true}) {
                for (Location location : Location.values()) {
                    if (location == Location.NOWHERE) {
                        result.add(new Scenario(dependentState, dependentAssigned, location, CcState.WIP, false, false));
                        continue;
                    }
                    for (CcState state : STATES) {
                        if (location != Location.WORKING) {
                            result.add(new Scenario(dependentState, dependentAssigned, location, state, false, false));
                            continue;
                        }
                        for (boolean assigned : new boolean[]{false, true}) {
                            for (boolean released : new boolean[]{false, true}) {
                                result.add(new Scenario(dependentState, dependentAssigned, location, state, assigned, released));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    // --- the world the validation reads -----------------------------------------------------------------------------

    private static final class World {
        final List<AccSummaryRecord> accs = new ArrayList<>();
        final List<AsccSummaryRecord> asccs = new ArrayList<>();
        final List<BccSummaryRecord> bccs = new ArrayList<>();
        final List<AsccpSummaryRecord> asccps = new ArrayList<>();
        final List<BccpSummaryRecord> bccps = new ArrayList<>();
        final List<DtSummaryRecord> dts = new ArrayList<>();
        final List<DtAwdPriSummaryRecord> awdPris = new ArrayList<>();
        final List<DtScSummaryRecord> scs = new ArrayList<>();
        final List<DtScAwdPriSummaryRecord> scAwdPris = new ArrayList<>();
        final List<CodeListSummaryRecord> codeLists = new ArrayList<>();
        final List<AgencyIdListSummaryRecord> agencyIdLists = new ArrayList<>();
        final Map<XbtManifestId, XbtSummaryRecord> xbts = new HashMap<>();
        final Set<XbtId> workingXbtIds = new HashSet<>();

        final Map<AccManifestId, ReleaseDependencyValidator.Located<AccSummaryRecord>> depAccs = new HashMap<>();
        final Map<AsccpManifestId, ReleaseDependencyValidator.Located<AsccpSummaryRecord>> depAsccps = new HashMap<>();
        final Map<BccpManifestId, ReleaseDependencyValidator.Located<BccpSummaryRecord>> depBccps = new HashMap<>();
        final Map<DtManifestId, ReleaseDependencyValidator.Located<DtSummaryRecord>> depDts = new HashMap<>();
        final Map<CodeListManifestId, ReleaseDependencyValidator.Located<CodeListSummaryRecord>> depCodeLists = new HashMap<>();
        final Map<AgencyIdListManifestId, ReleaseDependencyValidator.Located<AgencyIdListSummaryRecord>> depAgencyIdLists = new HashMap<>();

        final Set<AccManifestId> assignedAccs = new HashSet<>();
        final Set<AsccpManifestId> assignedAsccps = new HashSet<>();
        final Set<BccpManifestId> assignedBccps = new HashSet<>();
        final Set<DtManifestId> assignedDts = new HashSet<>();
        final Set<CodeListManifestId> assignedCodeLists = new HashSet<>();
        final Set<AgencyIdListManifestId> assignedAgencyIdLists = new HashSet<>();

        final ReleaseValidationResponse response = new ReleaseValidationResponse();
        final Scenario scenario;

        World(Scenario scenario) {
            this.scenario = scenario;
            XbtManifestId xbt = XbtManifestId.from(n(1));
            xbts.put(xbt, new XbtSummaryRecord(xbt, XbtId.from(n(1)), null, null, "xbt", null, null, null, null, null,
                    null, null));
            workingXbtIds.add(XbtId.from(n(1)));
        }

        boolean inWorking() {
            return scenario.location() == Location.WORKING;
        }

        boolean stable() {
            return scenario.location() == Location.PUBLISHED_RELEASE;
        }

        boolean inDependencyRelease() {
            return scenario.location() == Location.PUBLISHED_RELEASE || scenario.location() == Location.RELEASE_IN_WORK;
        }

        <T> ReleaseDependencyValidator.Located<T> located(T t) {
            return new ReleaseDependencyValidator.Located<>(t, stable());
        }

        // records

        AccSummaryRecord acc(long id, CcState state, AccManifestId based, boolean released) {
            return new AccSummaryRecord(null, null, AccManifestId.from(n(id)), AccId.from(n(id)), null, null, based,
                    "acc" + id, null, null, null, false, false, state, null, null, 1, null,
                    released ? AccManifestId.from(n(id + 1000)) : null, null);
        }

        AsccpSummaryRecord asccp(long id, CcState state, AccManifestId roleOf, boolean released) {
            return new AsccpSummaryRecord(null, null, AsccpManifestId.from(n(id)), AsccpId.from(n(id)), null, null,
                    roleOf, "asccp" + id, null, false, false, false, state, null, null, 1, null,
                    released ? AsccpManifestId.from(n(id + 1000)) : null, null);
        }

        BccpSummaryRecord bccp(long id, CcState state, DtManifestId dt, boolean released) {
            return new BccpSummaryRecord(null, null, BccpManifestId.from(n(id)), BccpId.from(n(id)), null, dt,
                    "bccp" + id, null, null, false, false, state, null, null, null, 1, null,
                    released ? BccpManifestId.from(n(id + 1000)) : null, null);
        }

        AsccSummaryRecord ascc(AccManifestId from, AsccpManifestId to) {
            return new AsccSummaryRecord(null, null, AsccManifestId.from(n(1)), AsccId.from(n(1)), null, from, to,
                    null, null, null, "ascc", null, false, CcState.WIP, null, 1, null, null, null);
        }

        BccSummaryRecord bcc(AccManifestId from, BccpManifestId to) {
            return new BccSummaryRecord(null, null, BccManifestId.from(n(1)), BccId.from(n(1)), null, from, to,
                    null, null, null, null, "bcc", null, false, false, CcState.WIP, null, null, 1, null, null, null);
        }

        DtSummaryRecord dt(long id, CcState state, DtManifestId based, boolean released) {
            return new DtSummaryRecord(null, null, DtManifestId.from(n(id)), DtId.from(n(id)), null, based, "dt" + id,
                    null, null, null, null, false, false, state, null, null, null, 1, null,
                    released ? DtManifestId.from(n(id + 1000)) : null, null);
        }

        CodeListSummaryRecord codeList(long id, CcState state, boolean released) {
            return new CodeListSummaryRecord(CodeListManifestId.from(n(id)), CodeListId.from(n(id)), null, null, null,
                    null, "cl" + id, null, null, null, null, false, state, null,
                    released ? CodeListManifestId.from(n(id + 1000)) : null, null, List.of());
        }

        AgencyIdListSummaryRecord agencyIdList(long id, CcState state, boolean released) {
            return new AgencyIdListSummaryRecord(AgencyIdListManifestId.from(n(id)), AgencyIdListId.from(n(id)), null,
                    null, null, "ail" + id, null, null, null, null, null, null, false, state, null,
                    released ? AgencyIdListManifestId.from(n(id + 1000)) : null, null, List.of());
        }

        // the Data Type that has a valid default of its own: it is there to be depended on, without being the problem

        void validDt(long id, CcState state) {
            dts.add(dt(id, state, null, false));
            allowedXbt(id);
        }

        void allowedXbt(long dtId) {
            awdPris.add(new DtAwdPriSummaryRecord(DtAwdPriId.from(n(awdPris.size() + 1)), null, DtId.from(n(dtId)),
                    null, XbtManifestId.from(n(1)), null, null, null, null, null, true));
        }

        /** A supplementary component of the Data Type that depends on, with a default of its own that uses the dependency. */
        void sc(CodeListManifestId codeList, AgencyIdListManifestId agencyIdList) {
            scs.add(new DtScSummaryRecord(null, null, DtScManifestId.from(n(1)), DtScId.from(n(1)), null,
                    DtManifestId.from(n(DEPENDENT)), null, "dt1.sc", null, null, null, null, false, CcState.WIP, null,
                    null, 1, null, null, null));
            scAwdPris.add(new DtScAwdPriSummaryRecord(DtScAwdPriId.from(n(1)), null, DtScId.from(n(1)), null,
                    null, null, codeList, null, agencyIdList, null, true));
        }

        // the dependent component, always in 'Working'

        void dependentAcc(AccManifestId based) {
            accs.add(acc(DEPENDENT, scenario.dependentState(), based, false));
            if (scenario.dependentAssigned()) {
                assignedAccs.add(AccManifestId.from(n(DEPENDENT)));
            }
        }

        void dependentAsccp(AccManifestId roleOf) {
            asccps.add(asccp(DEPENDENT, scenario.dependentState(), roleOf, false));
            if (scenario.dependentAssigned()) {
                assignedAsccps.add(AsccpManifestId.from(n(DEPENDENT)));
            }
        }

        void dependentBccp(DtManifestId dt) {
            bccps.add(bccp(DEPENDENT, scenario.dependentState(), dt, false));
            if (scenario.dependentAssigned()) {
                assignedBccps.add(BccpManifestId.from(n(DEPENDENT)));
            }
        }

        void dependentDt(DtManifestId based) {
            dts.add(dt(DEPENDENT, scenario.dependentState(), based, false));
            if (scenario.dependentAssigned()) {
                assignedDts.add(DtManifestId.from(n(DEPENDENT)));
            }
        }

        // the dependency: where it is depends on the scenario

        AccManifestId placeAcc() {
            AccSummaryRecord r = acc(DEPENDENCY, scenario.state(), null, scenario.released());
            if (inWorking()) {
                accs.add(r);
                if (scenario.assigned()) {
                    assignedAccs.add(r.accManifestId());
                }
            } else if (inDependencyRelease()) {
                depAccs.put(r.accManifestId(), located(r));
            }
            return r.accManifestId();
        }

        AsccpManifestId placeAsccp() {
            AsccpSummaryRecord r = asccp(DEPENDENCY, scenario.state(), AccManifestId.from(n(FILLER)), scenario.released());
            if (inWorking()) {
                asccps.add(r);
                if (scenario.assigned()) {
                    assignedAsccps.add(r.asccpManifestId());
                }
            } else if (inDependencyRelease()) {
                depAsccps.put(r.asccpManifestId(), located(r));
            }
            return r.asccpManifestId();
        }

        BccpManifestId placeBccp() {
            BccpSummaryRecord r = bccp(DEPENDENCY, scenario.state(), DtManifestId.from(n(FILLER)), scenario.released());
            if (inWorking()) {
                bccps.add(r);
                if (scenario.assigned()) {
                    assignedBccps.add(r.bccpManifestId());
                }
            } else if (inDependencyRelease()) {
                depBccps.put(r.bccpManifestId(), located(r));
            }
            return r.bccpManifestId();
        }

        DtManifestId placeDt() {
            DtSummaryRecord r = dt(DEPENDENCY, scenario.state(), null, scenario.released());
            if (inWorking()) {
                dts.add(r);
                allowedXbt(DEPENDENCY);
                if (scenario.assigned()) {
                    assignedDts.add(r.dtManifestId());
                }
            } else if (inDependencyRelease()) {
                depDts.put(r.dtManifestId(), located(r));
            }
            return r.dtManifestId();
        }

        CodeListManifestId placeCodeList() {
            CodeListSummaryRecord r = codeList(DEPENDENCY, scenario.state(), scenario.released());
            if (inWorking()) {
                codeLists.add(r);
                if (scenario.assigned()) {
                    assignedCodeLists.add(r.codeListManifestId());
                }
            } else if (inDependencyRelease()) {
                depCodeLists.put(r.codeListManifestId(), located(r));
            }
            return r.codeListManifestId();
        }

        AgencyIdListManifestId placeAgencyIdList() {
            AgencyIdListSummaryRecord r = agencyIdList(DEPENDENCY, scenario.state(), scenario.released());
            if (inWorking()) {
                agencyIdLists.add(r);
                if (scenario.assigned()) {
                    assignedAgencyIdLists.add(r.agencyIdListManifestId());
                }
            } else if (inDependencyRelease()) {
                depAgencyIdLists.put(r.agencyIdListManifestId(), located(r));
            }
            return r.agencyIdListManifestId();
        }

        void validate() {
            new ReleaseDependencyValidator(new ReleaseDependencyValidator.Data(accs, asccs, bccs, asccps, bccps, dts,
                    awdPris, scs, scAwdPris, codeLists, agencyIdLists, xbts, workingXbtIds,
                    new ReleaseDependencyValidator.Dependencies(depAccs, depAsccps, depBccps, depDts, depCodeLists,
                            depAgencyIdLists),
                    new ReleaseDependencyValidator.Assigned(assignedAccs, assignedAsccps, assignedBccps, assignedDts,
                            assignedCodeLists, assignedAgencyIdLists)), response).validate();
        }
    }

    // --- the dependencies ---------------------------------------------------------------------------------------------

    /** One kind of dependency: builds the world for a scenario, and tells where the validation put its messages. */
    record Edge(String name, Function<World, Set<String>> run) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static Set<String> tag(Set<ValidationMessage> messages, String where) {
        Set<String> result = new HashSet<>();
        if (messages != null) {
            messages.forEach(m -> result.add(m.getLevel() + "@" + where));
        }
        return result;
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    static Stream<Edge> edges() {
        long dependentId = DEPENDENT;
        long dependencyId = DEPENDENCY;
        return Stream.of(
                new Edge("ACC -> base ACC", w -> {
                    AccManifestId base = w.placeAcc();
                    w.dependentAcc(base);
                    w.validate();
                    return union(tag(w.response.getStatusMapForAcc().get(AccManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForAcc().get(AccManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("ACC -> ASCCP (by ASCC)", w -> {
                    w.accs.add(w.acc(FILLER, CcState.Published, null, false));
                    AsccpManifestId to = w.placeAsccp();
                    w.dependentAcc(null);
                    w.asccs.add(w.ascc(AccManifestId.from(n(dependentId)), to));
                    w.validate();
                    return union(tag(w.response.getStatusMapForAcc().get(AccManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForAsccp().get(AsccpManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("ACC -> BCCP (by BCC)", w -> {
                    w.validDt(FILLER, CcState.Published);
                    BccpManifestId to = w.placeBccp();
                    w.dependentAcc(null);
                    w.bccs.add(w.bcc(AccManifestId.from(n(dependentId)), to));
                    w.validate();
                    return union(tag(w.response.getStatusMapForAcc().get(AccManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForBccp().get(BccpManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("ASCCP -> ACC (role of)", w -> {
                    AccManifestId role = w.placeAcc();
                    w.dependentAsccp(role);
                    w.validate();
                    return union(tag(w.response.getStatusMapForAsccp().get(AsccpManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForAcc().get(AccManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("BCCP -> DT", w -> {
                    DtManifestId dt = w.placeDt();
                    w.dependentBccp(dt);
                    w.validate();
                    return union(tag(w.response.getStatusMapForBccp().get(BccpManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("DT -> base DT", w -> {
                    DtManifestId base = w.placeDt();
                    w.dependentDt(base);
                    w.allowedXbt(DEPENDENT);
                    w.validate();
                    return union(tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("DT -> Code List", w -> {
                    CodeListManifestId codeList = w.placeCodeList();
                    w.dependentDt(null);
                    w.awdPris.add(new DtAwdPriSummaryRecord(DtAwdPriId.from(n(1)), null, DtId.from(n(dependentId)),
                            null, null, null, codeList, null, null, null, true));
                    w.validate();
                    return union(tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForCodeList().get(CodeListManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("DT -> Agency ID List", w -> {
                    AgencyIdListManifestId agencyIdList = w.placeAgencyIdList();
                    w.dependentDt(null);
                    w.awdPris.add(new DtAwdPriSummaryRecord(DtAwdPriId.from(n(1)), null, DtId.from(n(dependentId)),
                            null, null, null, null, null, agencyIdList, null, true));
                    w.validate();
                    return union(tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForAgencyIdList().get(AgencyIdListManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("DT_SC -> Code List", w -> {
                    CodeListManifestId codeList = w.placeCodeList();
                    w.dependentDt(null);
                    w.allowedXbt(DEPENDENT);
                    w.sc(codeList, null);
                    w.validate();
                    return union(tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForCodeList().get(CodeListManifestId.from(n(dependencyId))), "dependency"));
                }),
                new Edge("DT_SC -> Agency ID List", w -> {
                    AgencyIdListManifestId agencyIdList = w.placeAgencyIdList();
                    w.dependentDt(null);
                    w.allowedXbt(DEPENDENT);
                    w.sc(null, agencyIdList);
                    w.validate();
                    return union(tag(w.response.getStatusMapForDt().get(DtManifestId.from(n(dependentId))), "dependent"),
                            tag(w.response.getStatusMapForAgencyIdList().get(AgencyIdListManifestId.from(n(dependencyId))), "dependency"));
                })
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("edges")
    void everyCombinationOfStateAndPlaceGivesWhatTheRulesSay(Edge edge) {
        List<String> mismatches = new ArrayList<>();
        int count = 0;
        for (Scenario s : scenarios()) {
            count++;
            Set<String> actual = edge.run().apply(new World(s));
            Set<String> expected = expected(s);
            if (!expected.equals(actual)) {
                mismatches.add(s + " expected " + expected + " but was " + actual);
            }
        }
        assertEquals(List.of(), mismatches.stream().limit(20).toList(),
                mismatches.size() + " of " + count + " combinations differ");
        assertEquals(602, count, "the combinations that are enumerated");
    }

    @Test
    void theRulesAsTheyAreStatedForRepresentativeCombinations() {
        // dependent assigned; dependency in 'Working'
        assertEquals(Set.of(), expected(new Scenario(CcState.Candidate, true, Location.WORKING, CcState.Published, false, false)));
        assertEquals(Set.of(), expected(new Scenario(CcState.Candidate, true, Location.WORKING, CcState.Candidate, true, false)));
        assertEquals(Set.of("Error@dependency"), expected(new Scenario(CcState.Candidate, true, Location.WORKING, CcState.WIP, true, true)));
        assertEquals(Set.of("Error@dependency"), expected(new Scenario(CcState.Candidate, true, Location.WORKING, CcState.WIP, false, false)));
        assertEquals(Set.of("Warning@dependency"), expected(new Scenario(CcState.Candidate, true, Location.WORKING, CcState.WIP, false, true)));
        // the dependent is not in the release: nothing is said
        assertEquals(Set.of(), expected(new Scenario(CcState.WIP, false, Location.NOWHERE, CcState.WIP, false, false)));
        // a published release of another library: the component must be Published; one in work can still change
        assertEquals(Set.of(), expected(new Scenario(CcState.Candidate, true, Location.PUBLISHED_RELEASE, CcState.Published, false, false)));
        assertEquals(Set.of("Error@dependent"), expected(new Scenario(CcState.Candidate, true, Location.PUBLISHED_RELEASE, CcState.Candidate, false, false)));
        assertEquals(Set.of("Warning@dependent"), expected(new Scenario(CcState.Candidate, true, Location.RELEASE_IN_WORK, CcState.Published, false, false)));
        assertEquals(Set.of("Error@dependent"), expected(new Scenario(CcState.Candidate, true, Location.RELEASE_IN_WORK, CcState.WIP, false, false)));
    }
}
