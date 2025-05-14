package org.oagi.score.gateway.http.api.cc_management.service;

import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcChangesResponse;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.CcRefactorValidationResponse;
import org.oagi.score.gateway.http.api.cc_management.model.CcListEntryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.graph.model.CoreComponentGraphContext;
import org.oagi.score.gateway.http.api.graph.model.Node;
import org.oagi.score.gateway.http.api.graph.repository.GraphContextRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class CcQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private GraphContextRepository graphContextRepository;

    private AccQueryRepository accQueryRepository(ScoreUser requester) {
        return repositoryFactory.accQueryRepository(requester);
    }

    private AsccpQueryRepository asccpQueryRepository(ScoreUser requester) {
        return repositoryFactory.asccpQueryRepository(requester);
    }

    private BccpQueryRepository bccpQueryRepository(ScoreUser requester) {
        return repositoryFactory.bccpQueryRepository(requester);
    }

    private DtQueryRepository dtQueryRepository(ScoreUser requester) {
        return repositoryFactory.dtQueryRepository(requester);
    }

    /**
     * @param requester
     * @param filterCriteria
     * @param pageRequest
     * @return
     */
    public ResultAndCount<CcListEntryRecord> getCcList(
            ScoreUser requester, CcListFilterCriteria filterCriteria, PageRequest pageRequest) {

        var ccQuery = repositoryFactory.ccQueryRepository(requester);
        return ccQuery.getCcList(filterCriteria, pageRequest);
    }

    public List<CcListEntryRecord> getBaseAccList(
            ScoreUser requester, AccManifestId accManifestId) {

        var ccQuery = repositoryFactory.ccQueryRepository(requester);
        return ccQuery.getBaseAccList(accManifestId);
    }

    /**
     * @param requester
     * @param bccpManifestId
     * @return
     * @throws IllegalArgumentException
     * @throws EmptyResultDataAccessException
     */
    public BccpDetailsRecord getBccpDetailsByBccpManifestId(
            ScoreUser requester, BccpManifestId bccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (bccpManifestId == null) {
            throw new IllegalArgumentException("`bccpManifestId` must not be null");
        }

        BccpDetailsRecord bccpDetails = repositoryFactory.bccpQueryRepository(requester).getBccpDetails(bccpManifestId);
        if (bccpDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return bccpDetails;
    }

    /**
     * @param requester
     * @param dtScManifestId
     * @return
     * @throws IllegalArgumentException
     * @throws EmptyResultDataAccessException
     */
    public DtScDetailsRecord getDtScDetailsByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null");
        }
        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null");
        }

        DtScDetailsRecord dtScManifestDetails = repositoryFactory.dtQueryRepository(requester)
                .getDtScDetails(dtScManifestId);
        if (dtScManifestDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return dtScManifestDetails;
    }

    public AccDetailsRecord getAccDetails(ScoreUser requester, AccManifestId accManifestId) {
        return accQueryRepository(requester).getAccDetails(accManifestId);
    }

    public AccDetailsRecord getPrevAccDetails(ScoreUser requester, AccManifestId accManifestId) {
        AccDetailsRecord prevAccDetails = accQueryRepository(requester).getPrevAccDetails(accManifestId);
        if (prevAccDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return prevAccDetails;
    }

    public AsccDetailsRecord getAsccDetails(ScoreUser requester, AsccManifestId asccManifestId) {
        return accQueryRepository(requester).getAsccDetails(asccManifestId);
    }

    public AsccpDetailsRecord getAsccpDetails(ScoreUser requester, AsccpManifestId asccpManifestId) {
        return asccpQueryRepository(requester).getAsccpDetails(asccpManifestId);
    }

    public AsccpDetailsRecord getPrevAsccpDetails(ScoreUser requester, AsccpManifestId asccpManifestId) {
        AsccpDetailsRecord prevAsccpDetails = asccpQueryRepository(requester).getPrevAsccpDetails(asccpManifestId);
        if (prevAsccpDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return prevAsccpDetails;
    }

    public BccDetailsRecord getBccDetails(ScoreUser requester, BccManifestId bccManifestId) {
        return accQueryRepository(requester).getBccDetails(bccManifestId);
    }

    public BccpDetailsRecord getBccpDetails(ScoreUser requester, BccpManifestId bccpManifestId) {
        return bccpQueryRepository(requester).getBccpDetails(bccpManifestId);
    }

    public BccpDetailsRecord getPrevBccpDetails(ScoreUser requester, BccpManifestId bccpManifestId) {
        BccpDetailsRecord prevBccpDetails = bccpQueryRepository(requester).getPrevBccpDetails(bccpManifestId);
        if (prevBccpDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return prevBccpDetails;
    }

    public DtDetailsRecord getDtDetails(ScoreUser requester, DtManifestId dtManifestId) {
        return dtQueryRepository(requester).getDtDetails(dtManifestId);
    }

    public DtDetailsRecord getPrevDtDetails(ScoreUser requester, DtManifestId dtManifestId) {
        DtDetailsRecord prevDtDetails = dtQueryRepository(requester).getPrevDtDetails(dtManifestId);
        if (prevDtDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return prevDtDetails;
    }

    public DtScDetailsRecord getDtScDetails(ScoreUser requester, DtScManifestId dtScManifestId) {
        return dtQueryRepository(requester).getDtScDetails(dtScManifestId);
    }

    public CcChangesResponse getCcChanges(ScoreUser requester, ReleaseId releaseId) {

        Collection<CcChangesResponse.CcChange> changeList =
                repositoryFactory.ccQueryRepository(requester).getCcChanges(releaseId);
        return new CcChangesResponse(changeList);
    }

    public List<DtScAwdPriDetailsRecord> getDefaultPrimitiveValues(
            ScoreUser requester, String representationTerm, DtScManifestId dtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (!hasLength(representationTerm)) {
            throw new IllegalArgumentException("`representationTerm` must not be empty.");
        }

        if (dtScManifestId == null) {
            throw new IllegalArgumentException("`dtScManifestId` must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtScDetailsRecord dtSc = query.getDtScDetails(dtScManifestId);
        if (dtSc == null) {
            throw new IllegalArgumentException("DT_SC record not found.");
        }

        if (StringUtils.equals(representationTerm, dtSc.representationTerm())) {
            return dtSc.dtScAwdPriList();
        }

        return query.getDefaultPrimitiveValues(representationTerm).stream()
                .map(dtAwdPri -> new DtScAwdPriDetailsRecord(
                        null,
                        dtAwdPri.release(),
                        dtSc.dtScId(),
                        dtAwdPri.cdtPriName(),
                        dtAwdPri.xbt(),
                        dtAwdPri.codeList(),
                        dtAwdPri.agencyIdList(),
                        dtAwdPri.isDefault(),
                        false)).collect(Collectors.toList());
    }

    public void assertSamePropertyTerm(
            ScoreUser requester, AccManifestId accManifestId, String propertyTerm) throws AssertionError {

        var query = repositoryFactory.accQueryRepository(requester);
        if (query.hasSamePropertyTerm(accManifestId, propertyTerm)) {
            throw new AssertionError("There is a duplicate property term '" + propertyTerm + "' " +
                    "that could cause an ambiguous content model depending on the expression.");
        }
    }

    public void verifySetBasedAcc(
            ScoreUser requester, AccManifestId accManifestId, AccManifestId basedAccManifestId) throws AssertionError {

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(accManifestId);

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        for (AsccSummaryRecord ascc : query.getAsccSummaryList(accManifestId)) {
            AsccpDetailsRecord asccp = asccpQuery.getAsccpDetails(ascc.toAsccpManifestId());
            if (asccp.roleOfAcc().isGroup()) {
                verifySetBasedAcc(requester, asccp.roleOfAcc().accManifestId(), basedAccManifestId);
            } else {
                assertSamePropertyTerm(requester, basedAccManifestId, asccp.propertyTerm());
            }
        }

        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        for (BccSummaryRecord bcc : query.getBccSummaryList(accManifestId)) {
            BccpDetailsRecord bccp = bccpQuery.getBccpDetails(bcc.toBccpManifestId());
            assertSamePropertyTerm(requester, basedAccManifestId, bccp.propertyTerm());
        }

        if (acc.basedAccManifestId() != null) {
            verifySetBasedAcc(requester, acc.basedAccManifestId(), basedAccManifestId);
        }
    }

    public void verifyAppendAsccp(
            ScoreUser requester, AccManifestId accManifestId, AsccpManifestId asccpManifestId) throws AssertionError {

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        AsccpDetailsRecord asccp = asccpQuery.getAsccpDetails(asccpManifestId);
        if (asccp.roleOfAcc().isGroup()) {
            verifySetBasedAcc(requester, asccp.roleOfAcc().accManifestId(), accManifestId);
        } else {
            assertSamePropertyTerm(requester, accManifestId, asccp.propertyTerm());
        }
    }

    public void verifyAppendBccp(
            ScoreUser requester, AccManifestId accManifestId, BccpManifestId bccpManifestId) throws AssertionError {

        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        BccpDetailsRecord bccp = bccpQuery.getBccpDetails(bccpManifestId);
        assertSamePropertyTerm(requester, accManifestId, bccp.propertyTerm());
    }

    // begins supporting dynamic primitive type lists

    public List<DtAwdPriSummaryRecord> availableDtAwdPriListByDtManifestId(
            ScoreUser requester, DtManifestId dtManifestId) {

        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        return dtQuery.getDtAwdPriSummaryList(dtManifestId);
    }

    public List<DtScAwdPriSummaryRecord> availableDtScAwdPriListByDtScManifestId(
            ScoreUser requester, DtScManifestId dtScManifestId) {

        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        return dtQuery.getDtScAwdPriSummaryList(dtScManifestId);
    }

    // ends supporting dynamic primitive type lists

    public String generatePlantUmlText(ScoreUser requester,
                                       AsccpManifestId asccpManifestId,
                                       String asccpLinkTemplate,
                                       String bccpLinkTemplate) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("!pragma layout smetana\n");
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

        var accQuery = repositoryFactory.accQueryRepository(requester);
        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        var dtQuery = repositoryFactory.dtQueryRepository(requester);

        AsccpSummaryRecord asccp = asccpQuery.getAsccpSummary(asccpManifestId);

        AccSummaryRecord acc = accQuery.getAccSummary(asccp.roleOfAccManifestId());

        String accObjectClassTerm = acc.objectClassTerm().replaceAll(" ", "");
        sb.append("class ").append(accObjectClassTerm).append(" {\n");

        StringBuilder attributesSb = new StringBuilder();
        StringBuilder propertiesSb = new StringBuilder();

        Stack<AccSummaryRecord> accStack = new Stack();
        accStack.add(acc);

        while (acc.basedAccManifestId() != null) {
            acc = accQuery.getAccSummary(acc.basedAccManifestId());
            accStack.add(acc);
        }

        List<AsccSummaryRecord> asccList = new ArrayList<>();

        while (!accStack.isEmpty()) {
            acc = accStack.pop();

            CoreComponentGraphContext coreComponentGraphContext =
                    graphContextRepository.buildGraphContext(requester, acc.release().releaseId());
            List<Node> children = new ArrayList<>(coreComponentGraphContext.findChildren(
                    coreComponentGraphContext.toNode(acc), true));

            while (!children.isEmpty()) {
                Node child = children.remove(0);
                if (child.getType() == Node.NodeType.ASCC) {
                    AsccSummaryRecord asccChild = accQuery.getAsccSummary((AsccManifestId) child.getManifestId());
                    AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(asccChild.toAsccpManifestId());
                    AccSummaryRecord roleOfAcc = accQuery.getAccSummary(toAsccp.roleOfAccManifestId());
                    if (roleOfAcc.isGroup()) {
                        children.addAll(0, coreComponentGraphContext.findChildren(
                                coreComponentGraphContext.toNode(roleOfAcc), true));
                    } else {
                        asccList.add(asccChild);
                    }
                } else if (child.getType() == Node.NodeType.BCC) {
                    BccSummaryRecord bccChild = accQuery.getBccSummary((BccManifestId) child.getManifestId());
                    StringBuilder bccSb;
                    if (bccChild.entityType() == EntityType.Attribute) {
                        bccSb = attributesSb;
                    } else {
                        bccSb = propertiesSb;
                    }
                    BccpSummaryRecord bccp = bccpQuery.getBccpSummary(bccChild.toBccpManifestId());
                    String bccpPropertyTerm = bccp.propertyTerm().replaceAll(" ", "");
                    DtSummaryRecord dt = dtQuery.getDtSummary(bccp.dtManifestId());
                    String dtDataTypeTerm = dt.dataTypeTerm().replaceAll(" ", "");

                    bccSb.append("\t").append(dtDataTypeTerm)
                            .append(" [[")
                            .append(bccpLinkTemplate.replaceAll("\\{manifestId\\}", bccp.bccpManifestId().toString()))
                            .append(" ").append(bccpPropertyTerm)
                            .append("]]\n");
                }
            }
        }

        sb.append(attributesSb);
        sb.append(propertiesSb);
        sb.append("}\n");

        StringBuilder classesSb = new StringBuilder();
        StringBuilder relationshipSb = new StringBuilder();

        List<String> positions = getPositions(asccList.size());
        Streams.zip(asccList.stream(), positions.stream(), (r, s) -> Pair.of(r, s)).forEach(pair -> {
            AsccSummaryRecord ascc = pair.getLeft();
            String pos = pair.getRight();

            AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(ascc.toAsccpManifestId());
            if ("Extension".equals(toAsccp.propertyTerm())) {
                pos = "d";
            }
            String asccpPropertyTerm = "\"<u>" + toAsccp.propertyTerm().replaceAll(" ", "") + "\"";

            classesSb.append("\n");
            classesSb.append("class ").append(asccpPropertyTerm).append(" <<").append(styleName).append(">> [[")
                    .append(asccpLinkTemplate.replaceAll("\\{manifestId\\}", ascc.toAsccpManifestId().toString()))
                    .append("]] {\n")
                    .append("}\n");

            relationshipSb.append(accObjectClassTerm).append(" o-").append(pos).append("- \"").append(ascc.cardinality().min()).append("...")
                    .append(ascc.cardinality().max() == -1 ? "∞" : ascc.cardinality().max()).append("\" ").append(asccpPropertyTerm).append("\n");
        });

        sb.append(classesSb);
        sb.append("\n");
        sb.append(relationshipSb);
        sb.append("\n");

        sb.append("hide circle\n");
        sb.append("hide empty members\n");
        sb.append("hide <<").append(styleName).append(">> stereotype\n");
        sb.append("@enduml");

        return sb.toString();
    }

    private List<String> getPositions(int n) {
        String[] directions = {"d", "r", "u", "l", "d", "u", "d", "u"};

        return IntStream.range(0, n)
                .mapToObj(i -> {
                    return directions[i % 8];
                })
                .collect(Collectors.toList());
    }

    public CcRefactorValidationResponse validateAsccRefactoring(
            ScoreUser requester, AsccManifestId targetManifestId, AccManifestId destinationManifestId) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        return accQuery.validateAsccRefactoring(targetManifestId, destinationManifestId);
    }

    public CcRefactorValidationResponse validateBccRefactoring(
            ScoreUser requester, BccManifestId targetManifestId, AccManifestId destinationManifestId) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        return accQuery.validateBccRefactoring(targetManifestId, destinationManifestId);
    }
}
