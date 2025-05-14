package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.DiscardBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateBiePackageRequest;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

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

    public BiePackageId create(ScoreUser requester, CreateBiePackageRequest request) {

        return command(requester).create(
                request.libraryId(),
                hasLength(request.versionId()) ? request.versionId() : "v1.0",
                hasLength(request.versionName()) ? request.versionName() : "New BIE Package",
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

            return command(requester).update(
                    request.biePackageId(),
                    request.versionId(),
                    request.versionName(),
                    request.description());
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

    public void addBieToBiePackage(
            ScoreUser requester, BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        BiePackageDetailsRecord biePackage = ensureBiePackageIsUpdatable(requester, biePackageId, true);

        command(requester).addBieToBiePackage(biePackage.biePackageId(), topLevelAsbiepIdList);
    }

    public void deleteBieInBiePackage(
            ScoreUser requester, BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {

        BiePackageDetailsRecord biePackage = ensureBiePackageIsUpdatable(requester, biePackageId, true);

        command(requester).deleteBieInBiePackage(biePackage.biePackageId(), topLevelAsbiepIdList);
    }
}
