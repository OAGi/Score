package org.oagi.score.gateway.http.api.bie_management.service;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.DiscardBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.model.BieState.Production;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.WIP;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

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
                hasLength(request.name()) ? request.name() : "New BIE Package",
                hasLength(request.versionId()) ? request.versionId() : "v1.0",
                hasLength(request.versionName()) ? request.versionName() : newVersionName(),
                request.description());
    }

    public boolean update(ScoreUser requester, UpdateBiePackageRequest request) {

        UserId requesterId = requester.userId();
        if (request.state() != null) {
            ensureBiePackageIsUpdatable(requester, request.biePackageId(), false);

            return command(requester).updateState(
                    request.biePackageId(), request.state());
        } else {
            ensureBiePackageIsUpdatable(requester, request.biePackageId(), true);

            try {
                return command(requester).update(
                        request.biePackageId(),
                        request.name(),
                        request.versionId(),
                        request.versionName(),
                        request.description(),
                        request.revisionReason());
            } catch (DuplicateKeyException e) {
                throw new IllegalArgumentException("A BIE package with the same name, version ID, and version name already exists.", e);
            }
        }
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

        if (!biePackage.owner().userId().equals(requester.userId())) {
            throw new DataAccessForbiddenException("Only allowed to update the BIE package by the owner.");
        }

        return biePackage;
    }

    public void copy(ScoreUser requester, Collection<BiePackageId> biePackageIdList) {
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

        command(requester).replaceBieInBiePackage(biePackage.biePackageId(), prevTopLevelAsbiepId, topLevelAsbiepId);
    }

    public void deleteBieInBiePackage(
            ScoreUser requester, BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        BiePackageDetailsRecord biePackage = ensureBiePackageIsUpdatable(requester, biePackageId, true);

        command(requester).deleteBieInBiePackage(biePackage.biePackageId(), topLevelAsbiepIdList);
    }

}
