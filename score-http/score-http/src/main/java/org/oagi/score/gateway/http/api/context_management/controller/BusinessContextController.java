package org.oagi.score.gateway.http.api.context_management.controller;

import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.api.tenant.service.TenantService;
import org.oagi.score.gateway.http.app.configuration.ConfigurationService;
import org.oagi.score.repo.api.businesscontext.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class BusinessContextController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private BusinessContextService businessContextService;
    
    @Autowired
    private ConfigurationService configService;
    
    @Autowired
    private TenantService tenantService;

    @RequestMapping(value = "/business_contexts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<BusinessContext> getBusinessContextList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "businessContextIdList", required = false) String businessContextIdList,
            @RequestParam(name = "topLevelAsbiepId", required = false) BigInteger topLevelAsbiepId,
            @RequestParam(name = "updaterUsernameList", required = false) String updaterUsernameList,
            @RequestParam(name = "updateStart", required = false) String updateStart,
            @RequestParam(name = "updateEnd", required = false) String updateEnd,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "notConnectedToTenant", required = false) Boolean notConnectedToTenant,
            @RequestParam(name = "sortActive", required = false) String sortActive,
            @RequestParam(name = "sortDirection", required = false) String sortDirection,
            @RequestParam(name = "pageIndex", defaultValue = "-1") int pageIndex,
            @RequestParam(name = "pageSize", defaultValue = "-1") int pageSize) {

        GetBusinessContextListRequest request = new GetBusinessContextListRequest(
                authenticationService.asScoreUser(requester));

        request.setName(name);
        request.setBusinessContextIdList(!StringUtils.hasLength(businessContextIdList) ? Collections.emptyList() :
                Arrays.asList(businessContextIdList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).map(e -> new BigInteger(e)).collect(Collectors.toList()));
        if (topLevelAsbiepId != null) {
            request.setTopLevelAsbiepIdList(Arrays.asList(topLevelAsbiepId));
        }
        request.setUpdaterUsernameList(!StringUtils.hasLength(updaterUsernameList) ? Collections.emptyList() :
                Arrays.asList(updaterUsernameList.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));
        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Timestamp(Long.valueOf(updateStart)).toLocalDateTime());
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(new Timestamp(calendar.getTimeInMillis()).toLocalDateTime());
        }
        request.setTenantId(tenantId);
        request.setNotConnectedToTenant(notConnectedToTenant != null ? notConnectedToTenant : false);
        
        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActive(sortActive);
        request.setSortDirection("asc".equalsIgnoreCase(sortDirection) ? ASC : DESC);

        GetBusinessContextListResponse response = businessContextService.getBusinessContextList(request, configService.isTenantInstance());

		List<BusinessContext> ctxs = response.getResults();
		if (configService.isTenantInstance()) {
			ctxs.forEach(c -> {

				List<String> names = tenantService.getTenantNameByBusinessCtxId(c.getBusinessContextId().longValue());
				String tenant = names.stream().map(Object::toString).collect(Collectors.joining(","));
				c.setConnectedTenantNames(tenant);
			});
		}

        PageResponse<BusinessContext> pageResponse = new PageResponse<>();
        pageResponse.setList(ctxs);
        pageResponse.setPage(response.getPage());
        pageResponse.setSize(response.getSize());
        pageResponse.setLength(response.getLength());
        return pageResponse;
    }

    @RequestMapping(value = "/business_context/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BusinessContext getBusinessContext(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger businessContextId) {

        GetBusinessContextRequest request =
                new GetBusinessContextRequest(authenticationService.asScoreUser(requester))
                        .withBusinessContextId(businessContextId);

        GetBusinessContextResponse response =
                businessContextService.getBusinessContext(request);

        return response.getBusinessContext();
    }

    @RequestMapping(value = "/business_context_values", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BusinessContextValue> getBusinessContextValues(
            @AuthenticationPrincipal AuthenticatedPrincipal requester) {

        GetBusinessContextListRequest request =
                new GetBusinessContextListRequest(authenticationService.asScoreUser(requester));
        request.setPageIndex(-1);
        request.setPageSize(-1);

        GetBusinessContextListResponse response = businessContextService.getBusinessContextList(request, false);
        return response.getResults().stream()
                .flatMap(e -> e.getBusinessContextValueList().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/business_context_values_from_biz_ctx/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BusinessContextValue> getBusinessCtxValuesFromBizCtx(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger businessContextId) {

        GetBusinessContextRequest request =
                new GetBusinessContextRequest(authenticationService.asScoreUser(requester))
                        .withBusinessContextId(businessContextId);

        GetBusinessContextResponse response =
                businessContextService.getBusinessContext(request);

        return response.getBusinessContext().getBusinessContextValueList();
    }

    @RequestMapping(value = "/business_context", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BusinessContext businessContext) {

        CreateBusinessContextRequest request =
                new CreateBusinessContextRequest(authenticationService.asScoreUser(requester));
        request.setName(businessContext.getName());
        request.setBusinessContextValueList(businessContext.getBusinessContextValueList());

        CreateBusinessContextResponse response =
                businessContextService.createBusinessContext(request);

        if (response.getBusinessContextId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/business_context/{id}", method = RequestMethod.POST)
    public ResponseEntity update(
            @PathVariable("id") BigInteger businessContextId,
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody BusinessContext businessContext) {

        UpdateBusinessContextRequest request =
                new UpdateBusinessContextRequest(authenticationService.asScoreUser(requester))
                        .withBusinessContextId(businessContextId)
                        .withName(businessContext.getName())
                        .withBusinessContextValueList(businessContext.getBusinessContextValueList());

        UpdateBusinessContextResponse response =
                businessContextService.updateBusinessContext(request);

        if (response.getBusinessContextId() != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/business_context/{id}", method = RequestMethod.PUT)
    public ResponseEntity assign(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger businessContextId,
            @RequestParam(name = "topLevelAsbiepId", required = true) BigInteger topLevelAsbiepId) {

        businessContextService.assign(businessContextId, topLevelAsbiepId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/business_context/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @PathVariable("id") BigInteger businessContextId,
            @RequestParam(name = "topLevelAsbiepId", required = false) BigInteger topLevelAsbiepId) {

        if (topLevelAsbiepId != null) {
            businessContextService.dismiss(businessContextId, topLevelAsbiepId);
            return ResponseEntity.noContent().build();
        } else {
            DeleteBusinessContextRequest request =
                    new DeleteBusinessContextRequest(authenticationService.asScoreUser(requester))
                            .withBusinessContextIdList(Arrays.asList(businessContextId));

            DeleteBusinessContextResponse response =
                    businessContextService.deleteBusinessContext(request);

            if (response.contains(businessContextId)) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

    }

    public static class DeleteBusinessContextRequestData {
        private List<BigInteger> businessContextIdList = Collections.emptyList();

        public List<BigInteger> getBusinessContextIdList() {
            return businessContextIdList;
        }

        public void setBusinessContextIdList(List<BigInteger> businessContextIdList) {
            this.businessContextIdList = businessContextIdList;
        }
    }

    @RequestMapping(value = "/business_context/delete", method = RequestMethod.POST)
    public ResponseEntity deletes(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestBody DeleteBusinessContextRequestData requestData) {
        DeleteBusinessContextRequest request =
                new DeleteBusinessContextRequest(authenticationService.asScoreUser(requester))
                        .withBusinessContextIdList(requestData.getBusinessContextIdList());

        DeleteBusinessContextResponse response =
                businessContextService.deleteBusinessContext(request);

        if (response.containsAll(requestData.getBusinessContextIdList())) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
