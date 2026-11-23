package org.oagi.score.gateway.http.api.release_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageCode;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageCode.*;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Error;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Warning;

/**
 * Checks what the components of the release depend on:
 * <ul>
 *   <li>an ACC: its base ACC, and through its ASCCs and BCCs the ASCCPs and BCCPs it has;</li>
 *   <li>an ASCCP: the ACC it is the role of;</li>
 *   <li>a BCCP: its Data Type;</li>
 *   <li>a Data Type: its base, and what its allowed primitives (and those of its supplementary components) use: an XBT,
 *   a Code List or an Agency ID List.</li>
 * </ul>
 * The release holds the components that are assigned to it, and the ones of earlier releases it keeps. So a dependency
 * that is in the 'Working' release of the library is judged this way:
 * <ul>
 *   <li>a {@code Published} one is fine;</li>
 *   <li>an assigned one must be {@code Candidate};</li>
 *   <li>one that is not assigned is an error when it has not been released before, since the release would not hold it,
 *   and a warning when it has, since the release then keeps the one of the earlier release.</li>
 * </ul>
 * A dependency that is in a release the library depends on is referenced as it is, so it must be {@code Published}, and
 * it is a warning when that release is itself still in work (its 'Working' release, or one not yet published), since the
 * other library can still revise the component. An XBT has no state: it is fine when the release holds it, that is when
 * 'Working' has it.
 */
final class ReleaseDependencyValidator {

    /** A component of a release the library depends on; {@code stable}: that release is published, so it will not change. */
    record Located<T>(T component, boolean stable) {
    }

    /** The components of the releases the library depends on, which the components of the library can refer to. */
    record Dependencies(
            Map<AccManifestId, Located<AccSummaryRecord>> accs,
            Map<AsccpManifestId, Located<AsccpSummaryRecord>> asccps,
            Map<BccpManifestId, Located<BccpSummaryRecord>> bccps,
            Map<DtManifestId, Located<DtSummaryRecord>> dts,
            Map<CodeListManifestId, Located<CodeListSummaryRecord>> codeLists,
            Map<AgencyIdListManifestId, Located<AgencyIdListSummaryRecord>> agencyIdLists) {

        static Dependencies none() {
            return new Dependencies(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
    }

    /** What is assigned to the release. */
    record Assigned(
            Collection<AccManifestId> accs,
            Collection<AsccpManifestId> asccps,
            Collection<BccpManifestId> bccps,
            Collection<DtManifestId> dts,
            Collection<CodeListManifestId> codeLists,
            Collection<AgencyIdListManifestId> agencyIdLists) {
    }

    /** What the validation reads: the 'Working' release of the library, and the releases it depends on. */
    record Data(
            List<AccSummaryRecord> accs,
            List<AsccSummaryRecord> asccs,
            List<BccSummaryRecord> bccs,
            List<AsccpSummaryRecord> asccps,
            List<BccpSummaryRecord> bccps,
            List<DtSummaryRecord> dts,
            List<DtAwdPriSummaryRecord> awdPris,
            List<DtScSummaryRecord> scs,
            List<DtScAwdPriSummaryRecord> scAwdPris,
            List<CodeListSummaryRecord> codeLists,
            List<AgencyIdListSummaryRecord> agencyIdLists,
            Map<XbtManifestId, XbtSummaryRecord> xbts,
            Set<XbtId> workingXbtIds,
            Map<AgencyIdListValueManifestId, AgencyIdListManifestId> agencyValueOwners,
            Dependencies dependencies,
            Assigned assigned) {
    }

    /** What is depended on, as the rules see it. */
    private record Target(String den, CcState state, boolean inWorking, boolean assigned, boolean released,
                          boolean stable, BiConsumer<ValidationMessageLevel, String> onDependency) {
    }

    private final Data data;
    private final ReleaseValidationResponse response;
    private final Map<AccManifestId, AccSummaryRecord> accMap;
    private final Map<AsccpManifestId, AsccpSummaryRecord> asccpMap;
    private final Map<BccpManifestId, BccpSummaryRecord> bccpMap;
    private final Map<DtManifestId, DtSummaryRecord> dtMap;
    private final Map<CodeListManifestId, CodeListSummaryRecord> codeListMap;
    private final Map<AgencyIdListManifestId, AgencyIdListSummaryRecord> agencyIdListMap;
    private final Map<DtId, List<DtAwdPriSummaryRecord>> awdPrisByDt;
    private final Map<DtScId, List<DtScAwdPriSummaryRecord>> scAwdPrisBySc;

    ReleaseDependencyValidator(Data data, ReleaseValidationResponse response) {
        this.data = data;
        this.response = response;
        this.accMap = data.accs().stream().collect(Collectors.toMap(AccSummaryRecord::accManifestId, Function.identity()));
        this.asccpMap = data.asccps().stream().collect(Collectors.toMap(AsccpSummaryRecord::asccpManifestId, Function.identity()));
        this.bccpMap = data.bccps().stream().collect(Collectors.toMap(BccpSummaryRecord::bccpManifestId, Function.identity()));
        this.dtMap = data.dts().stream().collect(Collectors.toMap(DtSummaryRecord::dtManifestId, Function.identity()));
        this.codeListMap = data.codeLists().stream().collect(Collectors.toMap(CodeListSummaryRecord::codeListManifestId, Function.identity()));
        this.agencyIdListMap = data.agencyIdLists().stream().collect(Collectors.toMap(AgencyIdListSummaryRecord::agencyIdListManifestId, Function.identity()));
        this.awdPrisByDt = data.awdPris().stream().collect(Collectors.groupingBy(DtAwdPriSummaryRecord::dtId));
        this.scAwdPrisBySc = data.scAwdPris().stream().collect(Collectors.groupingBy(DtScAwdPriSummaryRecord::dtScId));
    }

    private static boolean inRelease(CcState state, boolean assigned) {
        return state == CcState.Published || assigned;
    }

    void validate() {
        validateAccs();
        validateAsccps();
        validateBccps();
        validateDts();
        validateCodeLists();
        validateAgencyIdLists();
    }

    // --- ACC, ASCCP, BCCP -------------------------------------------------------------------------------------------

    private void validateAccs() {
        Map<AccManifestId, List<AsccSummaryRecord>> asccsByAcc = data.asccs().stream()
                .collect(Collectors.groupingBy(AsccSummaryRecord::fromAccManifestId));
        Map<AccManifestId, List<BccSummaryRecord>> bccsByAcc = data.bccs().stream()
                .collect(Collectors.groupingBy(BccSummaryRecord::fromAccManifestId));

        for (AccSummaryRecord acc : data.accs()) {
            if (!inRelease(acc.state(), data.assigned().accs().contains(acc.accManifestId()))) {
                continue;
            }
            String dependent = "'" + acc.den() + "'";
            if (acc.basedAccManifestId() != null) {
                dependOnAcc(dependent, acc.basedAccManifestId(), ACC_BasedACC,
                        (level, message) -> response.addMessageForAcc(acc.accManifestId(), level, message, ACC_BasedACC),
                        "The base ACC of " + dependent + " is in neither 'Working' nor a release the library depends on.");
            }
            for (AsccSummaryRecord ascc : asccsByAcc.getOrDefault(acc.accManifestId(), List.of())) {
                dependOnAsccp(dependent, ascc.toAsccpManifestId(), ACC_Association,
                        (level, message) -> response.addMessageForAcc(acc.accManifestId(), level, message, ACC_Association),
                        dependent + " has an ASCC to an ASCCP that is in neither 'Working' nor a release the library depends on.");
            }
            for (BccSummaryRecord bcc : bccsByAcc.getOrDefault(acc.accManifestId(), List.of())) {
                dependOnBccp(dependent, bcc.toBccpManifestId(), ACC_Association,
                        (level, message) -> response.addMessageForAcc(acc.accManifestId(), level, message, ACC_Association),
                        dependent + " has a BCC to a BCCP that is in neither 'Working' nor a release the library depends on.");
            }
        }
    }

    private void validateAsccps() {
        for (AsccpSummaryRecord asccp : data.asccps()) {
            if (!inRelease(asccp.state(), data.assigned().asccps().contains(asccp.asccpManifestId()))) {
                continue;
            }
            String dependent = "'" + asccp.den() + "'";
            dependOnAcc(dependent, asccp.roleOfAccManifestId(), ASCCP_RoleOfAcc,
                    (level, message) -> response.addMessageForAsccp(asccp.asccpManifestId(), level, message, ASCCP_RoleOfAcc),
                    "The ACC " + dependent + " is the role of is in neither 'Working' nor a release the library depends on.");
        }
    }

    private void validateBccps() {
        for (BccpSummaryRecord bccp : data.bccps()) {
            if (!inRelease(bccp.state(), data.assigned().bccps().contains(bccp.bccpManifestId()))) {
                continue;
            }
            String dependent = "'" + bccp.den() + "'";
            dependOnDt(dependent, bccp.dtManifestId(), BCCP_BDT,
                    (level, message) -> response.addMessageForBccp(bccp.bccpManifestId(), level, message, BCCP_BDT),
                    "The Data Type of " + dependent + " is in neither 'Working' nor a release the library depends on.");
        }
    }

    // --- Code List, Agency ID List ----------------------------------------------------------------------------------

    private void validateCodeLists() {
        for (CodeListSummaryRecord codeList : data.codeLists()) {
            if (!inRelease(codeList.state(), data.assigned().codeLists().contains(codeList.codeListManifestId()))) {
                continue;
            }
            String dependent = "'" + codeList.name() + "'";
            if (codeList.basedCodeListManifestId() != null) {
                dependOnCodeList(dependent, codeList.basedCodeListManifestId(), CODE_LIST_BasedCodeList,
                        (level, message) -> response.addMessageForCodeList(
                                codeList.codeListManifestId(), level, message, CODE_LIST_BasedCodeList));
            }
            dependOnAgencyOfValue(dependent, codeList.agencyIdListValueManifestId(), null, CODE_LIST_AgencyIdList,
                    (level, message) -> response.addMessageForCodeList(
                            codeList.codeListManifestId(), level, message, CODE_LIST_AgencyIdList));
        }
    }

    private void validateAgencyIdLists() {
        for (AgencyIdListSummaryRecord agencyIdList : data.agencyIdLists()) {
            if (!inRelease(agencyIdList.state(),
                    data.assigned().agencyIdLists().contains(agencyIdList.agencyIdListManifestId()))) {
                continue;
            }
            String dependent = "'" + agencyIdList.name() + "'";
            if (agencyIdList.basedAgencyIdListManifestId() != null) {
                dependOnAgencyIdList(dependent, agencyIdList.basedAgencyIdListManifestId(), AGENCY_ID_LIST_BasedAgencyIdList,
                        (level, message) -> response.addMessageForAgencyIdList(
                                agencyIdList.agencyIdListManifestId(), level, message, AGENCY_ID_LIST_BasedAgencyIdList));
            }
            dependOnAgencyOfValue(dependent, agencyIdList.agencyIdListValueManifestId(),
                    agencyIdList.agencyIdListManifestId(), AGENCY_ID_LIST_AgencyIdList,
                    (level, message) -> response.addMessageForAgencyIdList(
                            agencyIdList.agencyIdListManifestId(), level, message, AGENCY_ID_LIST_AgencyIdList));
        }
    }

    /** The agency of a Code List or an Agency ID List is a value of an Agency ID List; that list is what is depended on. */
    private void dependOnAgencyOfValue(String dependent, AgencyIdListValueManifestId value, AgencyIdListManifestId self,
                                       ValidationMessageCode code, BiConsumer<ValidationMessageLevel, String> onDependent) {
        if (value == null) {
            return;
        }
        AgencyIdListManifestId owner = data.agencyValueOwners().get(value);
        if (owner == null) {
            onDependent.accept(Error, dependent + " uses an agency that is in neither 'Working' nor a release the library depends on.");
        } else if (!owner.equals(self)) {
            dependOnAgencyIdList(dependent, owner, code, onDependent);
        }
    }

    // --- DT ---------------------------------------------------------------------------------------------------------

    private void validateDts() {
        Map<DtManifestId, List<DtScSummaryRecord>> scsByDt = data.scs().stream()
                .collect(Collectors.groupingBy(DtScSummaryRecord::ownerDtManifestId));

        for (DtSummaryRecord dt : data.dts()) {
            if (!inRelease(dt.state(), data.assigned().dts().contains(dt.dtManifestId()))) {
                continue;
            }
            BiConsumer<ValidationMessageLevel, String> onDt =
                    (level, message) -> response.addMessageForDt(dt.dtManifestId(), level, message, DT_BasedDT);
            if (dt.basedDtManifestId() != null) {
                dependOnDt("'" + dt.den() + "'", dt.basedDtManifestId(), DT_BasedDT, onDt,
                        "The base Data Type of '" + dt.den() + "' is in neither 'Working' nor a release the library depends on.");
            }
            validateAllowedPrimitives(dt, "'" + dt.den() + "'", awdPrisByDt.getOrDefault(dt.dtId(), List.of())
                    .stream().map(Row::of).toList());
            for (DtScSummaryRecord sc : scsByDt.getOrDefault(dt.dtManifestId(), List.of())) {
                validateAllowedPrimitives(dt, "'" + sc.den() + "' of '" + dt.den() + "'",
                        scAwdPrisBySc.getOrDefault(sc.dtScId(), List.of()).stream().map(Row::of).toList());
            }
        }
    }

    /** An allowed primitive, whether it is of a Data Type or of a supplementary component. */
    private record Row(XbtManifestId xbt, CodeListManifestId codeList, AgencyIdListManifestId agencyIdList,
                       boolean isDefault) {
        static Row of(DtAwdPriSummaryRecord r) {
            return new Row(r.xbtManifestId(), r.codeListManifestId(), r.agencyIdListManifestId(), r.isDefault());
        }

        static Row of(DtScAwdPriSummaryRecord r) {
            return new Row(r.xbtManifestId(), r.codeListManifestId(), r.agencyIdListManifestId(), r.isDefault());
        }
    }

    private void validateAllowedPrimitives(DtSummaryRecord dt, String owner, List<Row> rows) {
        BiConsumer<ValidationMessageLevel, String> onDt =
                (level, message) -> response.addMessageForDt(dt.dtManifestId(), level, message, DT_AllowedPrimitive);

        if (rows.stream().filter(Row::isDefault).count() != 1) {
            onDt.accept(Error, owner + " must have exactly one default value constraint.");
        }

        for (Row row : rows) {
            if (row.xbt() != null) {
                validateXbt(owner, row.xbt(), onDt);
            }
            if (row.codeList() != null) {
                dependOnCodeList(owner, row.codeList(), DT_AllowedPrimitive, onDt);
            }
            if (row.agencyIdList() != null) {
                dependOnAgencyIdList(owner, row.agencyIdList(), DT_AllowedPrimitive, onDt);
            }
        }
    }

    private void validateXbt(String owner, XbtManifestId xbtManifestId,
                             BiConsumer<ValidationMessageLevel, String> onDt) {
        XbtSummaryRecord xbt = data.xbts().get(xbtManifestId);
        if (xbt == null) {
            onDt.accept(Error, owner + " uses an XBT that is in neither 'Working' nor a release the library depends on.");
        } else if (!data.workingXbtIds().contains(xbt.xbtId())) {
            onDt.accept(Error, owner + " uses the XBT '" + xbt.name() + "', which the release does not hold.");
        }
    }

    // --- the dependency rules ---------------------------------------------------------------------------------------

    private void dependOnAcc(String dependent, AccManifestId id, ValidationMessageCode code,
                             BiConsumer<ValidationMessageLevel, String> onDependent, String missing) {
        AccSummaryRecord working = accMap.get(id);
        if (working != null) {
            judge(dependent, new Target(working.den(), working.state(), true, data.assigned().accs().contains(id),
                    working.prevAccManifestId() != null, true,
                    (level, message) -> response.addMessageForAcc(id, level, message, code)), onDependent);
            return;
        }
        var dependency = data.dependencies().accs().get(id);
        if (dependency != null) {
            judge(dependent, new Target(dependency.component().den(), dependency.component().state(), false, false,
                    false, dependency.stable(), null), onDependent);
        } else {
            onDependent.accept(Error, missing);
        }
    }

    private void dependOnAsccp(String dependent, AsccpManifestId id, ValidationMessageCode code,
                               BiConsumer<ValidationMessageLevel, String> onDependent, String missing) {
        AsccpSummaryRecord working = asccpMap.get(id);
        if (working != null) {
            judge(dependent, new Target(working.den(), working.state(), true, data.assigned().asccps().contains(id),
                    working.prevAsccpManifestId() != null, true,
                    (level, message) -> response.addMessageForAsccp(id, level, message, code)), onDependent);
            return;
        }
        var dependency = data.dependencies().asccps().get(id);
        if (dependency != null) {
            judge(dependent, new Target(dependency.component().den(), dependency.component().state(), false, false,
                    false, dependency.stable(), null), onDependent);
        } else {
            onDependent.accept(Error, missing);
        }
    }

    private void dependOnBccp(String dependent, BccpManifestId id, ValidationMessageCode code,
                              BiConsumer<ValidationMessageLevel, String> onDependent, String missing) {
        BccpSummaryRecord working = bccpMap.get(id);
        if (working != null) {
            judge(dependent, new Target(working.den(), working.state(), true, data.assigned().bccps().contains(id),
                    working.prevBccpManifestId() != null, true,
                    (level, message) -> response.addMessageForBccp(id, level, message, code)), onDependent);
            return;
        }
        var dependency = data.dependencies().bccps().get(id);
        if (dependency != null) {
            judge(dependent, new Target(dependency.component().den(), dependency.component().state(), false, false,
                    false, dependency.stable(), null), onDependent);
        } else {
            onDependent.accept(Error, missing);
        }
    }

    private void dependOnDt(String dependent, DtManifestId id, ValidationMessageCode code,
                            BiConsumer<ValidationMessageLevel, String> onDependent, String missing) {
        DtSummaryRecord working = dtMap.get(id);
        if (working != null) {
            judge(dependent, new Target(working.den(), working.state(), true, data.assigned().dts().contains(id),
                    working.prevDtManifestId() != null, true,
                    (level, message) -> response.addMessageForDt(id, level, message, code)), onDependent);
            return;
        }
        var dependency = data.dependencies().dts().get(id);
        if (dependency != null) {
            judge(dependent, new Target(dependency.component().den(), dependency.component().state(), false, false,
                    false, dependency.stable(), null), onDependent);
        } else {
            onDependent.accept(Error, missing);
        }
    }

    private void dependOnCodeList(String dependent, CodeListManifestId id, ValidationMessageCode code,
                                  BiConsumer<ValidationMessageLevel, String> onDependent) {
        CodeListSummaryRecord working = codeListMap.get(id);
        if (working != null) {
            judge(dependent, new Target(working.name(), working.state(), true, data.assigned().codeLists().contains(id),
                    working.prevCodeListManifestId() != null, true,
                    (level, message) -> response.addMessageForCodeList(id, level, message, code)), onDependent);
            return;
        }
        var dependency = data.dependencies().codeLists().get(id);
        if (dependency != null) {
            judge(dependent, new Target(dependency.component().name(), dependency.component().state(), false, false,
                    false, dependency.stable(), null), onDependent);
        } else {
            onDependent.accept(Error, dependent + " uses a Code List that is in neither 'Working' nor a release the library depends on.");
        }
    }

    private void dependOnAgencyIdList(String dependent, AgencyIdListManifestId id, ValidationMessageCode code,
                                      BiConsumer<ValidationMessageLevel, String> onDependent) {
        AgencyIdListSummaryRecord working = agencyIdListMap.get(id);
        if (working != null) {
            judge(dependent, new Target(working.name(), working.state(), true, data.assigned().agencyIdLists().contains(id),
                    working.prevAgencyIdListManifestId() != null, true,
                    (level, message) -> response.addMessageForAgencyIdList(id, level, message, code)), onDependent);
            return;
        }
        var dependency = data.dependencies().agencyIdLists().get(id);
        if (dependency != null) {
            judge(dependent, new Target(dependency.component().name(), dependency.component().state(), false, false,
                    false, dependency.stable(), null), onDependent);
        } else {
            onDependent.accept(Error, dependent + " uses an Agency ID List that is in neither 'Working' nor a release the library depends on.");
        }
    }

    /**
     * In 'Working' the message is shown on the dependency, as for the other components; in a release the library depends
     * on, it is shown on the dependent component, since the dependency is not the library's to change.
     */
    private static void judge(String dependent, Target target, BiConsumer<ValidationMessageLevel, String> onDependent) {
        CcState state = target.state();
        if (target.inWorking()) {
            if (state == CcState.Published) {
                return;
            }
            if (target.assigned()) {
                if (state != CcState.Candidate) {
                    target.onDependency().accept(Error, "'" + target.den() + "' should be in '" + CcState.Candidate + "'.");
                }
            } else if (!target.released()) {
                target.onDependency().accept(Error,
                        "'" + target.den() + "' is needed in the release assignment due to " + dependent + ".");
            } else {
                target.onDependency().accept(Warning,
                        "'" + target.den() + "' has been revised but not included in the release assignment.");
            }
            return;
        }
        if (state != CcState.Published) {
            onDependent.accept(Error, dependent + " uses '" + target.den() + "' of a release the library depends on, "
                    + "which is in '" + state + "' state, not in '" + CcState.Published + "'.");
        } else if (!target.stable()) {
            onDependent.accept(Warning, dependent + " uses '" + target.den() + "' of a release the library depends on "
                    + "that is still in work, so the component may be revised.");
        }
    }
}
