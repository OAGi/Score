package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.data.TopLevelAsbiep;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.data.*;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.Zip;
import org.oagi.score.repo.BiePackageRepository;
import org.oagi.score.repo.PaginationResponse;
import org.oagi.score.repo.api.bie.model.BiePackageState;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repository.TopLevelAsbiepRepository;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@Service
@Transactional
public class BiePackageService implements ApplicationContextAware {

    @Autowired
    private BiePackageRepository repository;

    @Autowired
    private BusinessContextService businessContextService;

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BieGenerateService bieGenerateService;

    @Autowired
    private TopLevelAsbiepRepository topLevelAsbiepRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Transactional(readOnly = true)
    public PageResponse<BiePackage> getBiePackageList(BiePackageListRequest request) {
        PaginationResponse<BiePackage> result = repository.getBiePackageList(request);

        result.getResult().forEach(biePackage -> biePackage.setAccess(
                AccessPrivilege.toAccessPrivilege(
                        sessionService.getAppUserByUsername(request.getRequester().getUserId()),
                        biePackage.getOwner().getUserId(), biePackage.getState())));

        PageResponse<BiePackage> response = new PageResponse();
        response.setList(result.getResult());
        response.setPage(request.getPageIndex());
        response.setSize(request.getPageSize());
        response.setLength(result.getPageCount());
        return response;
    }

    @Transactional(readOnly = true)
    public BiePackage getBiePackageById(ScoreUser requester, BigInteger biePackageId) {
        BiePackageListRequest request = new BiePackageListRequest(requester);
        request.setBiePackageIds(Arrays.asList(biePackageId));

        PageResponse<BiePackage> response = getBiePackageList(request);
        List<BiePackage> result = response.getList();
        if (result.isEmpty()) {
            throw new NullPointerException();
        }
        return result.get(0);
    }

    public CreateBiePackageResponse createBiePackage(CreateBiePackageRequest request) {
        if (!hasLength(request.getVersionId())) {
            request.setVersionId("v1.0");
        }
        if (!hasLength(request.getVersionName())) {
            request.setVersionName("New BIE Package");
        }
        BigInteger biePackageId = repository.createBiePackage(request);
        return new CreateBiePackageResponse(biePackageId);
    }

    public void updateBiePackage(UpdateBiePackageRequest request) {
        ensureBiePackageIsUpdatable(request.getRequester(), request.getBiePackageId());

        repository.updateBiePackage(request);
    }

    private BiePackage ensureBiePackageIsUpdatable(ScoreUser requester, BigInteger biePackageId) {
        BiePackage biePackage = getBiePackageById(requester, biePackageId);
        if (biePackage == null) {
            throw new IllegalArgumentException("No BIE Package with ID " + biePackageId);
        }

        if (BiePackageState.WIP != biePackage.getState()) {
            throw new DataAccessForbiddenException("Not allowed to update the BIE package in '" + biePackage.getState() + "' state.");
        }

        if (!requester.getUserId().equals(biePackage.getOwner().getUserId())) {
            throw new DataAccessForbiddenException("Only allowed to update the BIE package by the owner.");
        }

        return biePackage;
    }

    public void updateBiePackageState(UpdateBiePackageRequest request) {
        ScoreUser requester = request.getRequester();
        BigInteger biePackageId = request.getBiePackageId();

        BiePackage biePackage = getBiePackageById(requester, biePackageId);
        if (biePackage == null) {
            throw new IllegalArgumentException("No BIE Package with ID " + biePackageId);
        }

        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            if (!requester.getUserId().equals(biePackage.getOwner().getUserId())) {
                throw new DataAccessForbiddenException("Only allowed to update the BIE package by the owner.");
            }
        }

        repository.updateBiePackageState(requester, biePackage, request.getState());
    }

    public void deleteBiePackage(DeleteBiePackageRequest request) {
        List<BigInteger> biePackageIdList = request.getBiePackageIdList();
        if (biePackageIdList == null || biePackageIdList.isEmpty()) {
            return;
        }

        ScoreUser requester = request.getRequester();
        ensureProperDeleteBiePackageRequest(requester, biePackageIdList);
        repository.deleteBiePackageList(biePackageIdList);
    }

    private void ensureProperDeleteBiePackageRequest(ScoreUser requester, List<BigInteger> biePackageIdList) {
        List<BiePackage> biePackages = repository.getBiePackageList(
                        new BiePackageListRequest(requester)
                                .withBiePackageIdList(biePackageIdList))
                .getResult();

        // Issue #1576
        // Administrator can discard BIE packages in any state.
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            BigInteger requesterUserId = requester.getUserId();
            for (BiePackage biePackage : biePackages) {
                BiePackageState state = biePackage.getState();
                if (state == BiePackageState.Production) {
                    throw new DataAccessForbiddenException("Not allowed to delete the BIE package in '" + state + "' state.");
                }

                if (!requesterUserId.equals(biePackage.getOwner().getUserId())) {
                    throw new DataAccessForbiddenException("Only allowed to delete the BIE package by the owner.");
                }
            }
        }
    }

    public void transferOwnership(BieOwnershipTransferRequest request) {
        repository.transferOwnership(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<BieList> getBieListInBiePackage(BieListInBiePackageRequest request) {
        PaginationResponse<BieList> result = repository.getBieListInBiePackage(request);

        List<BieList> bieLists = result.getResult();
        bieLists.forEach(bieList -> {
            GetBusinessContextListRequest getBusinessContextListRequest =
                    new GetBusinessContextListRequest(request.getRequester())
                            .withTopLevelAsbiepIdList(Arrays.asList(bieList.getTopLevelAsbiepId()));

            getBusinessContextListRequest.setPageIndex(-1);
            getBusinessContextListRequest.setPageSize(-1);

            GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                    .getBusinessContextList(getBusinessContextListRequest, applicationConfigurationService.isTenantEnabled());

            bieList.setBusinessContexts(getBusinessContextListResponse.getResults());
            bieList.setAccess(
                    AccessPrivilege.toAccessPrivilege(
                            sessionService.getAppUserByUsername(request.getRequester().getUserId()),
                            bieList.getOwnerUserId(), bieList.getState())
            );
        });

        PageResponse<BieList> response = new PageResponse();
        response.setList(result.getResult());
        response.setPage(request.getPageIndex());
        response.setSize(request.getPageSize());
        response.setLength(result.getPageCount());
        return response;
    }

    public void addBieToBiePackage(AddBieToBiePackageRequest request) {
        BiePackage biePackage = ensureBiePackageIsUpdatable(request.getRequester(), request.getBiePackageId());

        repository.addBieToBiePackage(request.getRequester(),
                biePackage, request.getTopLevelAsbiepIdList());
    }

    public void deleteBieInBiePackage(DeleteBieInBiePackageRequest request) {
        BiePackage biePackage = ensureBiePackageIsUpdatable(request.getRequester(), request.getBiePackageId());

        repository.deleteBieInBiePackage(request.getRequester(),
                biePackage, request.getTopLevelAsbiepIdList());
    }

    public GenerateBiePackageResponse generate(GenerateBiePackageRequest request) throws IOException {

        BiePackage biePackage = getBiePackageById(request.getRequester(), request.getBiePackageId());
        List<BigInteger> topLevelAsbiepIdListInBiePackage = repository.getBieListInBiePackage(
                        new BieListInBiePackageRequest(request.getRequester())
                                .withBiePackageId(request.getBiePackageId())).getResult()
                .stream().map(e -> e.getTopLevelAsbiepId()).collect(Collectors.toList());

        List<BigInteger> topLevelAsbiepIdList = request.getTopLevelAsbiepIdList();
        if (topLevelAsbiepIdList.isEmpty()) {
            topLevelAsbiepIdList = topLevelAsbiepIdListInBiePackage;
        } else {
            for (BigInteger topLevelAsbiepId : topLevelAsbiepIdList) {
                if (!topLevelAsbiepIdListInBiePackage.contains(topLevelAsbiepId)) {
                    throw new IllegalArgumentException("Invalid request for generating a BIE that is not included in the BIE Package.");
                }
            }
        }

        List<TopLevelAsbiep> topLevelAsbiepList = topLevelAsbiepRepository.findByIdIn(topLevelAsbiepIdList);

        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setExpressionOption(request.getSchemaExpression());
        option.setBiePackage(biePackage);

        Map<BigInteger, File> result = bieGenerateService.generateSchemaForEach(topLevelAsbiepList, option);
        return makeGenerateBiePackageResponse(biePackage, result);
    }

    private GenerateBiePackageResponse makeGenerateBiePackageResponse(BiePackage biePackage, Map<BigInteger, File> result) throws IOException {
        File file;
        if (result.size() == 1) {
            file = result.values().iterator().next();
        } else {
            String filename = biePackage.getVersionName() + "-" + biePackage.getVersionId() + "-" + System.currentTimeMillis();
            file = Zip.compression(result.values(), filename);
        }

        GenerateBiePackageResponse response = new GenerateBiePackageResponse();
        response.setFile(file);

        String filename = file.getName();
        response.setFilename(filename);

        String contentType;
        if (filename.endsWith(".xsd")) {
            contentType = "text/xml";
        } else if (filename.endsWith(".json")) {
            contentType = "application/json";
        } else if (filename.endsWith(".zip")) {
            contentType = "application/zip";
        } else if (filename.endsWith(".yml")) {
            contentType = "text/x-yaml";
        } else {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        return response;
    }

}
