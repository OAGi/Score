package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

import java.util.Collection;

public interface BiePackageCommandRepository {

    BiePackageId create(LibraryId libraryId, String name, String versionId, String versionName, String description);

    BiePackageId revise(BiePackageId biePackageId, String versionId);

    boolean update(BiePackageId biePackageId, String name, String versionId, String versionName, String description, String revisionReason);

    boolean updateState(BiePackageId biePackageId, BieState state);

    boolean updateOwnerUserId(BiePackageId biePackageId, UserId userId);

    BiePackageId copy(BiePackageId biePackageId);

    int delete(Collection<BiePackageId> biePackageIdList);

    void deleteAssignedTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    void addBieToBiePackage(BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    void replaceBieInBiePackage(BiePackageId biePackageId, TopLevelAsbiepId prevTopLevelAsbiepId, TopLevelAsbiepId topLevelAsbiepId);

    void deleteBieInBiePackage(BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

}
