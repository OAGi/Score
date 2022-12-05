package org.oagi.score.repo.component.tenant;

import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BIZ_CTX;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.TENANT;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.TENANT_BUSINESS_CTX;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.USER_TENANT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.SelectConnectByStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectWithTiesAfterOffsetStep;
import org.jooq.SortField;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.tenant.data.BusinessTenantContext;
import org.oagi.score.gateway.http.api.tenant.data.Tenant;
import org.oagi.score.gateway.http.api.tenant.data.TenantBusinessCtxInfo;
import org.oagi.score.gateway.http.api.tenant.data.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant.data.TenantUserInfo;
import org.oagi.score.gateway.http.api.tenant.data.UserTenantInfo;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TenantBusinessCtxRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.TenantRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.UserTenantRecord;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;;


@Repository
public class TenantRepository {
	
	  @Autowired
	  private DSLContext dslContext;
	  
	  public List<ULong> getUserTenantsRoleByUserId(ULong userId) {
		  return dslContext.select(
				  		TENANT.TENANT_ID)
				  		.from(TENANT)
				  		.join(USER_TENANT)
				  		.on(TENANT.TENANT_ID.eq(USER_TENANT.TENANT_ID))
				        .where(USER_TENANT.APP_USER_ID.eq(userId))
				        .fetch(TENANT.TENANT_ID);
	  }
	  

		public Tenant getTenantById(Long tenantId) {
			return dslContext.select(
			  		TENANT.TENANT_ID, 
			  		TENANT.NAME)
			  		.from(TENANT)
			        .where(TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
			        .fetchOneInto(Tenant.class);
		}

	  public PageResponse<Tenant> getAllTenantsRole(TenantListRequest tenantRequest){
		  		  
		  PageResponse<Tenant>  response = new PageResponse<>();
		  SelectJoinStep<Record2<ULong, String>> step = null;
		  SelectConnectByStep<Record2<ULong, String>> conditionStep = null;
		  SelectWithTiesAfterOffsetStep<Record2<ULong, String>> offsetStep = null;
		  int pageCount = 0;
		  
		  step = dslContext.select(
				  TENANT.TENANT_ID,
				  TENANT.NAME)
				  .from(TENANT);
		  
		  List<Condition> conditions = new ArrayList();
		  if(StringUtils.hasLength(tenantRequest.getName())) {
			  conditions.addAll(contains(tenantRequest.getName(), TENANT.NAME));
		  }
		  conditionStep =  step.where(conditions);
		  pageCount = dslContext.fetchCount(conditionStep);
		  SortField sortField = TENANT.NAME.asc();
		  
		  if ("asc".equals(tenantRequest.getPageRequest().getSortDirection())) {
              sortField = TENANT.NAME.asc();
          } else if ("desc".equals(tenantRequest.getPageRequest().getSortDirection())) {
              sortField = TENANT.NAME.desc();
          }
		  
		  offsetStep = conditionStep.orderBy(sortField)
                  .limit(tenantRequest.getPageRequest().getOffset(), tenantRequest.getPageRequest()
                		  .getPageSize());
		  
		 response.setList((offsetStep != null) ?
		                        offsetStep.fetchInto(Tenant.class) : 
		                        	conditionStep.fetchInto(Tenant.class));
		 response.setPage(tenantRequest.getPageRequest().getPageIndex());
	     response.setSize(tenantRequest.getPageRequest().getPageSize());
	     response.setLength(pageCount);
	     return response;
		  
	  }


	public void createTenant(String name) {
		 ULong tenantId =  dslContext.select(
				  TENANT.TENANT_ID)
				  .from(TENANT)
				  .where(TENANT.NAME.eq(name))
				  .fetchOne(TENANT.TENANT_ID);
		
		if(tenantId != null) {
			return;
		}
		
		TenantRecord record = new TenantRecord();
		record.setName(name);
		dslContext.insertInto(TENANT)
        .set(record)
        .returning().fetchOne().getTenantId().longValue();	
   }

	public TenantBusinessCtxInfo getTenantBusinessCxtInfoById(Long tenantId) {
		TenantBusinessCtxInfo info = dslContext.select(
				  TENANT.TENANT_ID,
				  TENANT.NAME)
				  .from(TENANT)
				  .where(TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
				  .fetchOneInto(TenantBusinessCtxInfo.class);
		if(info != null) {
			List<BusinessTenantContext> tenantCtxs = new ArrayList<>();
			tenantCtxs = getTenantBusinessCtxs(tenantId);			
			
			List<BusinessTenantContext> ctxsWithoutTenant = new ArrayList<>();
			ctxsWithoutTenant = getBusinessCtxsNotAssociatedWithTenant(tenantId);
			
			if(ctxsWithoutTenant != null && !ctxsWithoutTenant.isEmpty()) {
				tenantCtxs.addAll(ctxsWithoutTenant);
			}
			info.setBusinessContext(tenantCtxs);
		}
		return info;
	}
	
	public boolean updateTenantBusinessContext(TenantBusinessCtxInfo tenantContextInfo) {
		Tenant tenant = dslContext.select(
				  TENANT.TENANT_ID,
				  TENANT.NAME)
				  .from(TENANT)
				  .where(TENANT.TENANT_ID.eq(ULong.valueOf(tenantContextInfo.getTenantId())))
				  .fetchOneInto(Tenant.class);
		
		boolean result = false;
		
		if(tenant != null) {
			tenantContextInfo.getBusinessContext().forEach( c->{
				if(c.isChecked()) {
					connectTenantToBusinessCtx(tenantContextInfo.getTenantId(), c);
				}else {
					disconnectTenantToBusinessCtx(tenantContextInfo.getTenantId(), c);
				}
			});		
			result = true;
		}
		
		return result;
	}	
	
	public void addUserToTenant(Long tenantId, Long appUserId) {
		if(!checkIfUserTenantExists(tenantId, appUserId)) {
			UserTenantRecord record = new UserTenantRecord();
			record.setTenantId(ULong.valueOf(tenantId));
			record.setAppUserId(ULong.valueOf(appUserId));
			dslContext.insertInto(USER_TENANT)
	                .set(record)
	                .returning().fetchOne().getUserTenantId().longValue();
		}
	}
	
	public void deleteTenantUser(Long tenantId, Long appUserId) {
		if(checkIfUserTenantExists(tenantId, appUserId)) {
			dslContext.deleteFrom(USER_TENANT)
			.where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
			.and(USER_TENANT.APP_USER_ID.eq(ULong.valueOf(appUserId)))
			.execute();
		}
	}

	private List<BusinessTenantContext> getBusinessCtxsNotAssociatedWithTenant(Long tenantId) {
		List<BusinessTenantContext> ctxsWithoutTenant;
		ctxsWithoutTenant = dslContext.select(
				BIZ_CTX.BIZ_CTX_ID,
				BIZ_CTX.NAME)
				.from(BIZ_CTX)
				.where(BIZ_CTX.BIZ_CTX_ID.notIn(
						dslContext.select(TENANT_BUSINESS_CTX.BIZ_CTX_ID)
						.from(TENANT_BUSINESS_CTX)
						.where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
						))
				.fetchStream()
				.map( e->{ 
					BusinessTenantContext context = new BusinessTenantContext();
					context.setBusinessCtxId(e.get(BIZ_CTX.BIZ_CTX_ID).longValue());
					context.setName(e.get(BIZ_CTX.NAME));
					return context;
				}).collect(Collectors.toList());
		return ctxsWithoutTenant;
	}	

	private List<BusinessTenantContext> getTenantBusinessCtxs(Long tenantId) {
		List<BusinessTenantContext> tenantCtxs;
		tenantCtxs = dslContext.select(
				BIZ_CTX.BIZ_CTX_ID,
				BIZ_CTX.NAME)
				.from(TENANT_BUSINESS_CTX)
				.join(BIZ_CTX)
				.on(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(BIZ_CTX.BIZ_CTX_ID))
				.where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
				.fetchStream()
				.map( e->{ 
					BusinessTenantContext context = new BusinessTenantContext();
					context.setBusinessCtxId(e.get(BIZ_CTX.BIZ_CTX_ID).longValue());
					context.setName(e.get(BIZ_CTX.NAME));
					context.setChecked(true);
					return context;
				}).collect(Collectors.toList());
		return tenantCtxs;
	}


	private void disconnectTenantToBusinessCtx(Long tenantId, BusinessTenantContext context) {
		if(checkIfTenantCtxExists(tenantId, context)) {
			dslContext.deleteFrom(TENANT_BUSINESS_CTX)
			.where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
			.and(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(ULong.valueOf(context.getBusinessCtxId())))
			.execute();
		}
	}

	private void connectTenantToBusinessCtx(Long tenantId, BusinessTenantContext context) {
		
		if(!checkIfTenantCtxExists(tenantId, context)) {
			TenantBusinessCtxRecord record = new TenantBusinessCtxRecord();
			record.setTenantId(ULong.valueOf(tenantId));
			record.setBizCtxId(ULong.valueOf(context.getBusinessCtxId()));
			dslContext.insertInto(TENANT_BUSINESS_CTX)
	                .set(record)
	                .returning().fetchOne().getTenantBusinessCtxId().longValue();
		}
	}

	private boolean checkIfTenantCtxExists(Long tenantId, BusinessTenantContext context) {
		ULong tenantBusinessContextId = dslContext.select(
				TENANT_BUSINESS_CTX.TENANT_BUSINESS_CTX_ID)
				.from(TENANT_BUSINESS_CTX)
				.where(TENANT_BUSINESS_CTX.TENANT_ID.eq(ULong.valueOf(tenantId)))
				.and(TENANT_BUSINESS_CTX.BIZ_CTX_ID.eq(ULong.valueOf(context.getBusinessCtxId())))
				.fetchOne(TENANT_BUSINESS_CTX.TENANT_BUSINESS_CTX_ID);
		return tenantBusinessContextId != null ? true : false;
	}
	
	private boolean checkIfUserTenantExists(Long tenantId, Long addUserId) {
		ULong userTenant = dslContext.select(
				USER_TENANT.USER_TENANT_ID)
				.from(USER_TENANT)
				.where(USER_TENANT.TENANT_ID.eq(ULong.valueOf(tenantId)))
				.and(USER_TENANT.APP_USER_ID.eq(ULong.valueOf(addUserId)))
				.fetchOne(USER_TENANT.USER_TENANT_ID);
		return userTenant != null ? true : false;
	}

}
