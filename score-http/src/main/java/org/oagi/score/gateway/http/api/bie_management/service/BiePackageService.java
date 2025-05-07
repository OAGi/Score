package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BiePackageService implements ApplicationContextAware, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BiePackageCommandRepository command(ScoreUser requester) {
        return repositoryFactory.biePackageCommandRepository(requester);
    };

    private BiePackageQueryRepository query(ScoreUser requester) {
        return repositoryFactory.biePackageQueryRepository(requester);
    };

    @Autowired
    private BiePackageQueryService biePackageQueryService;

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @Autowired
    private BusinessContextQueryService businessContextQueryService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieGenerateService bieGenerateService;

    @Autowired
    private BieUpliftingService upliftingService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EventListenerContainer eventListenerContainer;

    private final String INTERESTED_EVENT_NAME = "biePackageUpliftRequestEvent";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BieUpliftingService bieUpliftingService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        eventListenerContainer.addMessageListener(this, "onEventReceived",
//                new ChannelTopic(INTERESTED_EVENT_NAME));
    }

//    public void copy(CopyBiePackageRequest request) {
//        for (BigInteger biePackageId : request.getBiePackageIdList()) {
//            repository.copyBiePackage(request.getRequester(), biePackageId);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<BieList> getBieListInBiePackage(BieListInBiePackageRequest request) {
//        PaginationResponse<BieList> result = repository.getBieListInBiePackage(request);
//
//        List<BieList> bieLists = result.getResult();
//        bieLists.forEach(bieList -> {
//            bieList.setBusinessContexts(
//                    businessContextQueryService.getBusinessContextSummaryList(
//                            request.getRequester(), bieList.getTopLevelAsbiepId(), request.getBusinessContext())
//            );
//            bieList.setAccess(
//                    AccessPrivilege.toAccessPrivilege(
//                            sessionService.getScoreUserByUserId(new UserId(request.getRequester().getUserId())),
//                            bieList.getOwnerUserId().value(), bieList.getState())
//            );
//        });
//
//        PageResponse<BieList> response = new PageResponse();
//        response.setList(result.getResult());
//        response.setPage(request.getPageIndex());
//        response.setSize(request.getPageSize());
//        response.setLength(result.getPageCount());
//        return response;
//    }
//
//    public GenerateBiePackageResponse generate(GenerateBiePackageRequest request) throws IOException {
//
//        BiePackageDetailsRecord biePackage = biePackageQueryService.getBiePackageDetails(request.getRequester(), request.getBiePackageId());
//        List<TopLevelAsbiepId> topLevelAsbiepIdListInBiePackage = repository.getBieListInBiePackage(
//                        new BieListInBiePackageRequest(request.getRequester())
//                                .withBiePackageId(request.getBiePackageId())).getResult()
//                .stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toList());
//
//        List<TopLevelAsbiepId> topLevelAsbiepIdList = request.getTopLevelAsbiepIdList();
//        if (topLevelAsbiepIdList.isEmpty()) {
//            topLevelAsbiepIdList = topLevelAsbiepIdListInBiePackage;
//        } else {
//            for (TopLevelAsbiepId topLevelAsbiepId : topLevelAsbiepIdList) {
//                if (!topLevelAsbiepIdListInBiePackage.contains(topLevelAsbiepId)) {
//                    throw new IllegalArgumentException("Invalid request for generating a BIE that is not included in the BIE Package.");
//                }
//            }
//        }
//
//        List<TopLevelAsbiep> topLevelAsbiepList = bieQueryRepository.findByIdIn(topLevelAsbiepIdList);
//
//        GenerateExpressionOption option = new GenerateExpressionOption();
//        option.setExpressionOption(request.getSchemaExpression());
//        option.setBiePackage(biePackage);
//
//        Map<TopLevelAsbiepId, File> result = bieGenerateService.generateSchemaForEach(topLevelAsbiepList, option);
//        return makeGenerateBiePackageResponse(biePackage, result);
//    }
//
//    private GenerateBiePackageResponse makeGenerateBiePackageResponse(BiePackageDetailsRecord biePackage, Map<TopLevelAsbiepId, File> result) throws IOException {
//        File file;
//        if (result.size() == 1) {
//            file = result.values().iterator().next();
//        } else {
//            String filename = biePackage.versionName() + "-" + biePackage.versionId() + "-" + System.currentTimeMillis();
//            file = Zip.compression(result.values(), filename);
//        }
//
//        GenerateBiePackageResponse response = new GenerateBiePackageResponse();
//        response.setFile(file);
//
//        String filename = file.getName();
//        response.setFilename(filename);
//
//        String contentType;
//        if (filename.endsWith(".xsd")) {
//            contentType = "text/xml";
//        } else if (filename.endsWith(".json")) {
//            contentType = "application/json";
//        } else if (filename.endsWith(".zip")) {
//            contentType = "application/zip";
//        } else if (filename.endsWith(".yml")) {
//            contentType = "text/x-yaml";
//        } else {
//            contentType = "application/octet-stream";
//        }
//
//        response.setContentType(contentType);
//        return response;
//    }
//
//    public void upliftBiePackage(UpliftBiePackageRequest request) {
//        InitUpliftBiePackageResponse response =
//                repository.initUpliftBiePackage(request.getRequester(), request.getBiePackageId(), request.getTargetReleaseId());
//
//        BiePackageUpliftRequestEvent biePackageUpliftRequestEvent = new BiePackageUpliftRequestEvent(
//                new UserId(request.getRequester().getUserId()),
//                request.getTargetReleaseId(),
//                response.getUpliftedBiePackageId(),
//                response.getSourceTopLevelAsbiepIdList());
//
//        /*
//         * Message Publishing
//         */
//        redisTemplate.convertAndSend(INTERESTED_EVENT_NAME, biePackageUpliftRequestEvent);
//    }
//
//    /**
//     * This method is invoked by 'biePackageUpliftRequestEvent' channel subscriber.
//     *
//     * @param biePackageUpliftRequestEvent
//     */
//    @Transactional
//    public void onEventReceived(BiePackageUpliftRequestEvent biePackageUpliftRequestEvent) {
//        RLock lock = redissonClient.getLock("BiePackageUpliftRequestEvent:" + biePackageUpliftRequestEvent.hashCode());
//        if (!lock.tryLock()) {
//            return;
//        }
//        try {
//            logger.debug("Received BiePackageUpliftRequestEvent: " + biePackageUpliftRequestEvent);
//            ScoreUser requester = sessionService.getScoreUserByUserId(biePackageUpliftRequestEvent.getRequestUserId());
//
//            List<TopLevelAsbiepId> sourceTopLevelAsbiepIdList = biePackageUpliftRequestEvent.getSourceTopLevelAsbiepIdList();
//            List<TopLevelAsbiepId> targetTopLevelAsbiepIdList = new ArrayList<>();
//            for (TopLevelAsbiepId topLevelAsbiepId : sourceTopLevelAsbiepIdList) {
//                UpliftBieRequest upliftBieRequest = new UpliftBieRequest();
//                upliftBieRequest.setRequester(requester);
//                upliftBieRequest.setTopLevelAsbiepId(topLevelAsbiepId.value());
//                upliftBieRequest.setTargetReleaseId(biePackageUpliftRequestEvent.getTargetReleaseId().value());
//                bieQueryRepository.findRefTopLevelAsbieps(Arrays.asList(topLevelAsbiepId));
//
//                UpliftBieResponse upliftBieResponse = bieUpliftingService.upliftBie(upliftBieRequest);
//                targetTopLevelAsbiepIdList.add(new TopLevelAsbiepId(upliftBieResponse.getTopLevelAsbiepId()));
//            }
//
//            UpdateBiePackageRequest updateBiePackageRequest = new UpdateBiePackageRequest();
//            updateBiePackageRequest.setRequester(requester);
//            updateBiePackageRequest.setBiePackageId(biePackageUpliftRequestEvent.getUpliftedBiePackageId());
//            updateBiePackageRequest.setState(BiePackageState.WIP);
//            updateBiePackageState(updateBiePackageRequest);
//
//            AddBieToBiePackageRequest addBieToBiePackageRequest = new AddBieToBiePackageRequest();
//            addBieToBiePackageRequest.setRequester(requester);
//            addBieToBiePackageRequest.setBiePackageId(biePackageUpliftRequestEvent.getUpliftedBiePackageId());
//            addBieToBiePackageRequest.setTopLevelAsbiepIdList(targetTopLevelAsbiepIdList);
//            addBieToBiePackage(addBieToBiePackageRequest);
//        } finally {
//            lock.unlock();
//        }
//    }

}
