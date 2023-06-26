package org.oagi.score.gateway.http.api.oas_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.OasDocRepository;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpenAPIDocService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;
    @Autowired
    private OasDocRepository oasDocRepository;
    @Autowired
    private DSLContext dslContext;

    public GetOasDocResponse getOasDoc(GetOasDocRequest request) {
        GetOasDocResponse response = scoreRepositoryFactory.createOasDocReadRepository().getOasDoc(request);
        return response;
    }

    public GetOasDocListResponse getOasDocList(GetOasDocListRequest request) {
        GetOasDocListResponse response = scoreRepositoryFactory.createOasDocReadRepository().getOasDocList(request);
        return response;
    }

    @Transactional
    public CreateOasDocResponse createOasDoc(CreateOasDocRequest request) {
        CreateOasDocResponse response = scoreRepositoryFactory.createOasDocWriteRepository().createOasDoc(request);
        return response;
    }

    @Transactional
    public UpdateOasDocResponse updateOasDoc(UpdateOasDocRequest request) {
        UpdateOasDocResponse response = scoreRepositoryFactory.createOasDocWriteRepository().updateOasDoc(request);
        return response;
    }

    @Transactional
    public DeleteOasDocResponse DeleteOasDoc(DeleteOasDocRequest request) {
        DeleteOasDocResponse response = scoreRepositoryFactory.createOasDocWriteRepository().deleteOasDoc(request);
        return response;
    }

    public GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) {
        GetBieForOasDocResponse response = scoreRepositoryFactory.createBieForOasDocReadRepository().getBieForOasDoc(request);
        return response;
    }

    @Transactional
    public AddBieForOasDocResponse addBieForOasDoc(AuthenticatedPrincipal user, AddBieForOasDocRequest request) {
        BigInteger userId = sessionService.userId(user);
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }
        if (request.getOasDocId() == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }
        if (request.getTopLevelAsbiepId() == null) {
            throw new IllegalArgumentException("`TopLevelAsbiepId` parameter must not be null.");
        }
        AsccpManifestRecord asccpManifest =
                ccRepository.getAsccpManifestByManifestId(request.asccpManifestId());
        if (asccpManifest == null) {
            throw new IllegalArgumentException();
        }
        AccManifestRecord accManifestRecord = ccRepository.getAccManifestByManifestId(asccpManifest.getRoleOfAccManifestId());
        AccRecord accRecord = ccRepository.getAccById(accManifestRecord.getAccId());
        if (OagisComponentType.valueOf(accRecord.getOagisComponentType()).isGroup()) {
            throw new IllegalArgumentException("Cannot create BIE of `ASCCP` with group `ACC`.");
        }

        String asccpPath = "ASCCP-" + asccpManifest.getAsccpManifestId();
        String accPath = "ACC-" + asccpManifest.getRoleOfAccManifestId();
        accPath = String.join(">",
                Arrays.asList(asccpPath, accPath));

        long millis = System.currentTimeMillis();

        ULong topLevelAsbiepId = bieRepository.insertTopLevelAsbiep()
                .setUserId(userId)
                .setReleaseId(asccpManifest.getReleaseId())
                .setTimestamp(millis)
                .execute();

        ULong abieId = bieRepository.insertAbie()
                .setUserId(userId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setAccManifestId(asccpManifest.getRoleOfAccManifestId())
                .setPath(accPath)
                .setTimestamp(millis)
                .execute();

        bieRepository.insertBizCtxAssignments()
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setBizCtxIds(bizCtxIds)
                .execute();

        ULong asbiepId = bieRepository.insertAsbiep()
                .setAsccpManifestId(asccpManifest.getAsccpManifestId())
                .setRoleOfAbieId(abieId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .setPath(asccpPath)
                .setUserId(userId)
                .setTimestamp(millis)
                .execute();

        bieRepository.updateTopLevelAsbiep()
                .setAsbiepId(asbiepId)
                .setTopLevelAsbiepId(topLevelAsbiepId)
                .execute();

        BieCreateResponse response = new BieCreateResponse();
        response.setTopLevelAsbiepId(topLevelAsbiepId.toBigInteger());
        return response;
    }

    public boolean checkOasDocUniqueness(OasDoc oasDoc) {
        return oasDocRepository.checkOasDocUniqueness(oasDoc);
    }

    public boolean checkOasDocTitleUniqueness(OasDoc oasDoc) {
        return oasDocRepository.checkOasDocTitleUniqueness(oasDoc);
    }


}
