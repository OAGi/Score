package org.oagi.score.service.common.data;

import org.oagi.score.repo.api.bie.model.BiePackageState;
import org.oagi.score.repo.api.bie.model.BieState;

import java.math.BigInteger;

public enum AccessPrivilege {

    CanEdit,
    CanView,
    CanMove,
    Prohibited,
    Unprepared;

    public static AccessPrivilege toAccessPrivilege(AppUser requester, AppUser owner, CcState ccState, boolean isWorkingRelease) {
        AccessPrivilege accessPrivilege = Prohibited;
        switch (ccState) {
            case Deleted:
            case Production:
                if (isWorkingRelease) {
                    if (requester.isDeveloper()) {
                        accessPrivilege = CanMove;
                    } else {
                        accessPrivilege = CanView;
                    }
                } else {
                    if (requester.isDeveloper()) {
                        accessPrivilege = CanView;
                    } else {
                        accessPrivilege = CanMove;
                    }
                }
                break;
            case Published:
                if (isWorkingRelease) {
                    if (requester.isDeveloper()) {
                        accessPrivilege = CanMove;
                    } else {
                        accessPrivilege = CanView;
                    }
                } else {
                    accessPrivilege = CanView;
                }
                break;
            case WIP:
                if (requester.getAppUserId().equals(owner.getAppUserId())) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;
            case Draft:
            case QA:
            case Candidate:
                if (requester.getAppUserId().equals(owner.getAppUserId())) {
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

    public static AccessPrivilege toAccessPrivilege(AppUser requester, AppUser owner, String ccState, boolean isWorkingRelease) {
        return toAccessPrivilege(requester, owner, CcState.valueOf(ccState), isWorkingRelease);
    }

    public static AccessPrivilege toAccessPrivilege(AppUser requester, BigInteger bieOwnerId, BieState bieState) {
        AccessPrivilege accessPrivilege = Prohibited;
        switch (bieState) {
            case Initiating:
                accessPrivilege = Unprepared;
                break;
            case WIP:
                if (requester.getAppUserId().equals(bieOwnerId)) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;
            case QA:
                if (requester.getAppUserId().equals(bieOwnerId)) {
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

    public static AccessPrivilege toAccessPrivilege(AppUser requester, BigInteger biePackageOwnerId, BiePackageState biePackageState) {
        AccessPrivilege accessPrivilege = Prohibited;
        switch (biePackageState) {
            case WIP:
                if (requester.getAppUserId().equals(biePackageOwnerId)) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;
            case QA:
                if (requester.getAppUserId().equals(biePackageOwnerId)) {
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
