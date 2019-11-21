package org.oagi.srt.gateway.http.api.account_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.srt.entity.jooq.tables.records.AppUserRecord;
import org.oagi.srt.gateway.http.api.account_management.data.AccountListRequest;
import org.oagi.srt.gateway.http.api.account_management.data.AppUser;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.oagi.srt.entity.jooq.Tables.APP_USER;

@Service
@Transactional(readOnly = true)
public class AccountListService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DSLContext dslContext;

    public PageResponse<AppUser> getAccounts(org.springframework.security.core.userdetails.User requester,
                                             AccountListRequest request) {
        SelectJoinStep<Record5<ULong, String, String, Byte, String>> step = dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.ORGANIZATION
        ).from(APP_USER);

        List<Condition> conditions = new ArrayList();
        if (!StringUtils.isEmpty(request.getLoginId())) {
            conditions.add(APP_USER.LOGIN_ID.contains(request.getLoginId().trim()));
        }
        if (!StringUtils.isEmpty(request.getName())) {
            conditions.add(APP_USER.NAME.containsIgnoreCase(request.getName().trim()));
        }
        if (!StringUtils.isEmpty(request.getOrganization())) {
            conditions.add(APP_USER.ORGANIZATION.containsIgnoreCase(request.getOrganization().trim()));
        }
        if (!StringUtils.isEmpty(request.getRole())) {
            switch (request.getRole()) {
                case "developer":
                    conditions.add(APP_USER.IS_DEVELOPER.eq((byte) 1));
                    break;
                case "end-user":
                    conditions.add(APP_USER.IS_DEVELOPER.eq((byte) 0));
                    break;
            }
        }
        Boolean excludeRequester = request.getExcludeRequester();
        if (excludeRequester != null && excludeRequester == true) {
            conditions.add(APP_USER.LOGIN_ID.ne(requester.getUsername().trim()));
        }

        SelectConnectByStep<Record5<ULong, String, String, Byte, String>> conditionStep = step.where(conditions);

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        switch (pageRequest.getSortActive()) {
            case "loginId":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.LOGIN_ID.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.LOGIN_ID.desc();
                }

                break;
            case "name":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.NAME.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.NAME.desc();
                }

                break;
            case "organization":
                if ("asc".equals(sortDirection)) {
                    sortField = APP_USER.ORGANIZATION.asc();
                } else if ("desc".equals(sortDirection)) {
                    sortField = APP_USER.ORGANIZATION.desc();
                }

                break;
        }

        SelectWithTiesAfterOffsetStep<Record5<ULong, String, String, Byte, String>> offsetStep = null;
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<AppUser> result = (offsetStep != null) ?
                offsetStep.fetchInto(AppUser.class) : conditionStep.fetchInto(AppUser.class);

        PageResponse<AppUser> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(dslContext.selectCount()
                .from(APP_USER)
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0));

        return response;
    }

    public AppUser getAccount(String loginId) {
        return dslContext.select(
                APP_USER.APP_USER_ID,
                APP_USER.LOGIN_ID,
                APP_USER.PASSWORD,
                APP_USER.NAME,
                APP_USER.IS_DEVELOPER.as("developer"),
                APP_USER.ORGANIZATION
        ).from(APP_USER).where(APP_USER.LOGIN_ID.eq(loginId))
                .fetchOneInto(AppUser.class);
    }

    public List<String> getAccountLoginIds() {
        return dslContext.select(
                APP_USER.LOGIN_ID)
                .from(APP_USER)
                .fetchInto(String.class);
    }

    @Transactional
    public void insert(AppUser account) {
        AppUserRecord record = new AppUserRecord();
        record.setLoginId(account.getLoginId());
        record.setPassword(passwordEncoder.encode(account.getPassword()));
        record.setName(account.getName());
        record.setOrganization(account.getOrganization());
        record.setIsDeveloper((byte) (account.isDeveloper() ? 1 : 0));

        dslContext.insertInto(APP_USER)
                .set(record)
                .execute();
    }

    public boolean hasTaken(String loginId) {
        return dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.eq(loginId))
                .fetchOptionalInto(Long.class).orElse(0L) != 0L;
    }
}
