package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.BieListEntryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.oagi.score.gateway.http.common.util.Utility.toLowerCamelCase;
import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BieQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    public ResultAndCount<BieListEntryRecord> getBieList(
            ScoreUser requester, BieListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var bieQuery = repositoryFactory.bieQueryRepository(requester);
        return bieQuery.getBieList(filterCriteria, pageRequest);
    }

    public ResultAndCount<BieListEntryRecord> getBieList(
            ScoreUser requester, BieListInBiePackageFilterCriteria filterCriteria, PageRequest pageRequest) {

        var bieQuery = repositoryFactory.bieQueryRepository(requester);
        return bieQuery.getBieList(filterCriteria, pageRequest);
    }

    public String generatePlantUmlText(ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, String topLevelAsbiepLinkTemplate) {

        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("!pragma layout smetana\n");
        sb.append("skinparam svgLinkTarget _blank\n");
        String styleName = "link_style";
        sb.append("<style>\n")
                .append("\t").append("classDiagram {\n")
                .append("\t\t").append("class {\n")
                .append("\t\t\t").append("header {\n")
                .append("\t\t\t\t").append(".").append(styleName).append(" {\n")
                .append("\t\t\t\t\t").append("FontColor blue\n")
                .append("\t\t\t\t}\n")
                .append("\t\t\t}\n")
                .append("\t\t}\n")
                .append("\t}\n")
                .append("</style>\n");
        sb.append("\n");

        Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap = new HashMap<>();
        Map<TopLevelAsbiepSummaryRecord, Set<TopLevelAsbiepSummaryRecord>> reusesMap = new HashMap<>();
        Map<TopLevelAsbiepSummaryRecord, TopLevelAsbiepSummaryRecord> inheritsMap = new HashMap<>();

        findTopLevelAsbiepRecursively(requester, topLevelAsbiepId, topLevelAsbiepMap, reusesMap, inheritsMap);

        for (TopLevelAsbiepSummaryRecord topLevelAsbiep : topLevelAsbiepMap.values()) {
            sb.append(toClassDiagram(topLevelAsbiep, styleName, topLevelAsbiepLinkTemplate, reusesMap)).append("\n");
        }

        for (Map.Entry<TopLevelAsbiepSummaryRecord, Set<TopLevelAsbiepSummaryRecord>> reuses : reusesMap.entrySet()) {
            for (TopLevelAsbiepSummaryRecord reuse : reuses.getValue()) {
                sb.append("\"" + toName(reuses.getKey()) + "\" --> \"" + toName(reuse) + "\"\n");
            }
        }

        for (Map.Entry<TopLevelAsbiepSummaryRecord, TopLevelAsbiepSummaryRecord> inherits : inheritsMap.entrySet()) {
            sb.append("\"" + toName(inherits.getValue()) + "\" <|-- \"" + toName(inherits.getKey()) + "\"\n");
        }

        sb.append("\n");
        sb.append("hide circle\n");
        sb.append("hide empty members\n");
        sb.append("hide <<").append(styleName).append(">> stereotype\n");
        sb.append("@enduml");

        return sb.toString();
    }

    private void findTopLevelAsbiepRecursively(
            ScoreUser requester,
            TopLevelAsbiepId topLevelAsbiepId,
            Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap,
            Map<TopLevelAsbiepSummaryRecord, Set<TopLevelAsbiepSummaryRecord>> reusesMap,
            Map<TopLevelAsbiepSummaryRecord, TopLevelAsbiepSummaryRecord> inheritsMap) {

        if (!topLevelAsbiepMap.containsKey(topLevelAsbiepId)) {
            var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);
            TopLevelAsbiepSummaryRecord topLevelAsbiep = topLevelAsbiepQuery.getTopLevelAsbiepSummary(topLevelAsbiepId);

            topLevelAsbiepMap.put(topLevelAsbiepId, topLevelAsbiep);

            List<TopLevelAsbiepSummaryRecord> reusingTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusingTopLevelAsbiepSummaryList(topLevelAsbiepId);
            for (TopLevelAsbiepSummaryRecord reusingTopLevelAsbiep : reusingTopLevelAsbiepList) {
                if (!reusesMap.containsKey(topLevelAsbiep)) {
                    reusesMap.put(topLevelAsbiep, new HashSet<>());
                }
                reusesMap.get(topLevelAsbiep).add(reusingTopLevelAsbiep);

                findTopLevelAsbiepRecursively(requester, reusingTopLevelAsbiep.topLevelAsbiepId(),
                        topLevelAsbiepMap, reusesMap, inheritsMap);
            }
            List<TopLevelAsbiepSummaryRecord> reusedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getReusedTopLevelAsbiepSummaryList(topLevelAsbiepId);
            for (TopLevelAsbiepSummaryRecord reusedTopLevelAsbiep : reusedTopLevelAsbiepList) {
                if (!reusesMap.containsKey(reusedTopLevelAsbiep)) {
                    reusesMap.put(reusedTopLevelAsbiep, new HashSet<>());
                }
                reusesMap.get(reusedTopLevelAsbiep).add(topLevelAsbiep);

                findTopLevelAsbiepRecursively(requester, reusedTopLevelAsbiep.topLevelAsbiepId(),
                        topLevelAsbiepMap, reusesMap, inheritsMap);
            }
            List<TopLevelAsbiepSummaryRecord> derivedTopLevelAsbiepList =
                    topLevelAsbiepQuery.getDerivedTopLevelAsbiepSummaryList(topLevelAsbiepId);
            for (TopLevelAsbiepSummaryRecord derivedTopLevelAsbiep : derivedTopLevelAsbiepList) {
                if (!inheritsMap.containsKey(derivedTopLevelAsbiep)) {
                    inheritsMap.put(derivedTopLevelAsbiep, topLevelAsbiep);
                }

                findTopLevelAsbiepRecursively(requester, derivedTopLevelAsbiep.topLevelAsbiepId(),
                        topLevelAsbiepMap, reusesMap, inheritsMap);
            }
        }
    }

    private String toName(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return hasLength(topLevelAsbiep.displayName()) ?
                topLevelAsbiep.displayName() :
                topLevelAsbiep.propertyTerm();
    }

    private String toClassDiagram(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                  String styleName, String topLevelAsbiepLinkTemplate,
                                  Map<TopLevelAsbiepSummaryRecord, Set<TopLevelAsbiepSummaryRecord>> reusesMap) {
        StringBuilder sb = new StringBuilder();

        sb.append("class \"" + toName(topLevelAsbiep) + "\" ")
                .append("<<").append(styleName).append(">> [[")
                .append(topLevelAsbiepLinkTemplate.replaceAll("\\{topLevelAsbiepId\\}", topLevelAsbiep.topLevelAsbiepId().toString()))
                .append("]] {\n");

        for (TopLevelAsbiepSummaryRecord reuse : reusesMap.getOrDefault(topLevelAsbiep, Collections.emptySet())) {
            sb.append("\t +" + toLowerCamelCase(reuse.propertyTerm()) + ": \"" + toName(reuse) + "\"\n");
        }

        sb.append("}").append("\n");

        return sb.toString();
    }

}
