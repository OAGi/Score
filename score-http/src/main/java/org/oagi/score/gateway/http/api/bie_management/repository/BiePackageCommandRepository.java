package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

import java.util.Collection;

public interface BiePackageCommandRepository {

    BiePackageId create(LibraryId libraryId, String versionId, String versionName, String description);

    boolean update(BiePackageId biePackageId, String versionId, String versionName, String description);

    boolean updateState(BiePackageId biePackageId, BieState state);

    boolean updateOwnerUserId(BiePackageId biePackageId, UserId userId);

    BiePackageId copy(BiePackageId biePackageId);

    int delete(Collection<BiePackageId> biePackageIdList);

    void deleteAssignedTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    void addBieToBiePackage(BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    void deleteBieInBiePackage(BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

}
