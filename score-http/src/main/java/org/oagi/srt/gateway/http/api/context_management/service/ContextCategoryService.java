package org.oagi.srt.gateway.http.api.context_management.service;

import com.google.common.base.Functions;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.oagi.srt.gateway.http.api.context_management.data.ContextCategory;
import org.oagi.srt.gateway.http.api.context_management.data.ContextCategoryListRequest;
import org.oagi.srt.gateway.http.api.context_management.data.ContextScheme;
import org.oagi.srt.gateway.http.api.context_management.data.SimpleContextCategory;
import org.oagi.srt.gateway.http.helper.SrtGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.oagi.srt.entity.jooq.Tables.CTX_CATEGORY;
import static org.oagi.srt.entity.jooq.Tables.CTX_SCHEME;
import static org.oagi.srt.gateway.http.helper.filter.ContainsFilterBuilder.contains;

@Service
@Transactional(readOnly = true)
public class ContextCategoryService {

    @Autowired
    private DSLContext dslContext;

    private SelectJoinStep<Record4<ULong, String, String, String>> getSelectJoinStep() {
        return dslContext.select(
                CTX_CATEGORY.CTX_CATEGORY_ID,
                CTX_CATEGORY.GUID,
                CTX_CATEGORY.NAME,
                CTX_CATEGORY.DESCRIPTION
        ).from(CTX_CATEGORY);
    }

    public PageResponse<ContextCategory> getContextCategoryList(ContextCategoryListRequest request) {
        SelectJoinStep<Record4<ULong, String, String, String>> step = getSelectJoinStep();

        List<Condition> conditions = new ArrayList();
        if (!StringUtils.isEmpty(request.getName())) {
            conditions.addAll(contains(request.getName(), CTX_CATEGORY.NAME));
        }
        if (!StringUtils.isEmpty(request.getDescription())) {
            conditions.addAll(contains(request.getDescription(), CTX_CATEGORY.DESCRIPTION));
        }

        SelectConnectByStep<Record4<ULong, String, String, String>> conditionStep = step.where(conditions);

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
            case "name":
                if ("asc".equals(sortDirection)) {
                    sortField = CTX_CATEGORY.NAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = CTX_CATEGORY.NAME.desc();
                }

                break;
            case "description":
                if ("asc".equals(sortDirection)) {
                    sortField = CTX_CATEGORY.DESCRIPTION.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = CTX_CATEGORY.DESCRIPTION.desc();
                }

                break;
        }

        SelectWithTiesAfterOffsetStep<Record4<ULong, String, String, String>> offsetStep = null;
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<ContextCategory> result = (offsetStep != null) ?
                offsetStep.fetchInto(ContextCategory.class) : conditionStep.fetchInto(ContextCategory.class);
        if (!result.isEmpty()) {
            Map<Long, ContextCategory> ctxCategoryMap =
                    result.stream().collect(Collectors.toMap(ContextCategory::getCtxCategoryId, Functions.identity()));

            dslContext.select(CTX_SCHEME.CTX_CATEGORY_ID,
                    coalesce(count(CTX_SCHEME.CTX_SCHEME_ID), 0))
                    .from(CTX_SCHEME)
                    .where(CTX_SCHEME.CTX_CATEGORY_ID.in(
                            ctxCategoryMap.keySet().stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())))
                    .groupBy(CTX_SCHEME.CTX_CATEGORY_ID)
                    .fetch().stream().forEach(record -> {
                long ctxCategoryId = record.value1().longValue();
                int cnt = record.value2();
                ctxCategoryMap.get(ctxCategoryId).setUsed(cnt > 0);
            });
        }

        PageResponse<ContextCategory> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(dslContext.selectCount()
                .from(CTX_CATEGORY)
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0));

        return response;
    }

    public ContextCategory getContextCategory(long ctxCategoryId) {
        ContextCategory ctxCategory = getSelectJoinStep()
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(ctxCategoryId)))
                .fetchOptionalInto(ContextCategory.class).orElse(null);

        if (ctxCategory != null) {
            int cnt = dslContext.select(coalesce(count(CTX_SCHEME.CTX_SCHEME_ID), 0))
                    .from(CTX_SCHEME)
                    .where(CTX_SCHEME.CTX_CATEGORY_ID.eq(ULong.valueOf(ctxCategoryId)))
                    .groupBy(CTX_SCHEME.CTX_CATEGORY_ID)
                    .fetchOptionalInto(Integer.class).orElse(0);
            ctxCategory.setUsed(cnt > 0);
        }

        return ctxCategory;
    }

    public List<SimpleContextCategory> getSimpleContextCategoryList() {
        return dslContext.select(
                CTX_CATEGORY.CTX_CATEGORY_ID,
                CTX_CATEGORY.NAME
        ).from(CTX_CATEGORY)
                .fetchInto(SimpleContextCategory.class);
    }

    public List<ContextScheme> getContextSchemeByCategoryId(long ctxCategoryId) {
        return dslContext.select(
                CTX_SCHEME.CTX_SCHEME_ID,
                CTX_SCHEME.GUID,
                CTX_SCHEME.SCHEME_NAME,
                CTX_SCHEME.CTX_CATEGORY_ID,
                CTX_CATEGORY.NAME.as("ctx_category_name"),
                CTX_SCHEME.SCHEME_ID,
                CTX_SCHEME.SCHEME_AGENCY_ID,
                CTX_SCHEME.SCHEME_VERSION_ID,
                CTX_SCHEME.DESCRIPTION,
                CTX_SCHEME.LAST_UPDATE_TIMESTAMP
        ).from(CTX_SCHEME)
                .join(CTX_CATEGORY).on(CTX_SCHEME.CTX_CATEGORY_ID.equal(CTX_CATEGORY.CTX_CATEGORY_ID))
                .where(CTX_SCHEME.CTX_CATEGORY_ID.eq(ULong.valueOf(ctxCategoryId)))
                .fetchInto(ContextScheme.class);
    }

    @Transactional
    public void insert(ContextCategory contextCategory) {
        if (StringUtils.isEmpty(contextCategory.getGuid())) {
            contextCategory.setGuid(SrtGuid.randomGuid());
        }

        dslContext.insertInto(CTX_CATEGORY,
                CTX_CATEGORY.GUID,
                CTX_CATEGORY.NAME,
                CTX_CATEGORY.DESCRIPTION)
                .values(
                        contextCategory.getGuid(),
                        contextCategory.getName(),
                        contextCategory.getDescription()
                ).execute();
    }

    @Transactional
    public void update(ContextCategory contextCategory) {
        dslContext.update(CTX_CATEGORY)
                .set(CTX_CATEGORY.NAME, contextCategory.getName())
                .set(CTX_CATEGORY.DESCRIPTION, contextCategory.getDescription())
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(contextCategory.getCtxCategoryId())))
                .execute();
    }

    @Transactional
    public void delete(long ctxCategoryId) {
        dslContext.deleteFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.eq(ULong.valueOf(ctxCategoryId)))
                .execute();
    }

    @Transactional
    public void delete(List<Long> ctxCategoryIds) {
        if (ctxCategoryIds == null || ctxCategoryIds.isEmpty()) {
            return;
        }

        dslContext.deleteFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CTX_CATEGORY_ID.in(
                        ctxCategoryIds.stream().map(
                                e -> ULong.valueOf(e)).collect(Collectors.toList())
                ))
                .execute();
    }
}
