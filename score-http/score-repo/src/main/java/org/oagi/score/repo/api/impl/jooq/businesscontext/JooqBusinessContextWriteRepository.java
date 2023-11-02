package org.oagi.score.repo.api.impl.jooq.businesscontext;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.BusinessContextWriteRepository;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BizCtxRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BizCtxValueRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BIZ_CTX;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BIZ_CTX_VALUE;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils.randomGuid;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBusinessContextWriteRepository
        extends JooqScoreRepository
        implements BusinessContextWriteRepository {

    public JooqBusinessContextWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateBusinessContextResponse createBusinessContext(
            CreateBusinessContextRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        BizCtxRecord record = new BizCtxRecord();

        record.setGuid(randomGuid());
        record.setName(request.getName());
        record.setCreatedBy(requesterUserId);
        record.setLastUpdatedBy(requesterUserId);
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        BigInteger businessContextId = dslContext().insertInto(BIZ_CTX)
                .set(record)
                .returning(BIZ_CTX.BIZ_CTX_ID)
                .fetchOne().getBizCtxId().toBigInteger();

        CreateBusinessContextResponse response = new CreateBusinessContextResponse(businessContextId);

        for (BusinessContextValue businessContextValue : request.getBusinessContextValueList()) {
            businessContextValue = createBusinessContextValue(businessContextValue, businessContextId);
            response.addBusinessContextValue(businessContextValue);
        }

        return new CreateBusinessContextResponse(businessContextId);
    }

    private BusinessContextValue createBusinessContextValue(BusinessContextValue businessContextValue,
                                                            BigInteger businessContextId) {
        BizCtxValueRecord valueRecord = new BizCtxValueRecord();

        valueRecord.setBizCtxId(ULong.valueOf(businessContextId));
        valueRecord.setCtxSchemeValueId(ULong.valueOf(businessContextValue.getContextSchemeValueId()));

        BigInteger businessContextValueId = dslContext().insertInto(BIZ_CTX_VALUE)
                .set(valueRecord)
                .returning(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID)
                .fetchOne().getBizCtxValueId().toBigInteger();

        businessContextValue.setContextSchemeValueId(businessContextValueId);
        return businessContextValue;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateBusinessContextResponse updateBusinessContext(
            UpdateBusinessContextRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        BizCtxRecord record = dslContext().selectFrom(BIZ_CTX)
                .where(BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(request.getBusinessContextId())))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(request.getName(), record.getName())) {
            record.setName(request.getName());
            changedField.add(BIZ_CTX.NAME);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(requesterUserId);
            changedField.add(BIZ_CTX.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(timestamp);
            changedField.add(BIZ_CTX.LAST_UPDATE_TIMESTAMP);

            int affectedRows = record.update(changedField);
            if (affectedRows != 1) {
                throw new ScoreDataAccessException(new IllegalStateException());
            }
        }

        updateBusinessContextValue(request);

        return new UpdateBusinessContextResponse(
                record.getBizCtxId().toBigInteger(),
                !changedField.isEmpty());
    }

    private void updateBusinessContextValue(UpdateBusinessContextRequest request) {
        List<ULong> oldBizCtxValueIdList =
                dslContext().select(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID)
                        .from(BIZ_CTX_VALUE)
                        .where(BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(request.getBusinessContextId())))
                        .fetch(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID);

        Map<BigInteger, BusinessContextValue> businessContextValueMap = request.getBusinessContextValueList().stream()
                .filter(e -> e.getBusinessContextValueId() != null)
                .collect(Collectors.toMap(BusinessContextValue::getBusinessContextValueId, Function.identity()));

        // Delete business context values not contained in the request
        dslContext().deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.in(
                        oldBizCtxValueIdList.stream()
                                .filter(e -> !businessContextValueMap.keySet().contains(e.toBigInteger()))
                                .collect(Collectors.toList())
                ))
                .execute();

        for (BusinessContextValue businessContextValue : businessContextValueMap.values()) {
            dslContext().update(BIZ_CTX_VALUE)
                    .set(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID, ULong.valueOf(businessContextValue.getContextSchemeValueId()))
                    .where(BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID.eq(ULong.valueOf(businessContextValue.getBusinessContextValueId())))
                    .execute();
        }

        request.getBusinessContextValueList().stream()
                .filter(e -> e.getBusinessContextValueId() == null).forEach(e -> {
            createBusinessContextValue(e, request.getBusinessContextId());
        });
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteBusinessContextResponse deleteBusinessContext(
            DeleteBusinessContextRequest request) throws ScoreDataAccessException {

        List<BigInteger> businessContextIdList = request.getBusinessContextIdList();
        if (businessContextIdList == null || businessContextIdList.isEmpty()) {
            return new DeleteBusinessContextResponse(Collections.emptyList());
        }

        dslContext().delete(BIZ_CTX_VALUE)
                .where(
                        businessContextIdList.size() == 1 ?
                                BIZ_CTX_VALUE.BIZ_CTX_ID.eq(ULong.valueOf(businessContextIdList.get(0))) :
                                BIZ_CTX_VALUE.BIZ_CTX_ID.in(
                                        businessContextIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                                )
                )
                .execute();
        dslContext().delete(BIZ_CTX)
                .where(
                        businessContextIdList.size() == 1 ?
                                BIZ_CTX.BIZ_CTX_ID.eq(ULong.valueOf(businessContextIdList.get(0))) :
                                BIZ_CTX.BIZ_CTX_ID.in(
                                        businessContextIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                                )
                )
                .execute();

        DeleteBusinessContextResponse response = new DeleteBusinessContextResponse(businessContextIdList);
        return response;
    }
}
