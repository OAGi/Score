package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.oagi.score.data.*;
import org.oagi.score.repo.component.asbiep.AsbiepReadRepository;
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
    // Prepared Datas
    private Map<BigInteger, BdtPriRestri> findBdtPriRestriByBdtIdAndDefaultIsTrueMap;
    private Map<BigInteger, BdtPriRestri> findBdtPriRestriMap;
    private Map<BigInteger, CdtAwdPriXpsTypeMap> findCdtAwdPriXpsTypeMapMap;
    private Map<BigInteger, BdtScPriRestri> findBdtScPriRestriByBdtIdAndDefaultIsTrueMap;
    private Map<BigInteger, BdtScPriRestri> findBdtScPriRestriMap;
    private Map<BigInteger, CdtScAwdPriXpsTypeMap> findCdtScAwdPriXpsTypeMapMap;
    private Map<BigInteger, Xbt> findXbtMap;
    private Map<BigInteger, CodeList> findCodeListMap;
    private Map<BigInteger, List<CodeListValue>> findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap;
    private Map<BigInteger, ACC> findACCMap;
    private Map<BigInteger, BCC> findBCCMap;
    private Map<BigInteger, BCCP> findBCCPMap;
    private Map<BigInteger, List<BCC>> findBCCByFromAccIdMap;
    private Map<BigInteger, BCCP> findBccpByBccpIdMap;
    private Map<BigInteger, ASCC> findASCCMap;
    private Map<BigInteger, List<ASCC>> findASCCByFromAccIdMap;
    private Map<BigInteger, ASCCP> findASCCPMap;
    private Map<BigInteger, DT> findDTMap;
    private Map<BigInteger, DTSC> findDtScMap;
    private Map<BigInteger, AgencyIdList> findAgencyIdListMap;
    private Map<BigInteger, AgencyIdListValue> findAgencyIdListValueMap;
    private Map<BigInteger, List<AgencyIdListValue>> findAgencyIdListValueByOwnerListIdMap;
    private Map<BigInteger, ABIE> findAbieMap;
    private Map<BigInteger, List<BBIE>> findBbieByFromAbieIdAndUsedIsTrueMap;
    private Map<BigInteger, List<BBIESC>>
            findBbieScByBbieIdAndUsedIsTrueMap;
    private Map<BigInteger, List<ASBIE>> findAsbieByFromAbieIdMap;
    private Map<BigInteger, ASBIEP> findASBIEPMap;
    private Map<BigInteger, ASBIEP> findAsbiepByRoleOfAbieIdMap;
    private Map<BigInteger, BBIEP> findBBIEPMap;
    private Map<BigInteger, String> findUserNameMap;
    private Map<BigInteger, String> findReleaseNumberMap;
    private Map<BigInteger, ContextScheme> findContextSchemeMap;
    private Map<BigInteger, ContextCategory> findContextCategoryMap;

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
        if (releaseIdSet.size() != 1) {
            throw new UnsupportedOperationException("`releaseId` for all `topLevelAsbieps` parameter must be same.");
        }

        Set<TopLevelAsbiep> topLevelAsbiepSet = new HashSet();
        topLevelAsbiepSet.addAll(topLevelAsbieps);
        topLevelAsbiepSet.addAll(findRefTopLevelAsbieps(topLevelAsbiepSet));

        init(topLevelAsbiepSet.stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toList()), releaseIdSet.iterator().next());
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

    private void init(Collection<BigInteger> topLevelAsbiepIds, BigInteger releaseId) {
        List<BdtPriRestri> bdtPriRestriList = bdtPriRestriRepository.findAll();
        findBdtPriRestriByBdtIdAndDefaultIsTrueMap = bdtPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toMap(e -> e.getBdtId(), Function.identity()));
        findBdtPriRestriMap = bdtPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtPriRestriId(), Function.identity()));

        List<BdtScPriRestri> bdtScPriRestriList = bdtScPriRestriRepository.findAll();
        findBdtScPriRestriByBdtIdAndDefaultIsTrueMap = bdtScPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toMap(e -> e.getBdtScId(), Function.identity()));
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
                .filter(e -> e.getReleaseId() == releaseId)
                .collect(Collectors.toMap(e -> e.getXbtId(), Function.identity()));

        List<CodeList> codeLists = codeListRepository.findAll();
        findCodeListMap = codeLists.stream()
                .collect(Collectors.toMap(e -> e.getCodeListId(), Function.identity()));

        List<CodeListValue> codeListValues = codeListValueRepository.findAll();
        findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap = codeListValues.stream()
                .filter(e -> e.isUsedIndicator())
                .collect(Collectors.groupingBy(e -> e.getCodeListId()));

        List<ACC> accList = accRepository.findAllByReleaseId(releaseId);
        findACCMap = accList.stream()
                .collect(Collectors.toMap(e -> e.getAccManifestId(), Function.identity()));

        List<BCC> bccList = bccRepository.findAllByReleaseId(releaseId);
        findBCCMap = bccList.stream()
                .collect(Collectors.toMap(e -> e.getBccManifestId(), Function.identity()));

        List<BCCP> bccpList = bccpRepository.findAllByReleaseId(releaseId);
        findBCCPMap = bccpList.stream()
                .collect(Collectors.toMap(e -> e.getBccpManifestId(), Function.identity()));
        findBccpByBccpIdMap = bccpList.stream()
                .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));

        List<ASCC> asccList = asccRepository.findAllByReleaseId(releaseId);
        findASCCMap = asccList.stream()
                .collect(Collectors.toMap(e -> e.getAsccManifestId(), Function.identity()));

        findASCCByFromAccIdMap = asccList.stream().collect(Collectors.groupingBy(e -> e.getFromAccManifestId()));
        findBCCByFromAccIdMap = bccList.stream().collect(Collectors.groupingBy(e -> e.getFromAccManifestId()));

        List<ASCCP> asccpList = asccpRepository.findAllByReleaseId(releaseId);
        findASCCPMap = asccpList.stream()
                .collect(Collectors.toMap(e -> e.getAsccpManifestId(), Function.identity()));

        List<DT> dataTypeList = dataTypeRepository.findAll();
        findDTMap = dataTypeList.stream()
                .filter(e -> e.getReleaseId() == releaseId)
                .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));

        List<DTSC> dtScList = dtScRepository.findAllByReleaseId(releaseId);
        findDtScMap = dtScList.stream()
                .collect(Collectors.toMap(e -> e.getDtScManifestId(), Function.identity()));

        List<AgencyIdList> agencyIdLists = agencyIdListRepository.findAll();
        findAgencyIdListMap = agencyIdLists.stream()
                .collect(Collectors.toMap(e -> e.getAgencyIdListId(), Function.identity()));

        List<AgencyIdListValue> agencyIdListValues = agencyIdListValueRepository.findAll();
        findAgencyIdListValueMap = agencyIdListValues.stream()
                .collect(Collectors.toMap(e -> e.getAgencyIdListValueId(), Function.identity()));
        findAgencyIdListValueByOwnerListIdMap = agencyIdListValues.stream()
                .collect(Collectors.groupingBy(e -> e.getOwnerListId()));

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
        findReleaseNumberMap = releaseRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getReleaseId(), e -> e.getReleaseNum()));

        findContextSchemeMap = ctxSchemeRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getCtxSchemeId(), Function.identity()));
        findContextCategoryMap = ctxCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getCtxCategoryId(), Function.identity()));
    }

    public BdtPriRestri findBdtPriRestriByBdtIdAndDefaultIsTrue(BigInteger bdtId) {
        return (bdtId != null && bdtId.longValue() > 0L) ? findBdtPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtId) : null;
    }

    public BdtPriRestri findBdtPriRestri(BigInteger bdtPriRestriId) {
        return (bdtPriRestriId != null && bdtPriRestriId.longValue() > 0L) ? findBdtPriRestriMap.get(bdtPriRestriId) : null;
    }

    public CdtAwdPriXpsTypeMap findCdtAwdPriXpsTypeMap(BigInteger cdtAwdPriXpsTypeMapId) {
        return (cdtAwdPriXpsTypeMapId != null && cdtAwdPriXpsTypeMapId.longValue() > 0L) ?
                findCdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId) : null;
    }

    public BdtScPriRestri findBdtScPriRestriByBdtScIdAndDefaultIsTrue(BigInteger bdtScId) {
        return (bdtScId != null && bdtScId.longValue() > 0L) ? findBdtScPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtScId) : null;
    }

    public BdtScPriRestri findBdtScPriRestri(BigInteger bdtScPriRestriId) {
        return (bdtScPriRestriId != null && bdtScPriRestriId.longValue() > 0L) ? findBdtScPriRestriMap.get(bdtScPriRestriId) : null;
    }

    public CdtScAwdPriXpsTypeMap findCdtScAwdPriXpsTypeMap(BigInteger cdtScAwdPriXpsTypeMapId) {
        return (cdtScAwdPriXpsTypeMapId != null && cdtScAwdPriXpsTypeMapId.longValue() > 0L) ?
                findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId) : null;
    }

    public Xbt findXbt(BigInteger xbtId) {
        return (xbtId.longValue() > 0L) ? findXbtMap.get(xbtId) : null;
    }

    public CodeList findCodeList(BigInteger codeListId) {
        return (codeListId != null && codeListId.longValue() > 0L) ? findCodeListMap.get(codeListId) : null;
    }

    public List<CodeListValue> findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(BigInteger codeListId) {
        return findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.containsKey(codeListId) ?
                findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.get(codeListId) :
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

    public DT findDT(BigInteger dtId) {
        return (dtId != null && dtId.longValue() > 0L) ? findDTMap.get(dtId) : null;
    }

    public DTSC findDtSc(BigInteger dtScManifestId) {
        return (dtScManifestId != null && dtScManifestId.longValue() > 0L) ? findDtScMap.get(dtScManifestId) : null;
    }

    public AgencyIdList findAgencyIdList(BigInteger agencyIdListId) {
        return (agencyIdListId != null && agencyIdListId.longValue() > 0L) ? findAgencyIdListMap.get(agencyIdListId) : null;
    }

    public AgencyIdListValue findAgencyIdListValue(BigInteger agencyIdListValueId) {
        return (agencyIdListValueId != null && agencyIdListValueId.longValue() > 0L) ? findAgencyIdListValueMap.get(agencyIdListValueId) : null;
    }

    public List<AgencyIdListValue> findAgencyIdListValueByOwnerListId(BigInteger ownerListId) {
        return findAgencyIdListValueByOwnerListIdMap.containsKey(ownerListId) ?
                findAgencyIdListValueByOwnerListIdMap.get(ownerListId) :
                Collections.emptyList();
    }

    public ABIE findAbie(BigInteger abieId) {
        return (abieId != null && abieId.longValue() > 0L) ? findAbieMap.get(abieId) : null;
    }

    public List<BBIE> findBbieByFromAbieIdAndUsedIsTrue(BigInteger fromAbieId) {
        return findBbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                findBbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                Collections.emptyList();
    }

    public List<BBIESC> findBbieScByBbieIdAndUsedIsTrue(BigInteger bbieId) {
        return findBbieScByBbieIdAndUsedIsTrueMap.containsKey(bbieId) ?
                findBbieScByBbieIdAndUsedIsTrueMap.get(bbieId) :
                Collections.emptyList();
    }

    public List<ASBIE> findAsbieByFromAbieId(BigInteger fromAbieId) {
        return findAsbieByFromAbieIdMap.containsKey(fromAbieId) ?
                findAsbieByFromAbieIdMap.get(fromAbieId) :
                Collections.emptyList();
    }

    public ASBIEP findASBIEP(BigInteger asbiepId) {
        return (asbiepId != null && asbiepId.longValue() > 0L) ? findASBIEPMap.get(asbiepId) : null;
    }

    public ASBIEP findAsbiepByRoleOfAbieId(BigInteger roleOfAbieId) {
        return (roleOfAbieId != null && roleOfAbieId.longValue() > 0L) ? findAsbiepByRoleOfAbieIdMap.get(roleOfAbieId) : null;
    }

    public BBIEP findBBIEP(BigInteger bbiepId) {
        return (bbiepId != null && bbiepId.longValue() > 0L) ? findBBIEPMap.get(bbiepId) : null;
    }

    public String findUserName(BigInteger userId) {
        return (userId != null && userId.longValue() > 0L) ? findUserNameMap.get(userId) : null;
    }

    public String findReleaseNumber(BigInteger releaseId) {
        return (releaseId != null && releaseId.longValue() > 0L) ? findReleaseNumberMap.get(releaseId) : null;
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

        List<ASBIE> asbieList = findAsbieByFromAbieId(abie.getAbieId());
        List<BBIE> bbieList = findBbieByFromAbieIdAndUsedIsTrue(abie.getAbieId());

        List<BIE> result = new ArrayList();
        for (BIE bie : sorted(abie, asbieList, bbieList)) {
            if (bie instanceof BBIE) {
                BBIE bbie = (BBIE) bie;
                if (bbie.getCardinalityMax() != 0) {
                    result.add(bbie);
                }
            } else {
                ASBIE asbie = (ASBIE) bie;
                ASBIEP toAsbiep = findASBIEP(asbie.getToAsbiepId());
                ABIE roleOfAbie = findAbie(toAsbiep.getRoleOfAbieId());
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

    // Get only SCs whose is_used is true.
    public List<BBIESC> queryBBIESCs(BBIE bbie) {
        return (bbie != null) ? findBbieScByBbieIdAndUsedIsTrue(bbie.getBbieId()) : Collections.emptyList();
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
        return (bccp != null) ? findDT(bccp.getBdtId()) : null;
    }

    public ASCCP queryBasedASCCP(ASBIEP asbiep) {
        return (asbiep != null) ? findASCCP(asbiep.getBasedAsccpManifestId()) : null;
    }

    public ASCC queryBasedASCC(ASBIE asbie) {
        return (asbie != null) ? findASCC(asbie.getBasedAsccManifestId()) : null;
    }

    public ABIE queryTargetABIE(ASBIEP asbiep) {
        return (asbiep != null) ? findAbie(asbiep.getRoleOfAbieId()) : null;
    }

    public ACC queryTargetACC(ASBIEP asbiep) {
        ABIE abie = (asbiep != null) ? findAbie(asbiep.getRoleOfAbieId()) : null;
        return (abie != null) ? findACC(abie.getBasedAccManifestId()) : null;
    }

    public ABIE queryTargetABIE2(ASBIEP asbiep) {
        return (asbiep != null) ? findAbie(asbiep.getRoleOfAbieId()) : null;
    }

    public BCC queryBasedBCC(BBIE bbie) {
        return (bbie != null) ? findBCC(bbie.getBasedBccManifestId()) : null;
    }

    public BCCP queryToBCCP(BCC bcc) {
        return (bcc != null) ? findBccpByBccpIdMap.get(bcc.getToBccpId()) : null;
    }

    public CodeList getCodeList(BBIESC bbieSc) {
        if (bbieSc == null) {
            return null;
        }

        CodeList codeList = findCodeList(bbieSc.getCodeListId());
        if (codeList != null) {
            return codeList;
        }

        BdtScPriRestri bdtScPriRestri = findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        if (bdtScPriRestri != null) {
            return findCodeList(bdtScPriRestri.getCodeListId());
        } else {
            DTSC gDTSC = findDtSc(bbieSc.getBasedDtScManifestId());
            BdtScPriRestri bBDTSCPrimitiveRestriction =
                    (gDTSC != null) ? findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId()) : null;
            if (bBDTSCPrimitiveRestriction != null) {
                codeList = findCodeList(bBDTSCPrimitiveRestriction.getCodeListId());
            }
        }

        return codeList;
    }

    public AgencyIdList getAgencyIdList(BBIE bbie) {
        if (bbie == null) {
            return null;
        }
        AgencyIdList agencyIdList = findAgencyIdList(bbie.getAgencyIdListId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        BdtPriRestri bdtPriRestri =
                findBdtPriRestri(bbie.getBdtPriRestriId());
        if (bdtPriRestri != null) {
            agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListId());
        }

        if (agencyIdList == null) {
            DT bdt = queryAssocBDT(bbie);
            bdtPriRestri = findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
            if (bdtPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListId());
            }
        }
        return agencyIdList;
    }

    public AgencyIdList getAgencyIdList(BBIESC bbieSc) {
        if (bbieSc == null) {
            return null;
        }
        AgencyIdList agencyIdList = findAgencyIdList(bbieSc.getAgencyIdListId());
        if (agencyIdList != null) {
            return agencyIdList;
        }

        BdtScPriRestri bdtScPriRestri =
                findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        if (bdtScPriRestri != null) {
            agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
        }

        if (agencyIdList == null) {
            DTSC gDTSC = findDtSc(bbieSc.getBasedDtScManifestId());
            bdtScPriRestri = (gDTSC != null) ? findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId()) : null;
            if (bdtScPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
            }
        }
        return agencyIdList;
    }

    public List<CodeListValue> getCodeListValues(CodeList codeList) {
        return (codeList != null) ?
                findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(codeList.getCodeListId()) :
                Collections.emptyList();
    }

    public ASBIEP queryAssocToASBIEP(ASBIE asbie) {
        return (asbie != null) ? findASBIEP(asbie.getToAsbiepId()) : null;
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

}
