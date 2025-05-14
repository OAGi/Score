package org.oagi.score.gateway.http.common.model;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.util.Objects;

import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;

public enum AccessPrivilege {

    CanEdit,
    CanView,
    CanMove,
    Prohibited,
    Unprepared;

    public static AccessPrivilege toAccessPrivilege(ScoreUser requester, UserId ownerId, CcState ccState, boolean isWorkingRelease) {
        AccessPrivilege accessPrivilege = Prohibited;
        switch (ccState) {
            case Deleted:
            case Production:
                if (isWorkingRelease) {
                    if (requester.hasRole(DEVELOPER)) {
                        accessPrivilege = CanMove;
                    } else {
                        accessPrivilege = CanView;
                    }
                } else {
                    if (requester.hasRole(DEVELOPER)) {
                        accessPrivilege = CanView;
                    } else {
                        accessPrivilege = CanMove;
                    }
                }
                break;
            case Published:
                if (isWorkingRelease) {
                    if (requester.hasRole(DEVELOPER)) {
                        accessPrivilege = CanMove;
                    } else {
                        accessPrivilege = CanView;
                    }
                } else {
                    accessPrivilege = CanView;
                }
                break;
            case WIP:
                if (Objects.equals(requester.userId(), ownerId)) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;
            case Draft:
            case QA:
            case Candidate:
                if (Objects.equals(requester.userId(), ownerId)) {
                    accessPrivilege = CanMove;
                } else {
                    accessPrivilege = CanView;
                }
                break;
            case ReleaseDraft:
                accessPrivilege = CanView;
                break;
        }
        return accessPrivilege;
    }

    public static AccessPrivilege toAccessPrivilege(ScoreUser requester, UserId bieOwnerId, BieState bieState) {
        AccessPrivilege accessPrivilege = Prohibited;
        switch (bieState) {
            case Initiating:
                accessPrivilege = Unprepared;
                break;
            case WIP:
                if (requester.userId().equals(bieOwnerId)) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;
            case QA:
                if (requester.userId().equals(bieOwnerId)) {
                    accessPrivilege = CanMove;
                } else {
                    accessPrivilege = CanView;
                }
                break;
            case Production:
                accessPrivilege = CanView;
                break;
        }
        return accessPrivilege;
    }
}
