package org.oagi.score.gateway.http.api.bie_management.service;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CopyBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.DiscardBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BieReplacementRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.SemanticVersion;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.model.BieState.Production;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.WIP;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.hasText;

@Service
@Transactional
public class BiePackageCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BiePackageCommandRepository command(ScoreUser requester) {
        return repositoryFactory.biePackageCommandRepository(requester);
    }

    private BiePackageQueryRepository query(ScoreUser requester) {
        return repositoryFactory.biePackageQueryRepository(requester);
    }

    @Autowired
    private SessionService sessionService;

    private String newVersionName() {
        return RandomStringGenerator.builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build()
                .generate(8);
    }

    public BiePackageId create(ScoreUser requester, CreateBiePackageRequest request) {

        return command(requester).create(
                request.libraryId(),
                hasText(request.name()) ? request.name().trim() : "New BIE Package",
                hasText(request.versionId()) ? request.versionId().trim() : "v1.0",
                hasText(request.versionName()) ? request.versionName().trim() : newVersionName(),
                request.description());
    }

    public boolean update(ScoreUser requester, UpdateBiePackageRequest request) {

        UserId requesterId = requester.userId();
        if (request.state() != null) {
            BiePackageDetailsRecord biePackage =
                    ensureBiePackageIsUpdatable(requester, request.biePackageId(), false);
            if (!canMove(biePackage.state(), request.state())) {
                throw new IllegalArgumentException("The BIE package in '" + biePackage.state() +
                        "' state cannot move to '" + request.state() + "' state.");
            }

            return command(requester).updateState(
                    request.biePackageId(), request.state());
        } else {
            ensureBiePackageIsUpdatable(requester, request.biePackageId(), true);

            if (!hasText(request.name())) {
                throw new IllegalArgumentException("The BIE package name cannot be empty.");
            }
            if (!hasText(request.versionId())) {
                throw new IllegalArgumentException("The BIE package version ID cannot be empty.");
            }
            if (!hasText(request.versionName())) {
                throw new IllegalArgumentException("The BIE package version name cannot be empty.");
            }

            try {
                return command(requester).update(
                        request.biePackageId(),
                        request.name().trim(),
                        request.versionId().trim(),
                        request.versionName().trim(),
                        request.description(),
                        request.revisionReason());
            } catch (DuplicateKeyException e) {
                throw new IllegalArgumentException("A BIE package with the same name, version ID, and version name already exists.", e);
            }
        }
    }

    /** The moves the BIE Package page offers: WIP to QA, QA to WIP and QA to Production. */
    private boolean canMove(BieState from, BieState to) {
        if (from == WIP) {
            return to == BieState.QA;
        }
        if (from == BieState.QA) {
            return to == WIP || to == BieState.Production;
        }
        return false;
    }

    private BiePackageDetailsRecord ensureBiePackageIsUpdatable(
            ScoreUser requester, BiePackageId biePackageId, boolean stateCheck) {

        BiePackageDetailsRecord biePackage = query(requester).getBiePackageDetails(biePackageId);
        if (biePackage == null) {
            throw new IllegalArgumentException("No BIE Package with ID " + biePackageId);
        }

        if (stateCheck && (WIP != biePackage.state())) {
            throw new DataAccessForbiddenException("Not allowed to update the BIE package in '" + biePackage.state() + "' state.");
        }

        // An administrator can update a BIE package they do not own, as for a BIE (Issue #1576).
        if (!requester.isAdministrator() && !biePackage.owner().userId().equals(requester.userId())) {
            throw new DataAccessForbiddenException("Only allowed to update the BIE package by the owner.");
        }

        return biePackage;
    }

    /**
     * Copies BIE packages by the same rule as copying a BIE: a package can be copied when it is in QA or
     * Production, or when the requester owns it. Another user's WIP package is not. Every package is
     * checked before any is copied.
     */
    public void copy(ScoreUser requester, CopyBiePackageRequest request) {
        Collection<BiePackageId> biePackageIdList = request.biePackageIdList();
        for (BiePackageId biePackageId : biePackageIdList) {
            BiePackageSummaryRecord source = query(requester).getBiePackageSummary(biePackageId);
            if (source == null) {
                throw new NotFoundException("The BIE package to copy does not exist.");
            }
            boolean copyable = source.state() == BieState.QA || source.state() == Production
                    || requester.userId().equals(source.owner().userId());
            if (!copyable) {
                throw new DataAccessForbiddenException(
                        "Not allowed to copy the BIE package in '" + source.state() + "' state owned by another user.");
            }
        }
        for (BiePackageId biePackageId : biePackageIdList) {
            command(requester).copy(biePackageId);
        }
    }

    public int discard(ScoreUser requester, DiscardBiePackageRequest request) {
        Collection<BiePackageId> biePackageIdList = request.biePackageIdList();
        if (biePackageIdList == null || biePackageIdList.isEmpty()) {
            return 0;
        }

        ensureProperDeleteBiePackageRequest(requester, biePackageIdList);

        return command(requester).delete(biePackageIdList);
    }

    private void ensureProperDeleteBiePackageRequest(ScoreUser requester, Collection<BiePackageId> biePackageIdList) {
        UserId requesterId = requester.userId();
        List<BiePackageSummaryRecord> biePackages = query(requester).getBiePackageSummaryList(biePackageIdList);

        // Issue #1576
        // Administrator can discard BIE packages in any state.
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            for (BiePackageSummaryRecord biePackage : biePackages) {
                BieState state = biePackage.state();
                if (state == BieState.Production) {
                    throw new DataAccessForbiddenException("Not allowed to delete the BIE package in '" + state + "' state.");
                }

                if (!biePackage.owner().userId().equals(requesterId)) {
                    throw new DataAccessForbiddenException("Only allowed to delete the BIE package by the owner.");
                }
            }
        }

        // Referential integrity (applies to every user, including administrators):
        // A revision (prev_bie_package_id) is a real dependency — a BIE package referenced as the previous revision
        // by another package outside this request must not be discarded on its own. The referencing package has to be
        // discarded together (selected) or beforehand. (Copy/uplift provenance via source_bie_package_id is NOT a
        // dependency: breaking it is harmless, so it is simply detached during the delete rather than blocked here.)
        List<BiePackageSummaryRecord> externalReferencers =
                query(requester).getBiePackagesReferencingAsPrevious(biePackageIdList);
        if (!externalReferencers.isEmpty()) {
            String referencers = externalReferencers.stream()
                    .map(ref -> "'" + label(ref) + "'")
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Cannot discard the selected BIE package(s) because the following BIE package(s) are not included " +
                            "but reference them as their previous revision: " + referencers + ". " +
                            "Please discard these BIE package(s) as well, or include them in the selection.");
        }
    }

    private static String label(BiePackageSummaryRecord biePackage) {
        String name = hasLength(biePackage.name()) ? biePackage.name() : "BIE Package";
        return hasLength(biePackage.versionId()) ? name + " " + biePackage.versionId() : name;
    }

    public boolean transferOwnership(ScoreUser requester, BiePackageId biePackageId, ScoreUser targetUser) {

        // Issue #1576
        // Even if the administrator does not own BIE, they can transfer ownership.
        BiePackageSummaryRecord biePackage = query(requester).getBiePackageSummary(biePackageId);
        if (!requester.isAdministrator()) {
            if (WIP != biePackage.state()) {
                throw new IllegalArgumentException("Only the BIE package in 'WIP' state can be modified.");
            }

            if (!biePackage.owner().userId().equals(requester.userId())) {
                throw new IllegalArgumentException("It only allows to modify the BIE package by the owner.");
            }
        }

        if (biePackage.owner().userId().equals(targetUser.userId())) {
            throw new IllegalArgumentException("You already own this BIE package.");
        }

        return command(requester).updateOwnerUserId(
                biePackage.biePackageId(), targetUser.userId());
    }

    public BiePackageId revise(ScoreUser requester, BiePackageId biePackageId) {

        var query = query(requester);
        BiePackageSummaryRecord biePackage = query.getBiePackageSummary(biePackageId);
        if (biePackage == null) {
            throw new IllegalArgumentException("No BIE Package with ID " + biePackageId);
        }
        if (Production != biePackage.state()) {
            throw new IllegalArgumentException("Only the BIE package in 'Production' state can be revised.");
        }
        if (!query.getBiePackagesReferencingAsPrevious(List.of(biePackageId)).isEmpty()) {
            throw new IllegalArgumentException("This BIE package already has a newer revision.");
        }

        String versionId = biePackage.versionId();
        while (true) {
            versionId = newVersionId(versionId);
            if (!query.hasDuplicateVersion(biePackageId, versionId)) {
                break;
            }
        }

        return command(requester).revise(biePackageId, versionId);
    }

    private String newVersionId(String versionId) {
        try {
            SemanticVersion semver = new SemanticVersion(versionId);
            return semver.increment(false).toString();
        } catch (IllegalArgumentException e) {
            return addNumber(versionId);
        }
    }

    private String addNumber(String versionId) {
        Pattern pattern = Pattern.compile("^(.*?)(?:-(\\d+))?$");
        Matcher matcher = pattern.matcher(versionId);
        if (matcher.matches()) {
            String base = matcher.group(1);
            String numStr = matcher.group(2);
            int next = (numStr != null) ? Integer.parseInt(numStr) + 1 : 1;
            return base + "-" + next;
        }
        return versionId + "-1"; // fallback
    }

    public void addBieToBiePackage(
            ScoreUser requester, BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        BiePackageDetailsRecord biePackage = ensureBiePackageIsUpdatable(requester, biePackageId, true);

        command(requester).addBieToBiePackage(biePackage.biePackageId(), topLevelAsbiepIdList);
    }

    public void replaceBieInBiePackage(
            ScoreUser requester, BiePackageId biePackageId,
            TopLevelAsbiepId prevTopLevelAsbiepId, TopLevelAsbiepId topLevelAsbiepId) {

        BiePackageDetailsRecord biePackage = ensureBiePackageIsUpdatable(requester, biePackageId, true);

        if (prevTopLevelAsbiepId != null && topLevelAsbiepId != null &&
            isInReplacementChain(topLevelAsbiepId, prevTopLevelAsbiepId,
                    query(requester).getBieReplacementListInBiePackage(biePackage.biePackageId()))) {
            throw new IllegalArgumentException(
                    "The BIE to replace with is already in the replacement chain of the replaced BIE.");
        }

        command(requester).replaceBieInBiePackage(biePackage.biePackageId(), prevTopLevelAsbiepId, topLevelAsbiepId);
    }

    /**
     * Whether the BIE is the given replaced BIE or one that the replaced BIE has replaced (directly or through
     * a chain). Replacing with such a BIE would make {@code prev_top_level_asbiep_id} cyclic.
     */
    static boolean isInReplacementChain(TopLevelAsbiepId topLevelAsbiepId, TopLevelAsbiepId replacedTopLevelAsbiepId,
                                        List<BieReplacementRecord> replacements) {
        // replacing BIE -> the BIE it replaces
        Map<TopLevelAsbiepId, TopLevelAsbiepId> replacedByReplacing = replacements.stream()
                .collect(Collectors.toMap(
                        BieReplacementRecord::topLevelAsbiepId, BieReplacementRecord::prevTopLevelAsbiepId));
        Set<TopLevelAsbiepId> visited = new HashSet<>();
        for (TopLevelAsbiepId id = replacedTopLevelAsbiepId; id != null && visited.add(id);
             id = replacedByReplacing.get(id)) {
            if (id.equals(topLevelAsbiepId)) {
                return true;
            }
        }
        return false;
    }

    public void deleteBieInBiePackage(
            ScoreUser requester, BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        BiePackageDetailsRecord biePackage = ensureBiePackageIsUpdatable(requester, biePackageId, true);

        command(requester).deleteBieInBiePackage(biePackage.biePackageId(), topLevelAsbiepIdList);
    }

}
