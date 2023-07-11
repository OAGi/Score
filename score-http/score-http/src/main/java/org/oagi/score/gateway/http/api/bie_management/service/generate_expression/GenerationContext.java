package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.oagi.score.data.*;
import org.oagi.score.gateway.http.api.namespace_management.data.NamespaceList;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.component.asbiep.AsbiepReadRepository;
import org.oagi.score.repo.component.namespace.NamespaceReadRepository;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.repo.component.top_level_asbiep.TopLevelAsbiepReadRepository;
import org.oagi.score.repository.*;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.corecomponent.seqkey.SeqKeyHandler;
import org.oagi.score.service.corecomponent.seqkey.SeqKeySupportable;
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
public class GenerationContext implements InitializingBean {

    private final List<TopLevelAsbiep> topLevelAsbieps;
    private Map<BigInteger, TopLevelAsbiep> topLevelAsbiepMap;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Autowired
    private XbtRepository xbtRepository;

    @Autowired
    private CdtAwdPriXpsTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CdtScAwdPriXpsTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private DTRepository dataTypeRepository;

    @Autowired
    private DTSCRepository dtScRepository;

    @Autowired
    private BdtPriRestriRepository bdtPriRestriRepository;

    @Autowired
    private BdtScPriRestriRepository bdtScPriRestriRepository;

    @Autowired
    private ACCRepository accRepository;

    @Autowired
    private ASCCPRepository asccpRepository;

    @Autowired
    private BCCPRepository bccpRepository;

    @Autowired
    private ASCCRepository asccRepository;

    @Autowired
    private BCCRepository bccRepository;

    @Autowired
    private ABIERepository abieRepository;

    @Autowired
    private ASBIEPRepository asbiepRepository;

    @Autowired
    private AsbiepReadRepository asbiepReadRepository;

    @Autowired
    private BBIEPRepository bbiepRepository;

    @Autowired
    private ASBIERepository asbieRepository;

    @Autowired
    private BBIERepository bbieRepository;

    @Autowired
    private BBIESCRepository bbieScRepository;

    @Autowired
    private BizCtxRepository bizCtxRepository;

    @Autowired
    private BizCtxValueRepository bizCtxValueRepository;

    @Autowired
    private CtxSchemeRepository ctxSchemeRepository;

    @Autowired
    private CtxSchemeValueRepository ctxSchemeValueRepository;

    @Autowired
    private CtxCategoryRepository ctxCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopLevelAsbiepReadRepository topLevelAsbiepReadRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private NamespaceReadRepository namespaceReadRepository;

    // Prepared Datas
    private Map<BigInteger, BigInteger> findBdtManifestIdByBbieIdMap;
    private Map<BigInteger, BdtPriRestri> findBdtPriRestriByBdtManifestIdAndDefaultIsTrueMap;
    private Map<BigInteger, BdtPriRestri> findBdtPriRestriMap;
    private Map<BigInteger, CdtAwdPriXpsTypeMap> findCdtAwdPriXpsTypeMapMap;

    private Map<BigInteger, BigInteger> findBdtScManifestIdByBbieScIdMap;
    private Map<BigInteger, BdtScPriRestri> findBdtScPriRestriByBdtScManifestIdAndDefaultIsTrueMap;
    private Map<BigInteger, BdtScPriRestri> findBdtScPriRestriMap;
    private Map<BigInteger, CdtScAwdPriXpsTypeMap> findCdtScAwdPriXpsTypeMapMap;
    private Map<BigInteger, List<Xbt>> findXbtMap;
    private Map<BigInteger, CodeList> findCodeListMap;
    private Map<BigInteger, List<CodeListValue>> findCodeListValueByCodeListManifestIdMap;
    private Map<BigInteger, ACC> findACCMap;
    private Map<BigInteger, BCC> findBCCMap;
    private Map<BigInteger, BCCP> findBCCPMap;
    private Map<BigInteger, List<BCC>> findBCCByFromAccIdMap;
    private Map<BigInteger, ASCC> findASCCMap;
    private Map<BigInteger, List<ASCC>> findASCCByFromAccIdMap;
    private Map<BigInteger, ASCCP> findASCCPMap;
    private Map<BigInteger, DT> findDTMap;
    private Map<BigInteger, DTSC> findDtScMap;
    private Map<BigInteger, List<DTSC>> findDtScByOwnerDtManifestIdMap;
    private Map<BigInteger, AgencyIdList> findAgencyIdListMap;
    private Map<BigInteger, AgencyIdListValue> findAgencyIdListValueByAgencyIdListValueManifestIdMap;
    private Map<BigInteger, List<AgencyIdListValue>> findAgencyIdListValueByAgencyIdListManifestIdMap;
    private Map<BigInteger, ABIE> findAbieMap;
    private Map<BigInteger, List<BBIE>> findBbieByFromAbieIdAndUsedIsTrueMap;
    private Map<BigInteger, List<BBIESC>>
            findBbieScByBbieIdAndUsedIsTrueMap;
    private Map<BigInteger, List<ASBIE>> findAsbieByFromAbieIdMap;
    private Map<BigInteger, ASBIEP> findASBIEPMap;
    private Map<BigInteger, ASBIEP> findAsbiepByRoleOfAbieIdMap;
    private Map<BigInteger, BBIEP> findBBIEPMap;
    private Map<BigInteger, String> findUserNameMap;
    private Map<BigInteger, Release> findReleaseMap;
    private Map<BigInteger, ContextScheme> findContextSchemeMap;
    private Map<BigInteger, ContextCategory> findContextCategoryMap;
    private Map<BigInteger, NamespaceList> findNamespaceMap;

    @Data
    @AllArgsConstructor
    public static class SeqKey implements SeqKeySupportable {
        private String key;
        private String state;
        private BigInteger seqKeyId;
        private BigInteger prevSeqKeyId;
        private BigInteger nextSeqKeyId;
        private BigInteger toAsccpManifestId;
    }

    public GenerationContext(TopLevelAsbiep topLevelAsbiep) {
        this(Arrays.asList(topLevelAsbiep));
    }

    public GenerationContext(List<TopLevelAsbiep> topLevelAsbieps) {
        this.topLevelAsbieps = topLevelAsbieps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (topLevelAsbieps == null) {
            throw new IllegalStateException("'topLevelAsbieps' parameter must not be null.");
        }

        Set<BigInteger> releaseIdSet = topLevelAsbieps.stream().map(e -> e.getReleaseId()).collect(Collectors.toSet());

        Set<TopLevelAsbiep> topLevelAsbiepSet = new HashSet();
        topLevelAsbiepSet.addAll(topLevelAsbieps);
        topLevelAsbiepSet.addAll(findRefTopLevelAsbieps(topLevelAsbiepSet));

        this.topLevelAsbiepMap = topLevelAsbiepSet.stream()
                .collect(Collectors.toMap(TopLevelAsbiep::getTopLevelAsbiepId, Function.identity()));

        init(topLevelAsbiepSet.stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toList()), releaseIdSet);
    }

    private Set<TopLevelAsbiep> findRefTopLevelAsbieps(Set<TopLevelAsbiep> topLevelAsbiepSet) {
        Set<TopLevelAsbiep> refTopLevelAsbiepSet = new HashSet();
        refTopLevelAsbiepSet.addAll(
                topLevelAsbiepReadRepository.findRefTopLevelAsbieps(
                        topLevelAsbiepSet.stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toSet())
                )
        );

        if (!refTopLevelAsbiepSet.isEmpty()) {
            refTopLevelAsbiepSet.addAll(findRefTopLevelAsbieps(refTopLevelAsbiepSet));
        }

        return refTopLevelAsbiepSet;
    }

    private void init(Collection<BigInteger> topLevelAsbiepIds, Collection<BigInteger> releaseIds) {
        if (releaseIds == null || releaseIds.isEmpty()) {
            throw new IllegalArgumentException("'releaseIds' parameter must not be null.");
        }

        List<BdtPriRestri> bdtPriRestriList = bdtPriRestriRepository.findAll();
        findBdtPriRestriByBdtManifestIdAndDefaultIsTrueMap = bdtPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toMap(e -> e.getBdtManifestId(), Function.identity()));
        findBdtPriRestriMap = bdtPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtPriRestriId(), Function.identity()));

        List<BdtScPriRestri> bdtScPriRestriList = bdtScPriRestriRepository.findAll();
        findBdtScPriRestriByBdtScManifestIdAndDefaultIsTrueMap = bdtScPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toMap(e -> e.getBdtScManifestId(), Function.identity()));
        findBdtScPriRestriMap = bdtScPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtScPriRestriId(), Function.identity()));

        List<CdtAwdPriXpsTypeMap> cdtAwdPriXpsTypeMapList = cdtAwdPriXpsTypeMapRepository.findAll();
        findCdtAwdPriXpsTypeMapMap = cdtAwdPriXpsTypeMapList.stream()
                .collect(Collectors.toMap(e -> e.getCdtAwdPriXpsTypeMapId(), Function.identity()));

        List<CdtScAwdPriXpsTypeMap> cdtScAwdPriXpsTypeMapList = cdtScAwdPriXpsTypeMapRepository.findAll();
        findCdtScAwdPriXpsTypeMapMap = cdtScAwdPriXpsTypeMapList.stream()
                .collect(Collectors.toMap(e -> e.getCdtScAwdPriXpsTypeMapId(), Function.identity()));

        List<Xbt> xbtList = xbtRepository.findAll();
        findXbtMap = xbtList.stream()
                .filter(e -> releaseIds.contains(e.getReleaseId()))
                .collect(Collectors.groupingBy(e -> e.getXbtId()));

        List<CodeList> codeLists = codeListRepository.findAllByReleaseIds(releaseIds);
        findCodeListMap = codeLists.stream()
                .collect(Collectors.toMap(e -> e.getCodeListManifestId(), Function.identity()));

        List<CodeListValue> codeListValues = codeListValueRepository.findAll();
        findCodeListValueByCodeListManifestIdMap = codeListValues.stream()
                .collect(Collectors.groupingBy(e -> e.getCodeListManifestId()));

        List<ACC> accList = accRepository.findAllByReleaseIds(releaseIds);
        findACCMap = accList.stream()
                .collect(Collectors.toMap(e -> e.getAccManifestId(), Function.identity()));

        List<BCC> bccList = bccRepository.findAllByReleaseIds(releaseIds);
        findBCCMap = bccList.stream()
                .collect(Collectors.toMap(e -> e.getBccManifestId(), Function.identity()));

        List<BCCP> bccpList = bccpRepository.findAllByReleaseIds(releaseIds);
        findBCCPMap = bccpList.stream()
                .collect(Collectors.toMap(e -> e.getBccpManifestId(), Function.identity()));

        List<ASCC> asccList = asccRepository.findAllByReleaseIds(releaseIds);
        findASCCMap = asccList.stream()
                .collect(Collectors.toMap(e -> e.getAsccManifestId(), Function.identity()));

        findASCCByFromAccIdMap = asccList.stream().collect(Collectors.groupingBy(e -> e.getFromAccManifestId()));
        findBCCByFromAccIdMap = bccList.stream().collect(Collectors.groupingBy(e -> e.getFromAccManifestId()));

        List<ASCCP> asccpList = asccpRepository.findAllByReleaseIds(releaseIds);
        findASCCPMap = asccpList.stream()
                .collect(Collectors.toMap(e -> e.getAsccpManifestId(), Function.identity()));

        List<DT> dataTypeList = dataTypeRepository.findAll();
        findDTMap = dataTypeList.stream()
                .filter(e -> releaseIds.contains(e.getReleaseId()))
                .collect(Collectors.toMap(e -> e.getDtManifestId(), Function.identity()));

        List<DTSC> dtScList = dtScRepository.findAllByReleaseIds(releaseIds);
        findDtScMap = dtScList.stream()
                .collect(Collectors.toMap(e -> e.getDtScManifestId(), Function.identity()));
        findDtScByOwnerDtManifestIdMap = dtScList.stream()
                .collect(Collectors.groupingBy(DTSC::getOwnerDtManifestId));

        List<AgencyIdList> agencyIdLists = agencyIdListRepository.findAllByReleaseIds(releaseIds);
        findAgencyIdListMap = agencyIdLists.stream()
                .collect(Collectors.toMap(e -> e.getAgencyIdListManifestId(), Function.identity()));

        List<AgencyIdListValue> agencyIdListValues = agencyIdListValueRepository.findAll();
        findAgencyIdListValueByAgencyIdListValueManifestIdMap = agencyIdListValues.stream()
                .collect(Collectors.toMap(e -> e.getAgencyIdListValueManifestId(), Function.identity()));
        findAgencyIdListValueByAgencyIdListManifestIdMap = agencyIdListValues.stream()
                .collect(Collectors.groupingBy(e -> e.getAgencyIdListManifestId()));

        List<ABIE> abieList = abieRepository.findByOwnerTopLevelAsbiepIds(topLevelAsbiepIds);
        findAbieMap = abieList.stream()
                .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));

        List<BBIE> bbieList =
                bbieRepository.findByOwnerTopLevelAsbiepIdsAndUsed(topLevelAsbiepIds, true);
        findBbieByFromAbieIdAndUsedIsTrueMap = bbieList.stream()
                .filter(e -> e.isUsed())
                .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

        List<BBIESC> bbieScList =
                bbieScRepository.findByOwnerTopLevelAsbiepIdsAndUsed(topLevelAsbiepIds, true);
        findBbieScByBbieIdAndUsedIsTrueMap = bbieScList.stream()
                .filter(e -> e.isUsed())
                .collect(Collectors.groupingBy(e -> e.getBbieId()));

        List<ASBIE> asbieList =
                asbieRepository.findByOwnerTopLevelAsbiepIds(topLevelAsbiepIds);
        findAsbieByFromAbieIdMap = asbieList.stream()
                .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

        List<ASBIEP> asbiepList =
                asbiepRepository.findByOwnerTopLevelAsbiepIds(topLevelAsbiepIds);
        findASBIEPMap = asbiepList.stream()
                .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
        findAsbiepByRoleOfAbieIdMap = asbiepList.stream()
                .collect(Collectors.toMap(e -> e.getRoleOfAbieId(), Function.identity()));

        List<BBIEP> bbiepList =
                bbiepRepository.findByOwnerTopLevelAsbiepIds(topLevelAsbiepIds);
        findBBIEPMap = bbiepList.stream()
                .collect(Collectors.toMap(e -> e.getBbiepId(), Function.identity()));

        findUserNameMap = userRepository.getUsernameMap();
        findReleaseMap = releaseRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getReleaseId(), Function.identity()));

        findContextSchemeMap = ctxSchemeRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getCtxSchemeId(), Function.identity()));
        findContextCategoryMap = ctxCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getCtxCategoryId(), Function.identity()));

        findNamespaceMap = namespaceReadRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getNamespaceId(), Function.identity()));
    }

    private static BigInteger BASE_ID_FOR_DUMMIES = BigInteger.valueOf(100000000L);

    public TopLevelAsbiep findTopLevelAsbiep(BigInteger topLevelAsbiepId) {
        return this.topLevelAsbiepMap.get(topLevelAsbiepId);
    }

    public BdtPriRestri findBdtPriRestriByBbieAndDefaultIsTrue(BBIE bbie) {
        BCC bcc = findBCC(bbie.getBasedBccManifestId());
        BCCP bccp = findBCCP(bcc.getToBccpManifestId());
        return findBdtPriRestriByBdtManifestIdAndDefaultIsTrue(bccp.getBdtManifestId());
    }

    public BdtPriRestri findBdtPriRestriByBdtManifestIdAndDefaultIsTrue(BigInteger bdtManifestId) {
        return (bdtManifestId != null && bdtManifestId.longValue() > 0L) ? findBdtPriRestriByBdtManifestIdAndDefaultIsTrueMap.get(bdtManifestId) : null;
    }

    public BdtPriRestri findBdtPriRestri(BigInteger bdtPriRestriId) {
        return (bdtPriRestriId != null && bdtPriRestriId.longValue() > 0L) ? findBdtPriRestriMap.get(bdtPriRestriId) : null;
    }

    public CdtAwdPriXpsTypeMap findCdtAwdPriXpsTypeMap(BigInteger cdtAwdPriXpsTypeMapId) {
        return (cdtAwdPriXpsTypeMapId != null && cdtAwdPriXpsTypeMapId.longValue() > 0L) ?
                findCdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId) : null;
    }

    public BdtScPriRestri findBdtScPriRestriByBbieScAndDefaultIsTrue(BBIESC bbieSc) {
        DTSC dtSc = findDtSc(bbieSc.getBasedDtScManifestId());
        return findBdtScPriRestriByBdtScManifestIdAndDefaultIsTrue(dtSc.getDtScManifestId());
    }

    public BdtScPriRestri findBdtScPriRestriByBdtScManifestIdAndDefaultIsTrue(BigInteger bdtScManifestId) {
        return (bdtScManifestId != null && bdtScManifestId.longValue() > 0L) ? findBdtScPriRestriByBdtScManifestIdAndDefaultIsTrueMap.get(bdtScManifestId) : null;
    }

    public BdtScPriRestri findBdtScPriRestri(BigInteger bdtScPriRestriId) {
        return (bdtScPriRestriId != null && bdtScPriRestriId.longValue() > 0L) ? findBdtScPriRestriMap.get(bdtScPriRestriId) : null;
    }

    public CdtScAwdPriXpsTypeMap findCdtScAwdPriXpsTypeMap(BigInteger cdtScAwdPriXpsTypeMapId) {
        return (cdtScAwdPriXpsTypeMapId != null && cdtScAwdPriXpsTypeMapId.longValue() > 0L) ?
                findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId) : null;
    }

    public Xbt findXbt(BigInteger xbtId) {
        if (xbtId != null && xbtId.longValue() > 0L) {
            List<Xbt> xbtList = findXbtMap.get(xbtId);
            if (xbtList != null && !xbtList.isEmpty()) {
                return xbtList.get(0);
            }
        }
        return null;
    }

    public CodeList findCodeList(BigInteger codeListManifestId) {
        return (codeListManifestId != null && codeListManifestId.longValue() > 0L) ? findCodeListMap.get(codeListManifestId) : null;
    }

    public List<CodeListValue> findCodeListValueByCodeListManifestId(BigInteger codeListManifestId) {
        return findCodeListValueByCodeListManifestIdMap.containsKey(codeListManifestId) ?
                findCodeListValueByCodeListManifestIdMap.get(codeListManifestId) :
                Collections.emptyList();
    }

    public ACC findACC(BigInteger accManifestId) {
        return (accManifestId != null && accManifestId.longValue() > 0L) ? findACCMap.get(accManifestId) : null;
    }

    public BCC findBCC(BigInteger bccManifestId) {
        return (bccManifestId != null && bccManifestId.longValue() > 0L) ? findBCCMap.get(bccManifestId) : null;
    }

    public BCCP findBCCP(BigInteger bccpManifestId) {
        return (bccpManifestId != null && bccpManifestId.longValue() > 0L) ? findBCCPMap.get(bccpManifestId) : null;
    }

    public ASCC findASCC(BigInteger asccManifestId) {
        return (asccManifestId != null && asccManifestId.longValue() > 0L) ? findASCCMap.get(asccManifestId) : null;
    }

    public ASCCP findASCCP(BigInteger asccpManifestId) {
        return (asccpManifestId != null && asccpManifestId.longValue() > 0L) ? findASCCPMap.get(asccpManifestId) : null;
    }

    public DT findDT(BigInteger dtManifestId) {
        return (dtManifestId != null && dtManifestId.longValue() > 0L) ? findDTMap.get(dtManifestId) : null;
    }

    public DTSC findDtSc(BigInteger dtScManifestId) {
        return (dtScManifestId != null && dtScManifestId.longValue() > 0L) ? findDtScMap.get(dtScManifestId) : null;
    }

    public AgencyIdList findAgencyIdList(BigInteger agencyIdListManifestId) {
        return (agencyIdListManifestId != null && agencyIdListManifestId.longValue() > 0L) ? findAgencyIdListMap.get(agencyIdListManifestId) : null;
    }

    public AgencyIdListValue findAgencyIdListValue(BigInteger agencyIdListValueManifestId) {
        return (agencyIdListValueManifestId != null && agencyIdListValueManifestId.longValue() > 0L) ? findAgencyIdListValueByAgencyIdListValueManifestIdMap.get(agencyIdListValueManifestId) : null;
    }

    public List<AgencyIdListValue> findAgencyIdListValueByAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        return findAgencyIdListValueByAgencyIdListManifestIdMap.containsKey(agencyIdListManifestId) ?
                findAgencyIdListValueByAgencyIdListManifestIdMap.get(agencyIdListManifestId) :
                Collections.emptyList();
    }

    public ABIE findAbie(BigInteger abieId, TopLevelAsbiep topLevelAsbiep) {
        if (abieId == null || abieId.longValue() <= 0L) {
            return null;
        }
        if (abieId.compareTo(BASE_ID_FOR_DUMMIES) == 1) {
            ACC acc = findACC(abieId.subtract(BASE_ID_FOR_DUMMIES));

            ABIE abie = new ABIE();
            abie.setAbieId(BASE_ID_FOR_DUMMIES.add(acc.getAccManifestId()));
            abie.setGuid(ScoreGuid.randomGuid());
            abie.setBasedAccManifestId(acc.getAccManifestId());
            abie.setCreatedBy(topLevelAsbiep.getLastUpdatedBy());
            abie.setLastUpdatedBy(topLevelAsbiep.getLastUpdatedBy());
            abie.setCreationTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
            abie.setLastUpdateTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
            abie.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());
            return abie;
        } else {
            return findAbieMap.get(abieId);
        }
    }

    public List<BBIESC> findBbieScByBbieIdAndUsedIsTrue(BBIE bbie) {
        if (bbie == null) {
            return Collections.emptyList();
        }

        BigInteger bbieId = bbie.getBbieId();
        List<BBIESC> storedBBIESCs =  findBbieScByBbieIdAndUsedIsTrueMap.containsKey(bbieId) ?
                findBbieScByBbieIdAndUsedIsTrueMap.get(bbieId) :
                Collections.emptyList();

        TopLevelAsbiep topLevelAsbiep = this.topLevelAsbiepMap.get(bbie.getOwnerTopLevelAsbiepId());
        if (!topLevelAsbiep.isInverseMode()) {
            return storedBBIESCs;
        }

        Map<BigInteger, BBIESC> storedBbieScMap = storedBBIESCs.stream()
                .collect(Collectors.toMap(BBIESC::getBasedDtScManifestId, Function.identity()));

        BCC bcc = findBCC(bbie.getBasedBccManifestId());
        BCCP bccp = findBCCP(bcc.getToBccpManifestId());
        DT bdt = findDT(bccp.getBdtManifestId());
        List<DTSC> dtScList = findDtScByOwnerDtManifestIdMap.get(bdt.getDtManifestId());
        if (dtScList == null) {
            dtScList = Collections.emptyList();
        }
        dtScList = dtScList.stream().filter(e -> e.getCardinalityMax() > 0).collect(Collectors.toList());

        List<BBIESC> bbieScList = new ArrayList();
        for (DTSC dtSc : dtScList) {
            if (storedBbieScMap.containsKey(dtSc.getDtScManifestId())) {
                bbieScList.add(storedBbieScMap.get(dtSc.getDtScManifestId()));
            } else {
                BBIESC dummyBbieSc = new BBIESC();
                dummyBbieSc.setBbieScId(BASE_ID_FOR_DUMMIES.add(dtSc.getDtScManifestId()));
                dummyBbieSc.setGuid(ScoreGuid.randomGuid());
                dummyBbieSc.setBbieId(bbieId);
                dummyBbieSc.setBasedDtScManifestId(dtSc.getDtScManifestId());

                BdtScPriRestri bdtScPriRestri =
                        findBdtScPriRestriByBdtScManifestIdAndDefaultIsTrue(dtSc.getDtScManifestId());
                dummyBbieSc.setDtScPriRestriId(bdtScPriRestri.getBdtScPriRestriId());

                dummyBbieSc.setCardinalityMin(dtSc.getCardinalityMin());
                dummyBbieSc.setCardinalityMax(dtSc.getCardinalityMax());
                dummyBbieSc.setDefaultValue(dtSc.getDefaultValue());
                dummyBbieSc.setFixedValue(dtSc.getFixedValue());
                dummyBbieSc.setUsed(topLevelAsbiep.isInverseMode());
                dummyBbieSc.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());
                bbieScList.add(dummyBbieSc);
            }
        }

        return bbieScList;
    }

    private List<ASCC> findAsccListByAccManifestId(BigInteger accManifestId) {
        if (!findASCCByFromAccIdMap.containsKey(accManifestId)) {
            return Collections.emptyList();
        }

        List<ASCC> asccList = new ArrayList();
        for (ASCC ascc : findASCCByFromAccIdMap.get(accManifestId)) {
            ASCCP asccp = findASCCP(ascc.getToAsccpManifestId());
            ACC acc = findACC(asccp.getRoleOfAccManifestId());
            OagisComponentType componentType = OagisComponentType.valueOf(acc.getOagisComponentType());
            if (componentType.isGroup()) {
                asccList.addAll(findAsccListByAccManifestId(acc.getAccManifestId()));
            } else {
                asccList.add(ascc);
            }
        }
        return asccList;
    }

    public ASBIEP findASBIEP(BigInteger asbiepId, TopLevelAsbiep topLevelAsbiep) {
        if (asbiepId == null || asbiepId.longValue() <= 0L) {
            return null;
        }
        if (asbiepId.compareTo(BASE_ID_FOR_DUMMIES) == 1) {
            ASCCP asccp = findASCCP(asbiepId.subtract(BASE_ID_FOR_DUMMIES));

            ASBIEP asbiep = new ASBIEP();
            asbiep.setAsbiepId(BASE_ID_FOR_DUMMIES.add(asccp.getAsccpManifestId()));
            asbiep.setGuid(ScoreGuid.randomGuid());
            asbiep.setBasedAsccpManifestId(asccp.getAsccpManifestId());
            ACC roleOfAcc = findACC(asccp.getRoleOfAccManifestId());
            asbiep.setRoleOfAbieId(BASE_ID_FOR_DUMMIES.add(roleOfAcc.getAccManifestId()));
            asbiep.setCreatedBy(topLevelAsbiep.getLastUpdatedBy());
            asbiep.setLastUpdatedBy(topLevelAsbiep.getLastUpdatedBy());
            asbiep.setCreationTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
            asbiep.setLastUpdateTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
            asbiep.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());
            return asbiep;
        } else {
            return findASBIEPMap.get(asbiepId);
        }
    }

    public ASBIEP findAsbiepByRoleOfAbieId(BigInteger roleOfAbieId) {
        return (roleOfAbieId != null && roleOfAbieId.longValue() > 0L) ? findAsbiepByRoleOfAbieIdMap.get(roleOfAbieId) : null;
    }

    public BBIEP findBBIEP(BigInteger bbiepId, TopLevelAsbiep topLevelAsbiep) {
        if (bbiepId == null || bbiepId.longValue() <= 0L) {
            return null;
        }
        if (bbiepId.compareTo(BASE_ID_FOR_DUMMIES) == 1) {
            BCCP bccp = findBCCP(bbiepId.subtract(BASE_ID_FOR_DUMMIES));

            BBIEP bbiep = new BBIEP();
            bbiep.setBbiepId(BASE_ID_FOR_DUMMIES.add(bccp.getBccpManifestId()));
            bbiep.setGuid(ScoreGuid.randomGuid());
            bbiep.setBasedBccpManifestId(bccp.getBccpManifestId());
            bbiep.setCreatedBy(topLevelAsbiep.getLastUpdatedBy());
            bbiep.setLastUpdatedBy(topLevelAsbiep.getLastUpdatedBy());
            bbiep.setCreationTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
            bbiep.setLastUpdateTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
            bbiep.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());
            return bbiep;
        } else {
            return findBBIEPMap.get(bbiepId);
        }
    }

    public String findUserName(BigInteger userId) {
        return (userId != null && userId.longValue() > 0L) ? findUserNameMap.get(userId) : null;
    }

    public Release findRelease(BigInteger releaseId) {
        return (releaseId != null && releaseId.longValue() > 0L) ? findReleaseMap.get(releaseId) : null;
    }

    public String findReleaseNumber(BigInteger releaseId) {
        Release release = findRelease(releaseId);
        return (release != null) ? release.getReleaseNum() : null;
    }

    public ACC queryBasedACC(ABIE abie) {
        return (abie != null) ? findACC(abie.getBasedAccManifestId()) : null;
    }

    private List<SeqKey> findChildren(BigInteger accManifestId) {
        ACC acc = findACCMap.get(accManifestId);
        List<SeqKey> sorted = new ArrayList();
        if (acc == null) {
            throw new IllegalArgumentException();
        }
        if (acc.getBasedAccManifestId() != null) {
            sorted.addAll(findChildren(acc.getBasedAccManifestId()));
        }

        List<SeqKeySupportable> asso = new ArrayList();

        List<ASCC> asccs = findASCCByFromAccIdMap.get(acc.getAccManifestId());
        if (asccs != null) {
            asccs.forEach(e -> asso.add(
                    new SeqKey("ASCC-" + e.getAsccManifestId(), e.getState().name(), e.getSeqKeyId(),
                            e.getPrevSeqKeyId(), e.getNextSeqKeyId(), e.getToAsccpManifestId())));
        }

        List<BCC> bccs = findBCCByFromAccIdMap.get(acc.getAccManifestId());
        if (bccs != null) {
            bccs.forEach(e -> asso.add(
                    new SeqKey("BCC-" + e.getBccManifestId(), e.getState().name(), e.getSeqKeyId(),
                            e.getPrevSeqKeyId(), e.getNextSeqKeyId(), null)
            ));
        }

        SeqKeyHandler.sort(asso).forEach(e -> sorted.add((SeqKey) e));

        return sorted;
    }

    private List<SeqKey> loadGroupAssociations(List<SeqKey> seqKeyList) {
        List<SeqKey> assocs = new ArrayList();
        seqKeyList.forEach(seqKey -> {
            List<SeqKey> groupAsso = getGroupAssociations(seqKey);
            if (groupAsso.size() > 1) {
                assocs.addAll(loadGroupAssociations(groupAsso));
            } else {
                assocs.addAll(groupAsso);
            }
        });
        return assocs;
    }

    private List<SeqKey> getGroupAssociations(SeqKey seqKey) {
        ASCCP asccp = findASCCP(seqKey.getToAsccpManifestId());
        if (asccp != null) {
            ACC acc = findACC(asccp.getRoleOfAccManifestId());
            if (OagisComponentType.valueOf(acc.getOagisComponentType()).isGroup()) {
                return findChildren(acc.getAccManifestId());
            }
        }
        return Collections.singletonList(seqKey);
    }

    private List<BIE> sorted(ABIE abie, List<ASBIE> asbieList, List<BBIE> bbieList) {

        List<BIE> sorted = new ArrayList();
        List<SeqKey> assocs = findChildren(abie.getBasedAccManifestId());

        Map<String, BIE> bieMap = asbieList.stream().collect(Collectors.toMap(e -> "ASCC-" + e.getBasedAsccManifestId(), Function.identity()));
        bieMap.putAll(bbieList.stream().collect(Collectors.toMap(e -> "BCC-" + e.getBasedBccManifestId(), Function.identity())));

        loadGroupAssociations(assocs).forEach(e -> {
            BIE bie = bieMap.get(e.key);
            if (bie != null) {
                sorted.add(bie);
            }
        });
        return sorted;
    }

    // Get only Child BIEs whose is_used flag is true
    public List<BIE> queryChildBIEs(ABIE abie) {
        if (abie == null) {
            return Collections.emptyList();
        }

        TopLevelAsbiep topLevelAsbiep = this.topLevelAsbiepMap.get(abie.getOwnerTopLevelAsbiepId());

        List<BIE> result = new ArrayList();
        for (BIE bie : findAssociationBIEs(abie)) {
            if (bie instanceof BBIE) {
                BBIE bbie = (BBIE) bie;
                if (bbie.getCardinalityMax() != 0) {
                    result.add(bbie);
                }
            } else {
                ASBIE asbie = (ASBIE) bie;
                ASBIEP toAsbiep = findASBIEP(asbie.getToAsbiepId(), topLevelAsbiep);
                ABIE roleOfAbie = findAbie(toAsbiep.getRoleOfAbieId(), topLevelAsbiep);
                ACC roleOfAcc = findACC(roleOfAbie.getBasedAccManifestId());

                OagisComponentType oagisComponentType = OagisComponentType.valueOf(roleOfAcc.getOagisComponentType());
                if (oagisComponentType.isGroup()) {
                    result.addAll(queryChildBIEs(roleOfAbie));
                } else if (asbie.isUsed() && asbie.getCardinalityMax() != 0) {
                    result.add(asbie);
                }
            }
        }

        return result;
    }

    public List<BIE> findAssociationBIEs(ABIE abie) {
        if (abie == null) {
            return Collections.emptyList();
        }

        BigInteger fromAbieId = abie.getAbieId();
        List<ASBIE> storedASBIEs = findAsbieByFromAbieIdMap.containsKey(fromAbieId) ?
                findAsbieByFromAbieIdMap.get(fromAbieId) :
                Collections.emptyList();
        List<BBIE> storedBBIEs = findBbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                findBbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                Collections.emptyList();

        TopLevelAsbiep topLevelAsbiep = this.topLevelAsbiepMap.get(abie.getOwnerTopLevelAsbiepId());
        if (!topLevelAsbiep.isInverseMode()) {
            return sorted(abie,
                    storedASBIEs.stream().filter(e -> e.isUsed()).collect(Collectors.toList()),
                    storedBBIEs.stream().filter(e -> e.isUsed()).collect(Collectors.toList()));
        }

        Map<BigInteger, ASBIE> storedAsbieMap = storedASBIEs.stream()
                .collect(Collectors.toMap(ASBIE::getBasedAsccManifestId, Function.identity()));
        Map<BigInteger, BBIE> storedBbieMap = storedBBIEs.stream()
                .collect(Collectors.toMap(BBIE::getBasedBccManifestId, Function.identity()));

        ACC acc = findACC(abie.getBasedAccManifestId());
        List<CoreComponent> associations = findAssociationCCs(acc);

        List<ASBIE> asbieList = new ArrayList();
        List<BBIE> bbieList = new ArrayList();
        for (CoreComponent association : associations) {
            if (association instanceof ASCC) {
                ASCC ascc = (ASCC) association;
                if (storedAsbieMap.containsKey(ascc.getAsccManifestId())) {
                    asbieList.add(storedAsbieMap.get(ascc.getAsccManifestId()));
                } else {
                    ASBIE dummyAsbie = new ASBIE();
                    dummyAsbie.setAsbieId(BASE_ID_FOR_DUMMIES.add(ascc.getAsccManifestId()));
                    dummyAsbie.setGuid(ScoreGuid.randomGuid());
                    dummyAsbie.setBasedAsccManifestId(ascc.getAsccManifestId());
                    dummyAsbie.setFromAbieId(fromAbieId);

                    ASCCP toAsccp = findASCCP(ascc.getToAsccpManifestId());
                    dummyAsbie.setToAsbiepId(BASE_ID_FOR_DUMMIES.add(toAsccp.getAsccpManifestId()));
                    dummyAsbie.setCardinalityMin(ascc.getCardinalityMin());
                    dummyAsbie.setCardinalityMax(ascc.getCardinalityMax());
                    dummyAsbie.setCreatedBy(topLevelAsbiep.getLastUpdatedBy());
                    dummyAsbie.setLastUpdatedBy(topLevelAsbiep.getLastUpdatedBy());
                    dummyAsbie.setCreationTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
                    dummyAsbie.setLastUpdateTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
                    dummyAsbie.setUsed(topLevelAsbiep.isInverseMode());
                    dummyAsbie.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());

                    asbieList.add(dummyAsbie);
                }
            } else {
                BCC bcc = (BCC) association;
                if (storedBbieMap.containsKey(bcc.getBccManifestId())) {
                    bbieList.add(storedBbieMap.get(bcc.getBccManifestId()));
                } else {
                    BBIE dummyBbie = new BBIE();
                    dummyBbie.setBbieId(BASE_ID_FOR_DUMMIES.add(bcc.getBccManifestId()));
                    dummyBbie.setGuid(ScoreGuid.randomGuid());
                    dummyBbie.setBasedBccManifestId(bcc.getBccManifestId());
                    dummyBbie.setFromAbieId(fromAbieId);

                    BCCP toBccp = findBCCP(bcc.getToBccpManifestId());
                    dummyBbie.setToBbiepId(BASE_ID_FOR_DUMMIES.add(toBccp.getBccpManifestId()));

                    BdtPriRestri bdtPriRestri =
                            findBdtPriRestriByBdtManifestIdAndDefaultIsTrue(toBccp.getBdtManifestId());
                    dummyBbie.setBdtPriRestriId(bdtPriRestri.getBdtPriRestriId());

                    dummyBbie.setCardinalityMin(bcc.getCardinalityMin());
                    dummyBbie.setCardinalityMax(bcc.getCardinalityMax());
                    dummyBbie.setDefaultValue(toBccp.getDefaultValue());
                    dummyBbie.setFixedValue(toBccp.getFixedValue());
                    dummyBbie.setNillable(toBccp.isNillable());
                    dummyBbie.setCreatedBy(topLevelAsbiep.getLastUpdatedBy());
                    dummyBbie.setLastUpdatedBy(topLevelAsbiep.getLastUpdatedBy());
                    dummyBbie.setCreationTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
                    dummyBbie.setLastUpdateTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
                    dummyBbie.setUsed(topLevelAsbiep.isInverseMode());
                    dummyBbie.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());

                    bbieList.add(dummyBbie);
                }
            }
        }

        return sorted(abie,
                asbieList.stream().filter(e -> e.isUsed()).collect(Collectors.toList()),
                bbieList.stream().filter(e -> e.isUsed()).collect(Collectors.toList()));
    }

    private List<CoreComponent> findAssociationCCs(ACC acc) {
        List<CoreComponent> ccList = new ArrayList();
        Stack<ACC> accStack = new Stack();

        while (acc != null) {
            accStack.push(acc);
            if (acc.getBasedAccManifestId() == null) {
                break;
            }
            acc = findACC(acc.getBasedAccManifestId());
        }
        while (!accStack.isEmpty()) {
            acc = accStack.pop();

            BigInteger accManifestId = acc.getAccManifestId();

            if (findASCCByFromAccIdMap.containsKey(accManifestId)) {
                for (ASCC ascc : findASCCByFromAccIdMap.get(accManifestId)) {
                    ASCCP asccp = findASCCP(ascc.getToAsccpManifestId());
                    ACC roleOfAcc = findACC(asccp.getRoleOfAccManifestId());
                    OagisComponentType componentType = OagisComponentType.valueOf(roleOfAcc.getOagisComponentType());
                    if (componentType.isGroup()) {
                        ccList.addAll(findAssociationCCs(roleOfAcc));
                    } else {
                        ccList.add(ascc);
                    }
                }
            }
            if (findBCCByFromAccIdMap.containsKey(accManifestId)) {
                ccList.addAll(findBCCByFromAccIdMap.get(accManifestId));
            }
        }
        return ccList;
    }

    // Get only SCs whose is_used is true.
    public List<BBIESC> queryBBIESCs(BBIE bbie) {
        return (bbie != null) ? findBbieScByBbieIdAndUsedIsTrue(bbie) : Collections.emptyList();
    }

    public ASBIEP receiveASBIEP(ABIE abie) {
        return (abie != null) ? findAsbiepByRoleOfAbieId(abie.getAbieId()) : null;
    }

    public DT queryBDT(BBIE bbie) {
        BCC bcc = (bbie != null) ? findBCC(bbie.getBasedBccManifestId()) : null;
        BCCP bccp = (bcc != null) ? queryToBCCP(bcc) : null;
        return (bccp != null) ? queryBDT(bccp) : null;
    }

    public DT queryBDT(BCCP bccp) {
        return (bccp != null) ? findDT(bccp.getBdtManifestId()) : null;
    }

    public ASCCP queryBasedASCCP(ASBIEP asbiep) {
        return (asbiep != null) ? findASCCP(asbiep.getBasedAsccpManifestId()) : null;
    }

    public ASCC queryBasedASCC(ASBIE asbie) {
        return (asbie != null) ? findASCC(asbie.getBasedAsccManifestId()) : null;
    }

    public ABIE queryTargetABIE(ASBIEP asbiep) {
        if (asbiep == null) {
            return null;
        }

        TopLevelAsbiep topLevelAsbiep = this.topLevelAsbiepMap.get(asbiep.getOwnerTopLevelAsbiepId());
        return findAbie(asbiep.getRoleOfAbieId(), topLevelAsbiep);
    }

    public ACC queryTargetACC(ASBIEP asbiep) {
        if (asbiep == null) {
            return null;
        }

        ABIE abie = queryTargetABIE(asbiep);
        return (abie != null) ? findACC(abie.getBasedAccManifestId()) : null;
    }

    public BCC queryBasedBCC(BBIE bbie) {
        return (bbie != null) ? findBCC(bbie.getBasedBccManifestId()) : null;
    }

    public BCCP queryToBCCP(BCC bcc) {
        return (bcc != null) ? findBCCP(bcc.getToBccpManifestId()) : null;
    }

    public CodeList getCodeList(BBIESC bbieSc) {
        if (bbieSc == null) {
            return null;
        }

        CodeList codeList = findCodeList(bbieSc.getCodeListManifestId());
        if (codeList != null) {
            return codeList;
        }

        BdtScPriRestri bdtScPriRestri = findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        if (bdtScPriRestri != null) {
            return findCodeList(bdtScPriRestri.getCodeListManifestId());
        } else {
            DTSC gDTSC = findDtSc(bbieSc.getBasedDtScManifestId());
            BdtScPriRestri bBDTSCPrimitiveRestriction =
                    (gDTSC != null) ? findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc) : null;
            if (bBDTSCPrimitiveRestriction != null) {
                codeList = findCodeList(bBDTSCPrimitiveRestriction.getCodeListManifestId());
            }
        }

        return codeList;
    }

    public AgencyIdList getAgencyIdList(BBIE bbie) {
        if (bbie == null) {
            return null;
        }
        AgencyIdList agencyIdList = findAgencyIdList(bbie.getAgencyIdListManifestId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        BdtPriRestri bdtPriRestri =
                findBdtPriRestri(bbie.getBdtPriRestriId());
        if (bdtPriRestri != null) {
            agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListManifestId());
        }

        if (agencyIdList == null) {
            bdtPriRestri = findBdtPriRestriByBbieAndDefaultIsTrue(bbie);
            if (bdtPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListManifestId());
            }
        }
        return agencyIdList;
    }

    public AgencyIdList getAgencyIdList(BBIESC bbieSc) {
        if (bbieSc == null) {
            return null;
        }
        AgencyIdList agencyIdList = findAgencyIdList(bbieSc.getAgencyIdListManifestId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        BdtScPriRestri bdtScPriRestri =
                findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        if (bdtScPriRestri != null) {
            agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListManifestId());
        }

        if (agencyIdList == null) {
            DTSC gDTSC = findDtSc(bbieSc.getBasedDtScManifestId());
            bdtScPriRestri = (gDTSC != null) ? findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc) : null;
            if (bdtScPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListManifestId());
            }
        }
        return agencyIdList;
    }

    public List<CodeListValue> getCodeListValues(CodeList codeList) {
        return (codeList != null) ?
                findCodeListValueByCodeListManifestId(codeList.getCodeListManifestId()) :
                Collections.emptyList();
    }

    public ASBIEP queryAssocToASBIEP(ASBIE asbie) {
        if (asbie == null) {
            return null;
        }
        TopLevelAsbiep topLevelAsbiep = this.topLevelAsbiepMap.get(asbie.getOwnerTopLevelAsbiepId());
        return findASBIEP(asbie.getToAsbiepId(), topLevelAsbiep);
    }

    public DT queryAssocBDT(BBIE bbie) {
        BCC bcc = (bbie != null) ? findBCC(bbie.getBasedBccManifestId()) : null;
        BCCP bccp = (bcc != null) ? queryToBCCP(bcc) : null;
        return queryBDT(bccp);
    }

    public BizCtx findBusinessContext(TopLevelAsbiep topLevelAsbiep) {
        BigInteger bizCtxId = (topLevelAsbiep != null) ? bizCtxRepository.findByTopLevelAsbiep(topLevelAsbiep).get(0).getBizCtxId() : BigInteger.ZERO;
        //return the first one of the list
        return bizCtxRepository.findById(bizCtxId);
    }

    public List<BizCtx> findBusinessContexts(TopLevelAsbiep topLevelAsbiep) {
        return bizCtxRepository.findByTopLevelAsbiep(topLevelAsbiep);
    }

    public List<ContextSchemeValue> findContextSchemeValue(BizCtx businessContext) {
        List<BusinessContextValue> businessContextValues = (businessContext != null) ?
                bizCtxValueRepository.findByBizCtxId(businessContext.getBizCtxId()) :
                Collections.emptyList();

        return businessContextValues.stream()
                .map(e -> ctxSchemeValueRepository.findById(e.getCtxSchemeValueId()))
                .collect(Collectors.toList());
    }

    public ContextScheme findContextScheme(BigInteger ctxSchemeId) {
        return (ctxSchemeId != null && ctxSchemeId.longValue() > 0L) ? findContextSchemeMap.get(ctxSchemeId) : null;
    }

    public ContextCategory findContextCategory(BigInteger ctxCategoryId) {
        return (ctxCategoryId != null && ctxCategoryId.longValue() > 0L) ? findContextCategoryMap.get(ctxCategoryId) : null;
    }

    public AgencyIdList findAgencyIdList(ContextScheme contextScheme) {
        String schemeAgencyId = contextScheme.getSchemeAgencyId();
        if (!StringUtils.hasLength(schemeAgencyId)) {
            return null;
        }

        for (AgencyIdList agencyIdList : findAgencyIdListMap.values()) {
            if (schemeAgencyId.equals(agencyIdList.getListId())) {
                return agencyIdList;
            }
        }

        return null;
    }

    public NamespaceList findNamespace(BigInteger namespaceId) {
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
