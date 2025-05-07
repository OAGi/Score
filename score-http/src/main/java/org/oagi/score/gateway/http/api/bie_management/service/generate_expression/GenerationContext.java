package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextValueRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDetailsRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class GenerationContext implements InitializingBean, CcDocument {

    private final ScoreUser requester;
    private final List<TopLevelAsbiepSummaryRecord> topLevelAsbieps;
    private Map<TopLevelAsbiepId, TopLevelAsbiepSummaryRecord> topLevelAsbiepMap;
    private Set<TopLevelAsbiepSummaryRecord> refTopLevelAsbiepSet;

    @Autowired
    private RepositoryFactory repositoryFactory;

    // Prepared Datas
    private CcDocument ccDocument;

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        return ccDocument.getAcc(accManifestId);
    }

    @Override
    public List<CcAssociation> getAssociations(AccSummaryRecord accManifest) {
        return ccDocument.getAssociations(accManifest);
    }

    @Override
    public AsccSummaryRecord getAscc(AsccManifestId asccManifestId) {
        return ccDocument.getAscc(asccManifestId);
    }

    @Override
    public BccSummaryRecord getBcc(BccManifestId bccManifestId) {
        return ccDocument.getBcc(bccManifestId);
    }

    @Override
    public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
        return ccDocument.getAsccp(asccpManifestId);
    }

    @Override
    public Collection<AsccpSummaryRecord> getAsccpList() {
        return ccDocument.getAsccpList();
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        return ccDocument.getBccp(bccpManifestId);
    }

    @Override
    public DtSummaryRecord getDt(DtManifestId dtManifestId) {
        return ccDocument.getDt(dtManifestId);
    }

    @Override
    public DtScSummaryRecord getDtSc(DtScManifestId dtScManifestId) {
        return ccDocument.getDtSc(dtScManifestId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
        return ccDocument.getDtScList(dtManifestId);
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId) {
        return ccDocument.getDtAwdPriList(dtManifestId);
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId) {
        return ccDocument.getDtScAwdPriList(dtScManifestId);
    }

    public XbtSummaryRecord getXbt(BbieSummaryRecord bbie) {
        XbtSummaryRecord xbt;
        if (bbie.primitiveRestriction().xbtManifestId() != null) {
            xbt = getXbt(bbie.primitiveRestriction().xbtManifestId());
        } else {
            DtAwdPriSummaryRecord dtAwdPri =
                    findDtAwdPriByBbieAndDefaultIsTrue(bbie);
            xbt = Helper.getXbt(this, dtAwdPri);
        }
        return xbt;
    }

    @Override
    public XbtSummaryRecord getXbt(XbtManifestId xbtManifestId) {
        return ccDocument.getXbt(xbtManifestId);
    }

    @Override
    public List<XbtSummaryRecord> getXbtListByName(String name) {
        return ccDocument.getXbtListByName(name);
    }

    @Override
    public List<AccSummaryRecord> getAccListByBasedAccManifestId(AccManifestId basedAccManifestId) {
        return ccDocument.getAccListByBasedAccManifestId(basedAccManifestId);
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpListByRoleOfAccManifestId(AccManifestId roleOfAccManifestId) {
        return ccDocument.getAsccpListByRoleOfAccManifestId(roleOfAccManifestId);
    }

    @Override
    public List<AccSummaryRecord> getAccListByToAsccpManifestId(AsccpManifestId toAsccpManifestId) {
        return ccDocument.getAccListByToAsccpManifestId(toAsccpManifestId);
    }

    @Override
    public List<AccSummaryRecord> getAccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return ccDocument.getAccListByToBccpManifestId(toBccpManifestId);
    }

    @Override
    public List<BccpSummaryRecord> getBccpListByDtManifestId(DtManifestId dtManifestId) {
        return ccDocument.getBccpListByDtManifestId(dtManifestId);
    }

    @Override
    public List<AsccSummaryRecord> getAsccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return ccDocument.getAsccListByFromAccManifestId(fromAccManifestId);
    }

    @Override
    public List<BccSummaryRecord> getBccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return ccDocument.getBccListByFromAccManifestId(fromAccManifestId);
    }

    @Override
    public List<BccSummaryRecord> getBccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return ccDocument.getBccListByToBccpManifestId(toBccpManifestId);
    }

    @Override
    public List<SeqKeySupportable> getAssociationListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return ccDocument.getAssociationListByFromAccManifestId(fromAccManifestId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScListByDtManifestId(DtManifestId dtManifestId) {
        return ccDocument.getDtScListByDtManifestId(dtManifestId);
    }

    @Override
    public BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId) {
        return ccDocument.getBlobContent(blobContentManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByAccManifestId(AccManifestId accManifestId) {
        return ccDocument.getTagListByAccManifestId(accManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByAsccpManifestId(AsccpManifestId asccpManifestId) {
        return ccDocument.getTagListByAsccpManifestId(asccpManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByBccpManifestId(BccpManifestId bccpManifestId) {
        return ccDocument.getTagListByBccpManifestId(bccpManifestId);
    }

    @Override
    public List<TagSummaryRecord> getTagListByDtManifestId(DtManifestId dtManifestId) {
        return ccDocument.getTagListByDtManifestId(dtManifestId);
    }

    @Override
    public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
        return ccDocument.getNamespace(namespaceId);
    }

    private Map<AbieId, AbieSummaryRecord> findAbieMap;
    private Map<AbieId, List<BbieSummaryRecord>> findBbieByFromAbieIdAndUsedIsTrueMap;
    private Map<BbieId, List<BbieScSummaryRecord>> findBbieScByBbieIdAndUsedIsTrueMap;
    private Map<AbieId, List<AsbieSummaryRecord>> findAsbieByFromAbieIdMap;
    private Map<AsbiepId, AsbiepSummaryRecord> findASBIEPMap;
    private Map<AbieId, AsbiepSummaryRecord> findAsbiepByRoleOfAbieIdMap;
    private Map<BbiepId, BbiepSummaryRecord> findBBIEPMap;

    private Map<CodeListManifestId, CodeListSummaryRecord> findCodeListMap;
    private Map<AgencyIdListManifestId, AgencyIdListSummaryRecord> findAgencyIdListMap;
    private Map<AgencyIdListValueManifestId, AgencyIdListValueSummaryRecord> findAgencyIdListValueMap;

    private Map<UserId, String> findUserNameMap;
    private Map<ReleaseId, ReleaseDetailsRecord> findReleaseMap;
    private Map<ContextSchemeId, ContextSchemeSummaryRecord> findContextSchemeMap;
    private Map<ContextCategoryId, ContextCategorySummaryRecord> findContextCategoryMap;
    private Map<NamespaceId, NamespaceSummaryRecord> findNamespaceMap;

    public GenerationContext(ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        this(requester, Arrays.asList(topLevelAsbiep));
    }

    public GenerationContext(ScoreUser requester, List<TopLevelAsbiepSummaryRecord> topLevelAsbieps) {
        this.requester = requester;
        this.topLevelAsbieps = topLevelAsbieps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (topLevelAsbieps == null) {
            throw new IllegalStateException("'topLevelAsbieps' parameter must not be null.");
        }

        Set<ReleaseId> releaseIdSet = topLevelAsbieps.stream().map(e -> e.release().releaseId()).collect(Collectors.toSet());

        Set<TopLevelAsbiepSummaryRecord> topLevelAsbiepSet = new HashSet();
        topLevelAsbiepSet.addAll(topLevelAsbieps);
        refTopLevelAsbiepSet = findRefTopLevelAsbieps(topLevelAsbiepSet);
        topLevelAsbiepSet.addAll(refTopLevelAsbiepSet);

        this.topLevelAsbiepMap = topLevelAsbiepSet.stream()
                .collect(Collectors.toMap(TopLevelAsbiepSummaryRecord::topLevelAsbiepId, Function.identity()));

        init(topLevelAsbiepSet.stream().map(e -> e.topLevelAsbiepId()).collect(Collectors.toList()), releaseIdSet);
    }

    private Set<TopLevelAsbiepSummaryRecord> findRefTopLevelAsbieps(Set<TopLevelAsbiepSummaryRecord> topLevelAsbiepSet) {
        var topLevelAsbiepQuery = repositoryFactory.topLevelAsbiepQueryRepository(requester);

        Set<TopLevelAsbiepSummaryRecord> refTopLevelAsbiepSet = new HashSet();
        refTopLevelAsbiepSet.addAll(topLevelAsbiepQuery.getRefTopLevelAsbiepSummaryList(
                topLevelAsbiepSet.stream().map(e -> e.topLevelAsbiepId()).collect(Collectors.toSet())));

        if (!refTopLevelAsbiepSet.isEmpty()) {
            refTopLevelAsbiepSet.addAll(findRefTopLevelAsbieps(refTopLevelAsbiepSet));
        }

        return refTopLevelAsbiepSet;
    }

    public Set<TopLevelAsbiepSummaryRecord> getRefTopLevelAsbiepSet() {
        return (refTopLevelAsbiepSet != null) ? refTopLevelAsbiepSet : Collections.emptySet();
    }

    private void init(Collection<TopLevelAsbiepId> topLevelAsbiepIds, Collection<ReleaseId> releaseIds) {
        if (releaseIds == null || releaseIds.isEmpty()) {
            throw new IllegalArgumentException("'releaseIds' parameter must not be null.");
        }

        this.ccDocument = new CcDocumentImpl(requester, repositoryFactory, releaseIds);

        List<AbieSummaryRecord> abieList = repositoryFactory.abieQueryRepository(requester)
                .getAbieSummaryList(topLevelAsbiepIds);
        findAbieMap = abieList.stream()
                .collect(Collectors.toMap(e -> e.abieId(), Function.identity()));

        List<BbieSummaryRecord> bbieList = repositoryFactory.bbieQueryRepository(requester)
                .getBbieSummaryList(topLevelAsbiepIds);
        findBbieByFromAbieIdAndUsedIsTrueMap = bbieList.stream()
                .filter(e -> e.used())
                .collect(Collectors.groupingBy(e -> e.fromAbieId()));

        List<BbieScSummaryRecord> bbieScList = repositoryFactory.bbieScQueryRepository(requester)
                .getBbieScSummaryList(topLevelAsbiepIds);
        findBbieScByBbieIdAndUsedIsTrueMap = bbieScList.stream()
                .filter(e -> e.used())
                .collect(Collectors.groupingBy(e -> e.bbieId()));

        List<AsbieSummaryRecord> asbieList = repositoryFactory.asbieQueryRepository(requester)
                .getAsbieSummaryList(topLevelAsbiepIds);
        findAsbieByFromAbieIdMap = asbieList.stream()
                .collect(Collectors.groupingBy(e -> e.fromAbieId()));

        List<AsbiepSummaryRecord> asbiepList = repositoryFactory.asbiepQueryRepository(requester)
                .getAsbiepSummaryList(topLevelAsbiepIds);
        findASBIEPMap = asbiepList.stream()
                .collect(Collectors.toMap(e -> e.asbiepId(), Function.identity()));
        findAsbiepByRoleOfAbieIdMap = asbiepList.stream()
                .collect(Collectors.toMap(e -> e.roleOfAbieId(), Function.identity()));

        List<BbiepSummaryRecord> bbiepList = repositoryFactory.bbiepQueryRepository(requester)
                .getBbiepSummaryList(topLevelAsbiepIds);
        findBBIEPMap = bbiepList.stream()
                .collect(Collectors.toMap(e -> e.bbiepId(), Function.identity()));

        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        findCodeListMap = codeListQuery.getCodeListSummaryList().stream()
                .collect(Collectors.toMap(CodeListSummaryRecord::codeListManifestId, Function.identity()));
        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);
        findAgencyIdListMap = agencyIdListQuery.getAgencyIdListSummaryList().stream()
                .collect(Collectors.toMap(AgencyIdListSummaryRecord::agencyIdListManifestId, Function.identity()));
        findAgencyIdListValueMap = agencyIdListQuery.getAgencyIdListValueSummaryList().stream()
                .collect(Collectors.toMap(AgencyIdListValueSummaryRecord::agencyIdListValueManifestId, Function.identity()));

        findUserNameMap = repositoryFactory.scoreUserQueryRepository().getScoreUsers().stream()
                .collect(Collectors.toMap(e -> e.userId(), e -> e.username()));
        findReleaseMap = repositoryFactory.releaseQueryRepository(requester).getReleaseDetailsList().stream()
                .collect(Collectors.toMap(e -> e.releaseId(), Function.identity()));

        var ctxSchemeQuery = repositoryFactory.contextSchemeQueryRepository(requester);
        findContextSchemeMap = ctxSchemeQuery.getContextSchemeSummaryList().stream()
                .collect(Collectors.toMap(e -> e.contextSchemeId(), Function.identity()));
        var ctxCategoryQuery = repositoryFactory.contextCategoryQueryRepository(requester);
        findContextCategoryMap = ctxCategoryQuery.getContextCategorySummaryList().stream()
                .collect(Collectors.toMap(e -> e.contextCategoryId(), Function.identity()));

        var namespaceQuery = repositoryFactory.namespaceQueryRepository(requester);
        findNamespaceMap = namespaceQuery.getNamespaceSummaryList().stream()
                .collect(Collectors.toMap(e -> e.namespaceId(), Function.identity()));
    }

    private static BigInteger BASE_ID_FOR_DUMMIES = BigInteger.valueOf(100000000L);

    public TopLevelAsbiepSummaryRecord findTopLevelAsbiep(TopLevelAsbiepId topLevelAsbiepId) {
        return this.topLevelAsbiepMap.get(topLevelAsbiepId);
    }

    public DtAwdPriSummaryRecord findDtAwdPriByBbieAndDefaultIsTrue(BbieSummaryRecord bbie) {
        BccSummaryRecord bcc = getBcc(bbie.basedBccManifestId());
        BccpSummaryRecord bccp = getBccp(bcc.toBccpManifestId());
        return findDtAwdPriByDtManifestIdAndDefaultIsTrue(bccp.dtManifestId());
    }

    public DtAwdPriSummaryRecord findDtAwdPriByDtManifestIdAndDefaultIsTrue(DtManifestId bdtManifestId) {
        return getDtAwdPriList(bdtManifestId).stream().filter(e -> e.isDefault()).findFirst().orElse(null);
    }

    public DtScAwdPriSummaryRecord findDtScAwdPriByBbieScAndDefaultIsTrue(BbieScSummaryRecord bbieSc) {
        DtScSummaryRecord dtSc = getDtSc(bbieSc.basedDtScManifestId());
        return findDtScAwdPriByDtScManifestIdAndDefaultIsTrue(dtSc.dtScManifestId());
    }

    public DtScAwdPriSummaryRecord findDtScAwdPriByDtScManifestIdAndDefaultIsTrue(DtScManifestId bdtScManifestId) {
        return getDtScAwdPriList(bdtScManifestId).stream().filter(e -> e.isDefault()).findFirst().orElse(null);
    }

    public XbtSummaryRecord findXbtByName(String name) {
        if (name != null) {
            List<XbtSummaryRecord> xbtList = getXbtListByName(name);
            if (xbtList != null && !xbtList.isEmpty()) {
                return xbtList.get(0);
            }
        }
        return null;
    }

    public List<CodeListValueSummaryRecord> findCodeListValueByCodeListManifestId(CodeListManifestId codeListManifestId) {
        CodeListSummaryRecord codeList = findCodeListMap.get(findCodeListMap);
        return (codeList != null) ? codeList.valueList() : Collections.emptyList();
    }

    public AgencyIdListValueSummaryRecord findAgencyIdListValue(AgencyIdListValueManifestId agencyIdListValueManifestId) {
        return (agencyIdListValueManifestId != null) ? findAgencyIdListValueMap.get(agencyIdListValueManifestId) : null;
    }

    public List<AgencyIdListValueSummaryRecord> findAgencyIdListValueByAgencyIdListManifestId(AgencyIdListManifestId agencyIdListManifestId) {
        AgencyIdListSummaryRecord agencyIdList = findAgencyIdListMap.get(agencyIdListManifestId);
        return (agencyIdList != null) ? agencyIdList.valueList() : Collections.emptyList();
    }

    public AbieSummaryRecord findAbie(AbieId abieId, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (abieId == null) {
            return null;
        }
        if (abieId.value().compareTo(BASE_ID_FOR_DUMMIES) == 1) {
            AccSummaryRecord acc = getAcc(new AccManifestId(abieId.value().subtract(BASE_ID_FOR_DUMMIES)));

            return AbieSummaryRecord.builder(
                            new AbieId(BASE_ID_FOR_DUMMIES.add(acc.accManifestId().value())),
                            acc.accManifestId(),
                            topLevelAsbiep.state(),
                            topLevelAsbiep.topLevelAsbiepId(),
                            topLevelAsbiep.owner(), topLevelAsbiep.created(), topLevelAsbiep.lastUpdated())
                    .build();
        } else {
            return findAbieMap.get(abieId);
        }
    }

    public List<BbieScSummaryRecord> findBbieScByBbieIdAndUsedIsTrue(BbieSummaryRecord bbie) {
        if (bbie == null) {
            return Collections.emptyList();
        }

        BbieId bbieId = bbie.bbieId();
        List<BbieScSummaryRecord> storedBbieScSummaryRecords = findBbieScByBbieIdAndUsedIsTrueMap.containsKey(bbieId) ?
                findBbieScByBbieIdAndUsedIsTrueMap.get(bbieId) :
                Collections.emptyList();

        TopLevelAsbiepSummaryRecord topLevelAsbiep = this.topLevelAsbiepMap.get(bbie.ownerTopLevelAsbiepId());
        if (!topLevelAsbiep.inverseMode()) {
            return storedBbieScSummaryRecords;
        }

        Map<DtScManifestId, BbieScSummaryRecord> storedBbieScMap = storedBbieScSummaryRecords.stream()
                .collect(Collectors.toMap(BbieScSummaryRecord::basedDtScManifestId, Function.identity()));

        BccSummaryRecord bcc = getBcc(bbie.basedBccManifestId());
        BccpSummaryRecord bccp = getBccp(bcc.toBccpManifestId());
        DtSummaryRecord bdt = getDt(bccp.dtManifestId());
        List<DtScSummaryRecord> dtScList = getDtScList(bdt.dtManifestId());
        if (dtScList == null) {
            dtScList = Collections.emptyList();
        }
        dtScList = dtScList.stream().filter(e -> e.cardinality().max() > 0).collect(Collectors.toList());

        List<BbieScSummaryRecord> bbieScList = new ArrayList();
        for (DtScSummaryRecord dtSc : dtScList) {
            if (storedBbieScMap.containsKey(dtSc.dtScManifestId())) {
                bbieScList.add(storedBbieScMap.get(dtSc.dtScManifestId()));
            } else {
                DtScAwdPriSummaryRecord dtScAwdPri =
                        findDtScAwdPriByDtScManifestIdAndDefaultIsTrue(dtSc.dtScManifestId());
                BbieScSummaryRecord dummyBbieSc = BbieScSummaryRecord.builder(
                                new BbieScId(BASE_ID_FOR_DUMMIES.add(dtSc.dtScManifestId().value())),
                                dtSc.dtScManifestId(),
                                bbieId,
                                topLevelAsbiep.state(),
                                topLevelAsbiep.topLevelAsbiepId(),
                                topLevelAsbiep.owner(), topLevelAsbiep.created(), topLevelAsbiep.lastUpdated())
                        .primitiveRestriction(PrimitiveRestriction.fromDtScAwdPri(dtScAwdPri))
                        .cardinality(dtSc.cardinality())
                        .valueConstraint(dtSc.valueConstraint())
                        .used(topLevelAsbiep.inverseMode())
                        .build();
                bbieScList.add(dummyBbieSc);
            }
        }

        return bbieScList;
    }

    private List<AsccSummaryRecord> findAsccListByAccManifestId(AccManifestId accManifestId) {

        List<AsccSummaryRecord> asccList = getAsccListByFromAccManifestId(accManifestId);
        for (AsccSummaryRecord ascc : asccList) {
            AsccpSummaryRecord asccp = getAsccp(ascc.toAsccpManifestId());
            AccSummaryRecord acc = getAcc(asccp.roleOfAccManifestId());
            OagisComponentType componentType = acc.componentType();
            if (componentType.isGroup()) {
                asccList.addAll(findAsccListByAccManifestId(acc.accManifestId()));
            } else {
                asccList.add(ascc);
            }
        }
        return asccList;
    }

    public AsbiepSummaryRecord findASBIEP(AsbiepId asbiepId, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (asbiepId == null) {
            return null;
        }
        if (asbiepId.value().compareTo(BASE_ID_FOR_DUMMIES) == 1) {
            AsccpSummaryRecord asccp = getAsccp(new AsccpManifestId(asbiepId.value().subtract(BASE_ID_FOR_DUMMIES)));
            AccSummaryRecord roleOfAcc = getAcc(asccp.roleOfAccManifestId());

            return AsbiepSummaryRecord.builder(
                            new AsbiepId(BASE_ID_FOR_DUMMIES.add(asccp.asccpManifestId().value())),
                            asccp.asccpManifestId(),
                            new AbieId(BASE_ID_FOR_DUMMIES.add(roleOfAcc.accManifestId().value())),
                            topLevelAsbiep.state(),
                            topLevelAsbiep.topLevelAsbiepId(),
                            topLevelAsbiep.owner(), topLevelAsbiep.created(), topLevelAsbiep.lastUpdated())
                    .build();
        } else {
            return findASBIEPMap.get(asbiepId);
        }
    }

    public AsbiepSummaryRecord findAsbiepByRoleOfAbieId(AbieId roleOfAbieId) {
        return (roleOfAbieId != null) ? findAsbiepByRoleOfAbieIdMap.get(roleOfAbieId) : null;
    }

    public BbiepSummaryRecord findBBIEP(BbiepId bbiepId, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (bbiepId == null) {
            return null;
        }
        if (bbiepId.value().compareTo(BASE_ID_FOR_DUMMIES) == 1) {
            BccpSummaryRecord bccp = getBccp(new BccpManifestId(bbiepId.value().subtract(BASE_ID_FOR_DUMMIES)));

            return BbiepSummaryRecord.builder(
                            new BbiepId(BASE_ID_FOR_DUMMIES.add(bccp.bccpManifestId().value())),
                            bccp.bccpManifestId(),
                            topLevelAsbiep.state(),
                            topLevelAsbiep.topLevelAsbiepId(),
                            topLevelAsbiep.owner(), topLevelAsbiep.created(), topLevelAsbiep.lastUpdated())
                    .build();
        } else {
            return findBBIEPMap.get(bbiepId);
        }
    }

    public String findUserName(UserId userId) {
        return (userId != null) ? findUserNameMap.get(userId) : null;
    }

    public ReleaseDetailsRecord findRelease(ReleaseId releaseId) {
        return (releaseId != null) ? findReleaseMap.get(releaseId) : null;
    }

    public String findReleaseNumber(ReleaseId releaseId) {
        ReleaseDetailsRecord release = findRelease(releaseId);
        return (release != null) ? release.releaseNum() : null;
    }

    public AccSummaryRecord queryBasedACC(AbieSummaryRecord abie) {
        return (abie != null) ? getAcc(abie.basedAccManifestId()) : null;
    }

    private List<CcAssociation> findChildren(AccManifestId accManifestId) {
        AccSummaryRecord acc = getAcc(accManifestId);
        List<CcAssociation> sorted = new ArrayList();
        if (acc == null) {
            throw new IllegalArgumentException();
        }
        if (acc.basedAccManifestId() != null) {
            sorted.addAll(findChildren(acc.basedAccManifestId()));
        }

        ccDocument.getAssociationListByFromAccManifestId(acc.accManifestId())
                .forEach(e -> sorted.add((CcAssociation) e));

        return sorted;
    }

    private List<CcAssociation> loadGroupAssociations(List<CcAssociation> seqKeyList) {
        List<CcAssociation> assocs = new ArrayList();
        seqKeyList.forEach(seqKey -> {
            List<CcAssociation> groupAsso = getGroupAssociations(seqKey);
            if (groupAsso.size() > 1) {
                assocs.addAll(loadGroupAssociations(groupAsso));
            } else {
                assocs.addAll(groupAsso);
            }
        });
        return assocs;
    }

    private List<CcAssociation> getGroupAssociations(CcAssociation seqKey) {
        if (seqKey.isAscc()) {
            AsccpSummaryRecord asccp = getAsccp(((AsccSummaryRecord) seqKey).toAsccpManifestId());
            if (asccp != null) {
                AccSummaryRecord acc = getAcc(asccp.roleOfAccManifestId());
                if (acc.isGroup()) {
                    return findChildren(acc.accManifestId());
                }
            }
        }
        return Collections.singletonList(seqKey);
    }

    private List<BIE> sorted(AbieSummaryRecord abie,
                             List<AsbieSummaryRecord> asbieList, List<BbieSummaryRecord> bbieList) {

        List<BIE> sorted = new ArrayList();
        List<CcAssociation> assocs = findChildren(abie.basedAccManifestId());

        Map<String, BIE> bieMap = asbieList.stream().collect(Collectors.toMap(e -> "ASCC-" + e.basedAsccManifestId(), Function.identity()));
        bieMap.putAll(bbieList.stream().collect(Collectors.toMap(e -> "BCC-" + e.basedBccManifestId(), Function.identity())));

        loadGroupAssociations(assocs).forEach(e -> {
            String key = (e.isAscc()) ? ("ASCC-" + ((AsccSummaryRecord) e).asccManifestId()) : ("BCC-" + ((BccSummaryRecord) e).bccManifestId());
            BIE bie = bieMap.get(key);
            if (bie != null) {
                sorted.add(bie);
            }
        });
        return sorted;
    }

    // Get only Child BIEs whose is_used flag is true
    public List<BIE> queryChildBIEs(AbieSummaryRecord abie) {
        if (abie == null) {
            return Collections.emptyList();
        }

        TopLevelAsbiepSummaryRecord topLevelAsbiep = this.topLevelAsbiepMap.get(abie.ownerTopLevelAsbiepId());

        List<BIE> result = new ArrayList();
        List<BIE> assocs = findAssociationBIEs(abie);
        for (BIE bie : assocs) {
            if (bie instanceof BbieSummaryRecord) {
                BbieSummaryRecord bbie = (BbieSummaryRecord) bie;
                if (bbie.cardinality().max() != 0) {
                    result.add(bbie);
                }
            } else {
                AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;
                AsbiepSummaryRecord toAsbiep = findASBIEP(asbie.toAsbiepId(), topLevelAsbiep);
                AbieSummaryRecord roleOfAbie = findAbie(toAsbiep.roleOfAbieId(), topLevelAsbiep);
                AccSummaryRecord roleOfAcc = getAcc(roleOfAbie.basedAccManifestId());

                OagisComponentType oagisComponentType = roleOfAcc.componentType();
                if (oagisComponentType.isGroup()) {
                    result.addAll(queryChildBIEs(roleOfAbie));
                } else if (asbie.used() && asbie.cardinality().max() != 0) {
                    result.add(asbie);
                }
            }
        }

        return result;
    }

    public List<BIE> findAssociationBIEs(AbieSummaryRecord abie) {
        if (abie == null) {
            return Collections.emptyList();
        }

        AbieId fromAbieId = abie.abieId();
        List<AsbieSummaryRecord> storedASBIEs = findAsbieByFromAbieIdMap.containsKey(fromAbieId) ?
                findAsbieByFromAbieIdMap.get(fromAbieId) :
                Collections.emptyList();
        List<BbieSummaryRecord> storedBBIEs = findBbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                findBbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                Collections.emptyList();

        TopLevelAsbiepSummaryRecord topLevelAsbiep = this.topLevelAsbiepMap.get(abie.ownerTopLevelAsbiepId());
        if (!topLevelAsbiep.inverseMode()) {
            return sorted(abie,
                    storedASBIEs.stream().filter(e -> e.used()).collect(Collectors.toList()),
                    storedBBIEs.stream().filter(e -> e.used()).collect(Collectors.toList()));
        }

        Map<AsccManifestId, AsbieSummaryRecord> storedAsbieMap = storedASBIEs.stream()
                .collect(Collectors.toMap(AsbieSummaryRecord::basedAsccManifestId, Function.identity()));
        Map<BccManifestId, BbieSummaryRecord> storedBbieMap = storedBBIEs.stream()
                .collect(Collectors.toMap(BbieSummaryRecord::basedBccManifestId, Function.identity()));

        AccSummaryRecord acc = getAcc(abie.basedAccManifestId());
        List<CcAssociation> associations = findAssociationCCs(acc);

        List<AsbieSummaryRecord> asbieList = new ArrayList();
        List<BbieSummaryRecord> bbieList = new ArrayList();
        for (CcAssociation association : associations) {
            if (association instanceof AsccSummaryRecord) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) association;
                if (storedAsbieMap.containsKey(ascc.asccManifestId())) {
                    asbieList.add(storedAsbieMap.get(ascc.asccManifestId()));
                } else {
                    AsccpSummaryRecord toAsccp = getAsccp(ascc.toAsccpManifestId());
                    AsbieSummaryRecord dummyAsbie = AsbieSummaryRecord.builder(
                                    new AsbieId(BASE_ID_FOR_DUMMIES.add(ascc.asccManifestId().value())),
                                    ascc.asccManifestId(),
                                    fromAbieId,
                                    new AsbiepId(BASE_ID_FOR_DUMMIES.add(toAsccp.asccpManifestId().value())),
                                    topLevelAsbiep.state(),
                                    topLevelAsbiep.topLevelAsbiepId(),
                                    topLevelAsbiep.owner(), topLevelAsbiep.created(), topLevelAsbiep.lastUpdated())
                            .cardinality(ascc.cardinality())
                            .used(topLevelAsbiep.inverseMode())
                            .build();

                    asbieList.add(dummyAsbie);
                }
            } else {
                BccSummaryRecord bcc = (BccSummaryRecord) association;
                if (storedBbieMap.containsKey(bcc.bccManifestId())) {
                    bbieList.add(storedBbieMap.get(bcc.bccManifestId()));
                } else {
                    BccpSummaryRecord toBccp = getBccp(bcc.toBccpManifestId());
                    DtAwdPriSummaryRecord dtAwdPri =
                            findDtAwdPriByDtManifestIdAndDefaultIsTrue(toBccp.dtManifestId());
                    BbieSummaryRecord dummyBbie = BbieSummaryRecord.builder(
                                    new BbieId(BASE_ID_FOR_DUMMIES.add(bcc.bccManifestId().value())),
                                    bcc.bccManifestId(),
                                    fromAbieId,
                                    new BbiepId(BASE_ID_FOR_DUMMIES.add(toBccp.bccpManifestId().value())),
                                    topLevelAsbiep.state(),
                                    topLevelAsbiep.topLevelAsbiepId(),
                                    topLevelAsbiep.owner(), topLevelAsbiep.created(), topLevelAsbiep.lastUpdated())
                            .cardinality(bcc.cardinality())
                            .primitiveRestriction(PrimitiveRestriction.fromDtAwdPri(dtAwdPri))
                            .valueConstraint(toBccp.valueConstraint())
                            .nillable(toBccp.nillable())
                            .used(topLevelAsbiep.inverseMode())
                            .build();
                    bbieList.add(dummyBbie);
                }
            }
        }

        return sorted(abie,
                asbieList.stream().filter(e -> e.used()).collect(Collectors.toList()),
                bbieList.stream().filter(e -> e.used()).collect(Collectors.toList()));
    }

    private List<CcAssociation> findAssociationCCs(AccSummaryRecord acc) {
        List<CcAssociation> ccList = new ArrayList();
        Stack<AccSummaryRecord> accStack = new Stack();

        while (acc != null) {
            accStack.push(acc);
            if (acc.basedAccManifestId() == null) {
                break;
            }
            acc = getAcc(acc.basedAccManifestId());
        }
        while (!accStack.isEmpty()) {
            acc = accStack.pop();

            AccManifestId accManifestId = acc.accManifestId();

            for (AsccSummaryRecord ascc : getAsccListByFromAccManifestId(accManifestId)) {
                AsccpSummaryRecord asccp = getAsccp(ascc.toAsccpManifestId());
                AccSummaryRecord roleOfAcc = getAcc(asccp.roleOfAccManifestId());
                OagisComponentType componentType = roleOfAcc.componentType();
                if (componentType.isGroup()) {
                    ccList.addAll(findAssociationCCs(roleOfAcc));
                } else {
                    ccList.add(ascc);
                }
            }
            ccList.addAll(getBccListByFromAccManifestId(accManifestId));
        }
        return ccList;
    }

    // Get only SCs whose is_used is true.
    public List<BbieScSummaryRecord> queryBBIESCs(BbieSummaryRecord bbie) {
        return (bbie != null) ? findBbieScByBbieIdAndUsedIsTrue(bbie) : Collections.emptyList();
    }

    public AsbiepSummaryRecord receiveASBIEP(AbieSummaryRecord abie) {
        return (abie != null) ? findAsbiepByRoleOfAbieId(abie.abieId()) : null;
    }

    public DtSummaryRecord queryBDT(BbieSummaryRecord bbie) {
        BccSummaryRecord bcc = (bbie != null) ? getBcc(bbie.basedBccManifestId()) : null;
        BccpSummaryRecord bccp = (bcc != null) ? queryToBCCP(bcc) : null;
        return (bccp != null) ? queryBDT(bccp) : null;
    }

    public DtSummaryRecord queryBDT(BccpSummaryRecord bccp) {
        return (bccp != null) ? getDt(bccp.dtManifestId()) : null;
    }

    public AsccpSummaryRecord queryBasedASCCP(AsbiepSummaryRecord asbiep) {
        return (asbiep != null) ? getAsccp(asbiep.basedAsccpManifestId()) : null;
    }

    public AsccSummaryRecord queryBasedASCC(AsbieSummaryRecord asbie) {
        return (asbie != null) ? getAscc(asbie.basedAsccManifestId()) : null;
    }

    public AbieSummaryRecord queryTargetABIE(AsbiepSummaryRecord asbiep) {
        if (asbiep == null) {
            return null;
        }

        TopLevelAsbiepSummaryRecord topLevelAsbiep = this.topLevelAsbiepMap.get(asbiep.ownerTopLevelAsbiepId());
        return findAbie(asbiep.roleOfAbieId(), topLevelAsbiep);
    }

    public AccSummaryRecord queryTargetACC(AsbiepSummaryRecord asbiep) {
        if (asbiep == null) {
            return null;
        }

        AbieSummaryRecord abie = queryTargetABIE(asbiep);
        return (abie != null) ? getAcc(abie.basedAccManifestId()) : null;
    }

    public BccSummaryRecord queryBasedBCC(BbieSummaryRecord bbie) {
        return (bbie != null) ? getBcc(bbie.basedBccManifestId()) : null;
    }

    public BccpSummaryRecord queryToBCCP(BccSummaryRecord bcc) {
        return (bcc != null) ? getBccp(bcc.toBccpManifestId()) : null;
    }

    public CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        return (codeListManifestId != null) ? findCodeListMap.get(codeListManifestId) : null;
    }

    public CodeListSummaryRecord getCodeList(BbieScSummaryRecord bbieSc) {
        if (bbieSc == null) {
            return null;
        }

        CodeListSummaryRecord codeList = getCodeList(bbieSc.primitiveRestriction().codeListManifestId());
        if (codeList != null) {
            return codeList;
        }

        DtScAwdPriSummaryRecord dtScAwdPri = getDtScAwdPriList(bbieSc.basedDtScManifestId()).stream()
                .filter(e -> e.isDefault())
                .filter(e -> e.codeListManifestId() != null).findAny().orElse(null);
        return (dtScAwdPri != null) ? getCodeList(dtScAwdPri.codeListManifestId()) : null;
    }

    public AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        return (agencyIdListManifestId != null) ? findAgencyIdListMap.get(agencyIdListManifestId) : null;
    }

    public AgencyIdListSummaryRecord getAgencyIdList(BbieSummaryRecord bbie) {
        if (bbie == null) {
            return null;
        }
        return getAgencyIdList(bbie.primitiveRestriction().agencyIdListManifestId());
    }

    public AgencyIdListSummaryRecord getAgencyIdList(BbieScSummaryRecord bbieSc) {
        if (bbieSc == null) {
            return null;
        }
        AgencyIdListSummaryRecord agencyIdList = getAgencyIdList(bbieSc.primitiveRestriction().agencyIdListManifestId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        DtScAwdPriSummaryRecord dtScAwdPri = getDtScAwdPriList(bbieSc.basedDtScManifestId()).stream()
                .filter(e -> e.isDefault())
                .filter(e -> e.agencyIdListManifestId() != null).findAny().orElse(null);
        return (dtScAwdPri != null) ? getAgencyIdList(dtScAwdPri.agencyIdListManifestId()) : null;
    }

    public List<CodeListValueSummaryRecord> getCodeListValues(CodeListSummaryRecord codeList) {
        return (codeList != null) ? codeList.valueList() : Collections.emptyList();
    }

    public AsbiepSummaryRecord queryAssocToASBIEP(AsbieSummaryRecord asbie) {
        if (asbie == null) {
            return null;
        }
        TopLevelAsbiepSummaryRecord topLevelAsbiep = this.topLevelAsbiepMap.get(asbie.ownerTopLevelAsbiepId());
        return findASBIEP(asbie.toAsbiepId(), topLevelAsbiep);
    }

    public DtSummaryRecord queryAssocBDT(BbieSummaryRecord bbie) {
        BccSummaryRecord bcc = (bbie != null) ? getBcc(bbie.basedBccManifestId()) : null;
        BccpSummaryRecord bccp = (bcc != null) ? queryToBCCP(bcc) : null;
        return queryBDT(bccp);
    }

    public List<BusinessContextSummaryRecord> findBusinessContexts(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        var bizCtxQuery = repositoryFactory.businessContextQueryRepository(requester);
        return bizCtxQuery.getBusinessContextSummaryList(topLevelAsbiep.topLevelAsbiepId());
    }

    public List<ContextSchemeValueSummaryRecord> findContextSchemeValue(BusinessContextSummaryRecord businessContext) {
        if (businessContext == null) {
            return Collections.emptyList();
        }

        var bizCtxQuery = repositoryFactory.businessContextQueryRepository(requester);
        List<BusinessContextValueRecord> businessContextValues = bizCtxQuery.getBusinessContextValueList(businessContext.businessContextId());

        var ctxSchemeQuery = repositoryFactory.contextSchemeQueryRepository(requester);
        return businessContextValues.stream()
                .map(e -> ctxSchemeQuery.getContextSchemeValueSummary(e.contextSchemeValueId()))
                .collect(Collectors.toList());
    }

    public ContextSchemeSummaryRecord findContextScheme(ContextSchemeId ctxSchemeId) {
        return (ctxSchemeId != null) ? findContextSchemeMap.get(ctxSchemeId) : null;
    }

    public ContextCategorySummaryRecord findContextCategory(ContextCategoryId ctxCategoryId) {
        return (ctxCategoryId != null) ? findContextCategoryMap.get(ctxCategoryId) : null;
    }

    public AgencyIdListSummaryRecord findAgencyIdList(ContextSchemeSummaryRecord contextScheme) {
        String schemeAgencyId = contextScheme.schemeAgencyId();
        if (!StringUtils.hasLength(schemeAgencyId)) {
            return null;
        }

        for (AgencyIdListSummaryRecord agencyIdList : findAgencyIdListMap.values()) {
            if (schemeAgencyId.equals(agencyIdList.listId())) {
                return agencyIdList;
            }
        }

        return null;
    }

    public NamespaceSummaryRecord findNamespace(NamespaceId namespaceId) {
        return findNamespaceMap.get(namespaceId);
    }

    class ValueComparator implements Comparator<BIE> {

        Map<BIE, Double> base;

        public ValueComparator(Map<BIE, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(BIE a, BIE b) {
            if (base.get(a) <= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    private AsbiepReferenceCounter referenceCounter;

    public AsbiepReferenceCounter referenceCounter() {
        if (referenceCounter == null) {
            referenceCounter = new AsbiepReferenceCounter(this);
        }
        return referenceCounter;
    }

}
