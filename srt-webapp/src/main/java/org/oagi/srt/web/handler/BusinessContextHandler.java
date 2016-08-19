package org.oagi.srt.web.handler;

import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessContextService;
import org.oagi.srt.service.ContextCategoryService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class BusinessContextHandler extends UIHandler implements Serializable {

	private static final long serialVersionUID = 8706516047982751653L;

	@Autowired
	private BusinessContextService businessContextService;

	@Autowired
	private ContextCategoryService contextCategoryService;
	
	private String name;
	private String ccName;
	private int bcId;
	protected BusinessContext bcDetail;
	protected ContextCategory selected;
	protected ContextScheme selected1;
	protected List<ContextSchemeValue> selected2;

	private List<ContextCategory> contextCategories = new ArrayList();
	private List<ContextScheme> contextSchemes = new ArrayList();
	private List<BusinessContext> businessContexts = null;
	private List<ContextSchemeValue> contextValues = new ArrayList();
	private List<BusinessContextValues> bcValues = new ArrayList();
	private List<BusinessContextValues> bcDetails = new ArrayList();
	
	private String cValues;

	public void chooseCV() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("business_context_select_cv", options, null);
    }
	
	public class BusinessContextValues {
		private ContextCategory ccVO;
		private ContextScheme csVO;
		private List<ContextSchemeValue> csList;
		private String csValues;
		private ContextSchemeValue csvVO;
		
		public ContextCategory getCcVO() {
			return ccVO;
		}
		public void setCcVO(ContextCategory ccVO) {
			this.ccVO = ccVO;
		}
		public ContextScheme getCsVO() {
			return csVO;
		}
		public void setCsVO(ContextScheme csVO) {
			this.csVO = csVO;
		}
		public List<ContextSchemeValue> getCsList() {
			return csList;
		}
		public void setCsList(List<ContextSchemeValue> csList) {
			this.csList = csList;
		}
		public String getCsValues() {
			return csValues;
		}
		public void setCsValues(String csValues) {
			this.csValues = csValues;
		} 
		public ContextSchemeValue getCsvVO() {
			return csvVO;
		}
		public void setCsvVO(ContextSchemeValue csvVO) {
			this.csvVO = csvVO;
		}
	}

	public void onCCChosen(SelectEvent event) {
		BusinessContextHandler bh = (BusinessContextHandler) event.getObject();
		//bcValues = new ArrayList<BusinessContextValues>();

		BusinessContextValues bcv = new BusinessContextValues();
		bcv.setCcVO(bh.getSelected());
		bcv.setCsVO(bh.getSelected1());
		bcv.setCsList(bh.getSelected2());
		cValues = "";
//		this.selected = bh.getSelected();
//		this.selected1 = bh.getSelected1();
//		this.selected2 = bh.getSelected2();
		for (ContextSchemeValue cVO : bh.getSelected2()) {
			cValues += (cVO.getValue() != null) ? cVO.getValue() + ", " : "";
		}
		if (cValues.length() > 0) {
			cValues = cValues.substring(0, cValues.lastIndexOf(","));
		}
		bcv.setCsValues(cValues);
		bcValues.add(bcv);

		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Context Values are added", "Context Values are added");

		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void onCSChosen(SelectEvent event) {
        ContextScheme aContextScheme = (ContextScheme) event.getObject();
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "ContextScheme Selected", "Id:" + aContextScheme.getCtxSchemeId());
         
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getCcName() {
		return ccName;
	}

	public void setCcName(String ccName) {
		this.ccName = ccName;
	}

	public List<ContextCategory> getContextCategories() {
		return contextCategories;
	}

	public void setContextCategories(List<ContextCategory> contextCategories) {
		this.contextCategories = contextCategories;
	}

	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void createBusinessContext() {
		businessContextService.newBusinessContextBuilder()
				.name(this.name)
				.userId(userId)
				.ctxSchemeValueIds(
						bcValues.stream()
								.flatMap(e -> e.getCsList().stream())
								.map(c -> c.getCtxSchemeValueId())
								.distinct()
								.collect(Collectors.toList()))
				.build();
	}

	public List<String> completeInput(String query) {
		return contextCategoryService.findByNameContaining(query).stream()
				.map(e -> e.getName())
				.distinct()
				.collect(Collectors.toList());
	}
	
	public void search() {
		contextCategories = contextCategoryService.findByNameContaining(getCcName());
	}
	
	public ContextCategory getSelected() {
        return selected;
    }
 
    public void setSelected(ContextCategory selected) {
        this.selected = selected;
    }
    
    public ContextScheme getSelected1() {
        return selected1;
    }
 
    public void setSelected1(ContextScheme selected1) {
        this.selected1 = selected1;
    }
    
    public List<ContextSchemeValue> getSelected2() {
        return selected2;
    }
 
    public void setSelected2(List<ContextSchemeValue> selected2) {
        this.selected2 = selected2;
    }

	public void onRowSelect(SelectEvent event) {
		ContextCategory contextCategory = (ContextCategory) event.getObject();
        FacesMessage msg = new FacesMessage(contextCategory.getName(),
				String.valueOf(contextCategory.getCtxCategoryId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);

		selected = contextCategory;
		contextSchemes = contextCategoryService.findByCtxCategoryId(contextCategory.getCtxCategoryId());
		contextValues = new ArrayList();
		selected1 = null;
		selected2 = null;
    }
 
    public void onRowUnselect(UnselectEvent event) {
		ContextCategory contextCategory = (ContextCategory) event.getObject();
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(contextCategory.getCtxCategoryId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onRowSelect1(SelectEvent event) {
		ContextScheme contextScheme = (ContextScheme) event.getObject();
        FacesMessage msg = new FacesMessage(contextScheme.getSchemeName(), String.valueOf(contextScheme.getCtxSchemeId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);

		selected1 = contextScheme;
		contextValues = contextCategoryService.findByOwnerCtxSchemeId(contextScheme.getCtxSchemeId());
    }
    
    public void onRowUnselect1(UnselectEvent event) {
		ContextCategory contextCategory = (ContextCategory) event.getObject();
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(contextCategory.getCtxCategoryId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
	public List<ContextScheme> getContextSchemes() {
		return contextSchemes;
	}

	public void setContextSchemes(List<ContextScheme> contextSchemes) {
		this.contextSchemes = contextSchemes;
	}

	public List<ContextSchemeValue> getContextValues() {
		return contextValues;
	}

	public void setContextValues(List<ContextSchemeValue> contextValues) {
		this.contextValues = contextValues;
	}

	public String getcValues() {
		return cValues;
	}

	public void setcValues(String cValues) {
		this.cValues = cValues;
	}

	public List<BusinessContextValues> getBcValues() {
		return bcValues;
	}

	public void setBcValues(List<BusinessContextValues> bcValues) {
		this.bcValues = bcValues;
	}

	public List<BusinessContext> getBusinessContexts() {
		if (businessContexts == null)
			businessContexts = businessContextService.findAll(Sort.Direction.DESC, "creationTimestamp");
		return businessContexts;
	}

	public void setBusinessContexts(List<BusinessContext> businessContexts) {
		this.businessContexts = businessContexts;
	}

	public BusinessContext getBcDetail() {
		bcDetails = new ArrayList();
		if (bcDetail != null) {
			bcId = bcDetail.getBizCtxId();
			List<BusinessContextValue> bcvVOList = businessContextService.findByBizCtxId(bcDetail.getBizCtxId());
			for (BusinessContextValue businessContextValue : bcvVOList) {
				BusinessContextValues businessContextValues = new BusinessContextValues();

				ContextSchemeValue contextSchemeValue = contextCategoryService.findContextSchemeValueById(businessContextValue.getCtxSchemeValueId());
				businessContextValues.setCsvVO(contextSchemeValue);

				ContextScheme contextScheme = contextCategoryService.findContextSchemeById(contextSchemeValue.getOwnerCtxSchemeId());
				businessContextValues.setCsVO(contextScheme);

				ContextCategory contextCategory = contextCategoryService.findContextCategoryById(contextScheme.getCtxCategoryId());
				businessContextValues.setCcVO(contextCategory);

				bcDetails.add(businessContextValues);
			}
		}
		return bcDetail;
	}

	public void setBcDetail(BusinessContext bcDetail) {
		this.bcDetail = bcDetail;
	}

	public List<BusinessContextValues> getBcDetails() {
		return bcDetails;
	}

	public void setBcDetails(List<BusinessContextValues> bcDetails) {
		this.bcDetails = bcDetails;
	}
	
}