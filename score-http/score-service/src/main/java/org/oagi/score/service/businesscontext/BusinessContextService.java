package org.oagi.score.service.businesscontext;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BIZ_CTX_ASSIGNMENT;

import java.math.BigInteger;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.businesscontext.model.CreateBusinessContextRequest;
import org.oagi.score.repo.api.businesscontext.model.CreateBusinessContextResponse;
import org.oagi.score.repo.api.businesscontext.model.DeleteBusinessContextRequest;
import org.oagi.score.repo.api.businesscontext.model.DeleteBusinessContextResponse;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextResponse;
import org.oagi.score.repo.api.businesscontext.model.UpdateBusinessContextRequest;
import org.oagi.score.repo.api.businesscontext.model.UpdateBusinessContextResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BusinessContextService {

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private DSLContext dslContext;

    public GetBusinessContextListResponse getBusinessContextList(GetBusinessContextListRequest request, boolean isTenantInstance) {
        GetBusinessContextListResponse response =
                scoreRepositoryFactory.createBusinessContextReadRepository()
                        .getBusinessContextList(request, isTenantInstance);

        return response;
    }

    public GetBusinessContextResponse getBusinessContext(GetBusinessContextRequest request) {
        GetBusinessContextResponse response =
                scoreRepositoryFactory.createBusinessContextReadRepository()
                        .getBusinessContext(request);

        return response;
    }

    @Transactional
    public CreateBusinessContextResponse createBusinessContext(CreateBusinessContextRequest request) {
        CreateBusinessContextResponse response =
                scoreRepositoryFactory.createBusinessContextWriteRepository()
                        .createBusinessContext(request);

        return response;
    }

    @Transactional
    public UpdateBusinessContextResponse updateBusinessContext(UpdateBusinessContextRequest request) {
        UpdateBusinessContextResponse response =
                scoreRepositoryFactory.createBusinessContextWriteRepository()
                        .updateBusinessContext(request);

        return response;
    }

    @Transactional
    public DeleteBusinessContextResponse deleteBusinessContext(DeleteBusinessContextRequest request) {
        DeleteBusinessContextResponse response =
                scoreRepositoryFactory.createBusinessContextWriteRepository()
                        .deleteBusinessContext(request);

        return response;
    }

    @Transactional
    public void assign(BigInteger bizCtxId, BigInteger topLevelAsbiepId) {
        dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId))
                .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(bizCtxId))
                .execute();
    }

    @Transactional
    public void dismiss(BigInteger bizCtxId, BigInteger topLevelAsbiepId) {
        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(and(
                        BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)),
                        BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.eq(ULong.valueOf(bizCtxId))
                ))
                .execute();
    }

}
