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
            "select tla.top_level_abie_id, abie.abie_id, tla.state, tla.owner, u.name as owner_name, " +
            "abie.creation_timestamp, asbiep.asbiep_id,asccp.asccp_id, asccp.property_term, " +
            "bc.biz_ctx_id, bc.name as biz_ctx_name " +
            "from top_level_abie tla, abie, asbiep, asccp, biz_ctx bc, app_user u " +
            "where tla.abie_id = abie.abie_id and abie.biz_ctx_id = bc.biz_ctx_id and abie.abie_id = asbiep.role_of_abie_id " +
            "and asbiep.based_asccp_id = asccp.asccp_id and tla.owner = u.app_user_id";

    public List<ProfileBOD> findAll() {
        Query query = entityManager.createNativeQuery(FIND_ALL_STATEMENT, ProfileBOD.class);
        return query.getResultList();
    }
}
