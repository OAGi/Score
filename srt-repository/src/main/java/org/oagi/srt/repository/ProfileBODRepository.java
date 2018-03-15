package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ProfileBOD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ProfileBODRepository {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_STATEMENT =
            "SELECT new ProfileBOD( tla.topLevelAbieId, abie.abieId, abie.version, abie.status, asbiep.asbiepId, asccp.asccpId, tla.releaseId, " +
                                   "asccp.propertyTerm, bc.bizCtxId, bc.name as bizCtxName, r.releaseNum, tla.state, " +
                                   "tla.ownerUserId, u.loginId as ownerName, abie.creationTimestamp ) " +
            "FROM TopLevelAbie tla, AggregateBusinessInformationEntity abie, AssociationBusinessInformationEntityProperty asbiep, " +
                 "AssociationCoreComponentProperty asccp, BusinessContext bc, User u, Release r " +
            "WHERE tla.abie.abieId = abie.abieId AND abie.abieId = asbiep.roleOfAbieId AND asbiep.basedAsccpId = asccp.asccpId" +
             " AND abie.bizCtxId = bc.bizCtxId AND tla.ownerUserId = u.appUserId AND (tla.releaseId = r.releaseId OR tla.releaseId IS NULL)";

    public List<ProfileBOD> findAll() {
        Query query = entityManager.createQuery(FIND_ALL_STATEMENT, ProfileBOD.class);
        return query.getResultList();
    }

    public ProfileBOD findOne(long topLevelAbieId) {
        Query query = entityManager.createQuery(FIND_ALL_STATEMENT + " AND tla.topLevelAbieId = ?", ProfileBOD.class);
        query.setParameter(1, topLevelAbieId);
        return (ProfileBOD) query.getSingleResult();
    }
}
