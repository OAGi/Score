package org.oagi.srt.web.handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.BusinessContextVO;
import org.oagi.srt.persistence.dto.BusinessContextValueVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@ManagedBean
@ViewScoped
public class BusinessContextHandler implements Serializable {

	private static final long serialVersionUID = 8706516047982751653L;
	
	private DAOFactory df;
	private SRTDAO daoBC;
	private SRTDAO daoBCV;
	private SRTDAO daoCC;
	private SRTDAO daoCS;
	private SRTDAO daoCV;
	private SRTDAO daoUser;
	private ContextCategoryVO selected;
	private ContextSchemeVO selected1;
	private List<ContextSchemeValueVO> selected2;
	private BusinessContextVO bcDetail;
	private int userId;

	@PostConstruct
	private void init() {
		try {
			df = DAOFactory.getDAOFactory();
			daoBC = df.getDAO("BusinessContext");
			daoBCV = df.getDAO("BusinessContextValue");
			daoCC = df.getDAO("ContextCategory");
			daoCS = df.getDAO("ContextScheme");
			daoCV = df.getDAO("ContextSchemeValue");
			daoUser = df.getDAO("User");
			
			QueryCondition qc = new QueryCondition();
			qc.add("user_name", "oagis");
			userId = ((UserVO)daoUser.findObject(qc)).getUserID();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String name;
	private String ccName;
	private int bcId;

	private List<SRTObject> contextCategories = new ArrayList<SRTObject>();
	private List<SRTObject> contextSchemes = new ArrayList<SRTObject>();
	private List<SRTObject> businessContexts = null;
	private List<SRTObject> contextValues = new ArrayList<SRTObject>();
	private List<BusinessContextValues> bcValues = new ArrayList<BusinessContextValues>();
	private List<BusinessContextValues> bcDetails = new ArrayList<BusinessContextValues>();
	
	private String cValues;
	
	public void chooseCV() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("business_context_select_cv", options, null);
    }
	
	public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }
	
	public class BusinessContextValues {
		private ContextCategoryVO ccVO;
		private ContextSchemeVO csVO;
		private List<ContextSchemeValueVO> csList;
		private String csValues;
		private ContextSchemeValueVO csvVO;
		
		public ContextCategoryVO getCcVO() {
			return ccVO;
		}
		public void setCcVO(ContextCategoryVO ccVO) {
			this.ccVO = ccVO;
		}
		public ContextSchemeVO getCsVO() {
			return csVO;
		}
		public void setCsVO(ContextSchemeVO csVO) {
			this.csVO = csVO;
		}
		public List<ContextSchemeValueVO> getCsList() {
			return csList;
		}
		public void setCsList(List<ContextSchemeValueVO> csList) {
			this.csList = csList;
		}
		public String getCsValues() {
			return csValues;
		}
		public void setCsValues(String csValues) {
			this.csValues = csValues;
		} 
		public ContextSchemeValueVO getCsvVO() {
			return csvVO;
		}
		public void setCsvVO(ContextSchemeValueVO csvVO) {
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
		for(ContextSchemeValueVO cVO : bh.getSelected2()) {
			cValues += (cVO.getValue() != null) ? cVO.getValue() + ", " : "";
		}
		cValues = cValues.substring(0, cValues.lastIndexOf(","));
		bcv.setCsValues(cValues);
		bcValues.add(bcv);
		
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Context Values are added", "Context Values are added");
         
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
     
    public void onCSChosen(SelectEvent event) {
        ContextSchemeVO aContextSchemeVO = (ContextSchemeVO) event.getObject();
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "ContextScheme Selected", "Id:" + aContextSchemeVO.getContextSchemeID());
         
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

	public List<SRTObject> getContextCategories() {
		return contextCategories;
	}

	public void setContextCategories(List<SRTObject> contextCategories) {
		this.contextCategories = contextCategories;
	}

	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void createBusinessContext() {
		try {
			BusinessContextVO bcVO = new BusinessContextVO();
			bcVO.setName(this.name);
			String guid = Utility.generateGUID();
			bcVO.setBusinessContextGUID(guid);
			bcVO.setCreatedByUserId(userId);
			bcVO.setLastUpdatedByUserId(userId);
			daoBC.insertObject(bcVO);
			
			QueryCondition qc = new QueryCondition();
			qc.add("Business_Context_GUID", guid);
			BusinessContextVO bvVO1 = (BusinessContextVO)daoBC.findObject(qc);
			
			for(BusinessContextValues bcv : bcValues) {
				for(ContextSchemeValueVO cVO : bcv.getCsList()) {
					BusinessContextValueVO bcvVO = new BusinessContextValueVO();
					bcvVO.setBusinessContextID(bvVO1.getBusinessContextID());
					bcvVO.setContextSchemeValueID(cVO.getContextSchemeValueID());
					daoBCV.insertObject(bcvVO);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			contextCategories = daoCC.findObjects();
			for(SRTObject obj : contextCategories) {
				ContextCategoryVO ccVO = (ContextCategoryVO)obj;
				if(ccVO.getName().contains(query)) {
					results.add(ccVO.getName());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public void search() {
		try {
			QueryCondition qc = new QueryCondition();
			qc.addLikeClause("name", "%" + getCcName() + "%");
			contextCategories = daoCC.findObjects(qc);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public ContextCategoryVO getSelected() {
        return selected;
    }
 
    public void setSelected(ContextCategoryVO selected) {
        this.selected = selected;
    }
    
    public ContextSchemeVO getSelected1() {
        return selected1;
    }
 
    public void setSelected1(ContextSchemeVO selected1) {
        this.selected1 = selected1;
    }
    
    public List<ContextSchemeValueVO> getSelected2() {
        return selected2;
    }
 
    public void setSelected2(List<ContextSchemeValueVO> selected2) {
        this.selected2 = selected2;
    }

	public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ContextCategoryVO) event.getObject()).getName(), String.valueOf(((ContextCategoryVO) event.getObject()).getContextCategoryID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if(event.getObject() instanceof ContextCategoryVO) {
        	selected = (ContextCategoryVO) event.getObject();
        	QueryCondition qc = new QueryCondition();
        	qc.add("Context_Category_ID", selected.getContextCategoryID());
        	try {
        		contextSchemes = daoCS.findObjects(qc);
        		contextValues = new ArrayList<SRTObject>();
        		selected1 = null;
        		selected2 = null;
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
        } else {
        }
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ContextCategoryVO) event.getObject()).getContextCategoryID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onRowSelect1(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ContextSchemeVO) event.getObject()).getSchemeName(), String.valueOf(((ContextSchemeVO) event.getObject()).getContextSchemeID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if(event.getObject() instanceof ContextSchemeVO) {
        	selected1 = (ContextSchemeVO) event.getObject();
        	QueryCondition qc = new QueryCondition();
        	qc.add("Owner_Context_Scheme_ID", selected1.getContextSchemeID());
        	try {
        		contextValues = daoCV.findObjects(qc);
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
        } else {
        }
    }
    
    public void onRowUnselect1(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ContextCategoryVO) event.getObject()).getContextCategoryID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
	public List<SRTObject> getContextSchemes() {
		return contextSchemes;
	}

	public void setContextSchemes(List<SRTObject> contextSchemes) {
		this.contextSchemes = contextSchemes;
	}

	public List<SRTObject> getContextValues() {
		return contextValues;
	}

	public void setContextValues(List<SRTObject> contextValues) {
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

	public List<SRTObject> getBusinessContexts() {
		try {
			if(businessContexts == null)
				businessContexts = daoBC.findObjects();
			//bcDetail = null;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return businessContexts;
	}

	public void setBusinessContexts(List<SRTObject> businessContexts) {
		this.businessContexts = businessContexts;
	}

	public BusinessContextVO getBcDetail() {
		bcDetails = new ArrayList<BusinessContextValues>();
		if(bcDetail != null) {
			bcId = bcDetail.getBusinessContextID();
			QueryCondition qc = new QueryCondition();
			qc.add("Business_Context_ID", bcDetail.getBusinessContextID());
			try {
				BusinessContextVO bcVO = (BusinessContextVO)daoBC.findObject(qc);
				qc = new QueryCondition();
				qc.add("Business_Context_ID", bcDetail.getBusinessContextID());
				
				List<SRTObject> bcvVOList = daoBCV.findObjects(qc);
				for(SRTObject sVO : bcvVOList) {
					BusinessContextValues bcv = new BusinessContextValues();
					BusinessContextValueVO bcvVO = (BusinessContextValueVO)sVO;
					qc = new QueryCondition();
					qc.add("Context_Scheme_Value_ID", bcvVO.getContextSchemeValueID());
					ContextSchemeValueVO  csvVO = (ContextSchemeValueVO)daoCV.findObject(qc);
					bcv.setCsvVO(csvVO);
					
					qc = new QueryCondition();
					qc.add("Classification_Context_Scheme_ID", csvVO.getOwnerContextSchemeID());
					ContextSchemeVO csVO = (ContextSchemeVO)daoCS.findObject(qc);
					bcv.setCsVO(csVO);
					
					qc = new QueryCondition();
					qc.add("Context_Category_ID", csVO.getContextCategoryID());
					ContextCategoryVO ccVO = (ContextCategoryVO)daoCC.findObject(qc);
					bcv.setCcVO(ccVO);
					
					bcDetails.add(bcv);
				}
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
		}
		return bcDetail;
	}

	public void setBcDetail(BusinessContextVO bcDetail) {
		this.bcDetail = bcDetail;
	}

	public List<BusinessContextValues> getBcDetails() {
		return bcDetails;
	}

	public void setBcDetails(List<BusinessContextValues> bcDetails) {
		this.bcDetails = bcDetails;
	}
	
}