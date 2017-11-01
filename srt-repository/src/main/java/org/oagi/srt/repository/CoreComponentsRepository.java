package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CoreComponentState;
import org.oagi.srt.repository.entity.CoreComponents;
import org.oagi.srt.repository.entity.OagisComponentType;
import org.oagi.srt.repository.entity.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CoreComponentsRepository {

    @Autowired
    private EntityManager entityManager;

    private static final Map<String, String> FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES = new HashMap();
    static {
        FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES.put("ACC",
                "SELECT 'ACC' as type, acc.acc_id AS id, acc.guid, acc.den, acc.owner_user_id, u.login_id AS owner, acc.release_id, acc.state, " +
                "acc.oagis_component_type, acc.last_updated_by, acc.last_update_timestamp, m.module, acc.definition " +
                "FROM acc JOIN app_user u ON acc.owner_user_id = u.app_user_id LEFT JOIN module m ON acc.module_id = m.module_id " +
                "WHERE acc.revision_num = 0 AND acc.release_id IS NULL");
        FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES.put("ASCC",
                "SELECT 'ASCC' as type, ascc.ascc_id AS id, ascc.guid, ascc.den, ascc.owner_user_id, u.login_id AS owner, ascc.release_id, ascc.state, " +
                "0 AS oagis_component_type, ascc.last_updated_by, ascc.last_update_timestamp, null AS module, ascc.definition " +
                "FROM ascc JOIN app_user u ON ascc.owner_user_id = u.app_user_id JOIN asccp ON ascc.to_asccp_id = asccp.asccp_id JOIN acc ON asccp.role_of_acc_id = acc.acc_id " +
                "WHERE ascc.revision_num = 0 AND ascc.release_id IS NULL AND acc.oagis_component_type <> " + OagisComponentType.UserExtensionGroup.getValue());
        FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES.put("ASCCP",
                "SELECT 'ASCCP' as type, asccp.asccp_id AS id, asccp.guid, asccp.den, asccp.owner_user_id, u.login_id AS owner, asccp.release_id, asccp.state, " +
                "0 AS oagis_component_type, asccp.last_updated_by, asccp.last_update_timestamp, m.module, asccp.definition " +
                "FROM asccp JOIN app_user u ON asccp.owner_user_id = u.app_user_id JOIN acc ON asccp.role_of_acc_id = acc.acc_id LEFT JOIN module m ON asccp.module_id = m.module_id " +
                "WHERE asccp.revision_num = 0 AND asccp.release_id IS NULL AND acc.oagis_component_type <> " + OagisComponentType.UserExtensionGroup.getValue());
        FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES.put("BCC",
                "SELECT 'BCC' as type, bcc.bcc_id AS id, bcc.guid, bcc.den, bcc.owner_user_id, u.login_id AS owner, bcc.release_id, bcc.state, " +
                "0 AS oagis_component_type, bcc.last_updated_by, bcc.last_update_timestamp, null AS module, bcc.definition " +
                "FROM bcc JOIN app_user u ON bcc.owner_user_id = u.app_user_id " +
                "WHERE bcc.revision_num = 0 AND bcc.release_id IS NULL");
        FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES.put("BCCP",
                "SELECT 'BCCP' as type, bccp.bccp_id AS id, bccp.guid, bccp.den, bccp.owner_user_id, u.login_id AS owner, bccp.release_id, bccp.state, " +
                "0 AS oagis_component_type, bccp.last_updated_by, bccp.last_update_timestamp, m.module, bccp.definition " +
                "FROM bccp JOIN app_user u ON bccp.owner_user_id = u.app_user_id LEFT JOIN module m ON bccp.module_id = m.module_id " +
                "WHERE bccp.revision_num = 0 AND bccp.release_id IS NULL");
    }

    private static final String FIND_ALL_STATEMENT_FOR_CURRENT_RELEASE =
            "SELECT t.type, t.id, t.guid, t.den, t.owner_user_id, t.owner, t.release_id, t.state, t.oagis_component_type, " +
                    "uu.login_id AS last_updated_user, t.last_update_timestamp, t.module, t.definition " +
            "FROM ( $INNER_QUERY$ ) t JOIN app_user uu ON t.last_updated_by = uu.app_user_id " +
            "WHERE t.state IN (:states)";

    private static final Map<String, String> FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES = new HashMap();
    static {
        FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES.put("ACC",
                "SELECT 'ACC' as type, acc.acc_id AS id, acc.guid, acc.den, acc.owner_user_id, u.login_id AS owner, acc.release_id, acc.state, " +
                "acc.oagis_component_type, acc.last_updated_by, acc.last_update_timestamp, m.module, acc.definition " +
                "FROM acc JOIN app_user u ON acc.owner_user_id = u.app_user_id LEFT JOIN module m ON acc.module_id = m.module_id " +
                "WHERE acc.revision_num > 0 AND acc.release_id <= :releaseId");
        FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES.put("ASCC",
                "SELECT 'ASCC' as type, ascc.ascc_id AS id, ascc.guid, ascc.den, ascc.owner_user_id, u.login_id AS owner, ascc.release_id, ascc.state, " +
                "0 AS oagis_component_type, ascc.last_updated_by, ascc.last_update_timestamp, null AS module, ascc.definition " +
                "FROM ascc JOIN app_user u ON ascc.owner_user_id = u.app_user_id JOIN asccp ON ascc.to_asccp_id = asccp.asccp_id JOIN acc ON asccp.role_of_acc_id = acc.acc_id " +
                "WHERE ascc.revision_num > 0 AND ascc.release_id <= :releaseId AND acc.oagis_component_type <> " + OagisComponentType.UserExtensionGroup.getValue());
        FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES.put("ASCCP",
                "SELECT 'ASCCP' as type, asccp.asccp_id AS id, asccp.guid, asccp.den, asccp.owner_user_id, u.login_id AS owner, asccp.release_id, asccp.state, " +
                "0 AS oagis_component_type, asccp.last_updated_by, asccp.last_update_timestamp, m.module, asccp.definition " +
                "FROM asccp JOIN app_user u ON asccp.owner_user_id = u.app_user_id JOIN acc ON asccp.role_of_acc_id = acc.acc_id LEFT JOIN module m ON asccp.module_id = m.module_id " +
                "WHERE asccp.revision_num > 0 AND asccp.release_id <= :releaseId AND acc.oagis_component_type <> " + OagisComponentType.UserExtensionGroup.getValue());
        FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES.put("BCC",
                "SELECT 'BCC' as type, bcc.bcc_id AS id, bcc.guid, bcc.den, bcc.owner_user_id, u.login_id AS owner, bcc.release_id, bcc.state, " +
                "0 AS oagis_component_type, bcc.last_updated_by, bcc.last_update_timestamp, null AS module, bcc.definition " +
                "FROM bcc JOIN app_user u ON bcc.owner_user_id = u.app_user_id " +
                "WHERE bcc.revision_num > 0 AND bcc.release_id <= :releaseId");
        FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES.put("BCCP",
                "SELECT 'BCCP' as type, bccp.bccp_id AS id, bccp.guid, bccp.den, bccp.owner_user_id, u.login_id AS owner, bccp.release_id, bccp.state, " +
                "0 AS oagis_component_type, bccp.last_updated_by, bccp.last_update_timestamp, m.module, bccp.definition " +
                "FROM bccp JOIN app_user u ON bccp.owner_user_id = u.app_user_id LEFT JOIN module m ON bccp.module_id = m.module_id " +
                "WHERE bccp.revision_num > 0 AND bccp.release_id <= :releaseId");
    }

    private static final String FIND_ALL_STATEMENT_FOR_FIXED_RELEASE =
            "SELECT t.type, t.id, t.guid, t.den, t.owner_user_id, t.owner, t.release_id, t.state, t.oagis_component_type, " +
                    "uu.login_id AS last_updated_user, t.last_update_timestamp, t.module, t.definition " +
            "FROM ( $INNER_QUERY$ ) t JOIN app_user uu ON t.last_updated_by = uu.app_user_id " +
            "WHERE t.state IN (:states)";

    private static final String FIND_DELTA_STATEMENT =
            "SELECT t.type, t.id, t.guid, t.den, t.owner_user_id, t.owner, t.release_id, t.state, " +
                    "t.oagis_component_type, uu.login_id AS last_updated_user, t.last_update_timestamp, t.module, t.definition " +
            "FROM ( " +
            "SELECT 'ACC' as type, acc.acc_id AS id, acc.guid, acc.den, acc.owner_user_id, u.login_id AS owner, acc.release_id, acc.state, " +
                    "acc.oagis_component_type, acc.last_updated_by, acc.last_update_timestamp, m.module, acc.definition " +
            "FROM acc JOIN app_user u ON acc.owner_user_id = u.app_user_id LEFT JOIN module m ON acc.module_id = m.module_id WHERE acc.revision_num != 0 " +
            "UNION ALL " +
            "SELECT 'ASCC' as type, ascc.ascc_id AS id, ascc.guid, ascc.den, ascc.owner_user_id, u.login_id AS owner, ascc.release_id, ascc.state, " +
                    "0 AS oagis_component_type, ascc.last_updated_by, ascc.last_update_timestamp, null AS module, ascc.definition " +
            "FROM ascc JOIN app_user u ON ascc.owner_user_id = u.app_user_id JOIN asccp ON ascc.to_asccp_id = asccp.asccp_id JOIN acc ON asccp.role_of_acc_id = acc.acc_id WHERE ascc.revision_num != 0 AND acc.oagis_component_type <> " + OagisComponentType.UserExtensionGroup.getValue() + " " +
            "UNION ALL " +
            "SELECT 'ASCCP' as type, asccp.asccp_id AS id, asccp.guid, asccp.den, asccp.owner_user_id, u.login_id AS owner, asccp.release_id, asccp.state, " +
                    "0 AS oagis_component_type, asccp.last_updated_by, asccp.last_update_timestamp, m.module, asccp.definition " +
            "FROM asccp JOIN app_user u ON asccp.owner_user_id = u.app_user_id JOIN acc ON asccp.role_of_acc_id = acc.acc_id LEFT JOIN module m ON asccp.module_id = m.module_id WHERE asccp.revision_num != 0 AND acc.oagis_component_type <> " + OagisComponentType.UserExtensionGroup.getValue() + " " +
            "UNION ALL " +
            "SELECT 'BCC' as type, bcc.bcc_id AS id, bcc.guid, bcc.den, bcc.owner_user_id, u.login_id AS owner, bcc.release_id, bcc.state, " +
                    "0 AS oagis_component_type, bcc.last_updated_by, bcc.last_update_timestamp, null AS module, bcc.definition " +
            "FROM bcc JOIN app_user u ON bcc.owner_user_id = u.app_user_id WHERE bcc.revision_num != 0 " +
            "UNION ALL " +
            "SELECT 'BCCP' as type, bccp.bccp_id AS id, bccp.guid, bccp.den, bccp.owner_user_id, u.login_id AS owner, bccp.release_id, bccp.state, " +
                    "0 AS oagis_component_type, bccp.last_updated_by, bccp.last_update_timestamp, m.module, bccp.definition " +
            "FROM bccp JOIN app_user u ON bccp.owner_user_id = u.app_user_id LEFT JOIN module m ON bccp.module_id = m.module_id WHERE bccp.revision_num != 0 " +
            ") t JOIN app_user uu ON t.last_updated_by = uu.app_user_id " +
            "WHERE t.state in (2, 3) and t.owner_user_id in (select au.app_user_id from app_user au where au.oagis_developer_indicator = 1)";

    public List<CoreComponents> findAll(List<String> types, List<CoreComponentState> states, Release release, Sort.Order order) {
        if (types.isEmpty()) {
            types = Arrays.asList(
                    "ACC", "ASCC", "ASCCP", "BCC", "BCCP"
            );
        }
        if (states.isEmpty()) {
            states = Arrays.asList(
                    CoreComponentState.Editing,
                    CoreComponentState.Candidate,
                    CoreComponentState.Published);
        }

        String sortProperty = order.getProperty() + " " + order.getDirection().toString();
        Query query;
        if (release.equals(Release.CURRENT_RELEASE)) {
            String statement =
                    FIND_ALL_STATEMENT_FOR_CURRENT_RELEASE.replace("$INNER_QUERY$",
                            String.join(" UNION ALL ",
                                    types.stream().map(e -> FIND_STATEMENT_FOR_CURRENT_RELEASE_MAP_BY_TYPES.get(e))
                                            .collect(Collectors.toList())));
            query = entityManager.createNativeQuery(statement + " ORDER BY " + sortProperty, CoreComponents.class);
        } else {
            String statement =
                    FIND_ALL_STATEMENT_FOR_FIXED_RELEASE.replace("$INNER_QUERY$",
                            String.join(" UNION ALL ",
                                    types.stream().map(e -> FIND_STATEMENT_FOR_FIXED_RELEASE_MAP_BY_TYPES.get(e))
                                            .collect(Collectors.toList())));
            query = entityManager.createNativeQuery(statement + " ORDER BY " + sortProperty, CoreComponents.class);
            query.setParameter("releaseId", release.getReleaseId());
        }

        List<Integer> statesParam = states.stream().map(e -> e.getValue()).collect(Collectors.toList());
        query.setParameter("states", statesParam);

        return query.getResultList();
    }

    public List<CoreComponents> findDeltaForRelease(Release release) {
        String releaseClause = " and t.release_id";

        if (release.getReleaseId() == 0L) {
            releaseClause += " is null ";
        } else {
            releaseClause  = releaseClause + " = " + release.getReleaseId();
        }

        Query query = entityManager.createNativeQuery(FIND_DELTA_STATEMENT + releaseClause +
                " ORDER BY t.last_update_timestamp DESC", CoreComponents.class);

        return query.getResultList();
    }
}
