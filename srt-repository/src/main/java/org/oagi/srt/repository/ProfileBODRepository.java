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
            "SELECT tla.top_level_abie_id, abie.abie_id, tla.state, tla.owner_user_id, u.login_id as owner_name, tla.release_id, " +
            "abie.creation_timestamp, abie.version, abie.status, asbiep.asbiep_id, " +
            "asccp.asccp_id, asccp.property_term, " +
            "bc.biz_ctx_id, bc.name as biz_ctx_name, release.release_num " +
            "FROM top_level_abie tla JOIN abie ON tla.abie_id = abie.abie_id " +
                                    "JOIN asbiep ON abie.abie_id = asbiep.role_of_abie_id " +
                                    "JOIN asccp ON asbiep.based_asccp_id = asccp.asccp_id " +
                                    "JOIN biz_ctx bc ON abie.biz_ctx_id = bc.biz_ctx_id " +
                                    "JOIN app_user u ON tla.owner_user_id = u.app_user_id " +
                                    "LEFT JOIN release ON tla.release_id = release.release_id";

    public List<ProfileBOD> findAll() {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT, ProfileBOD.class);
        return query.getResultList();
    }
}
