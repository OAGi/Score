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
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@ManagedBean
@ViewScoped
public class BusinessContextHandler implements Serializable {

	private static final long serialVersionUID = 8706516047982751653L;
	
	private DAOFactory df;
	private SRTDAO dao;
	private SRTDAO daoCC;
	private SRTDAO daoCS;
	private SRTDAO daoCV;
	private ContextCategoryVO selected;
	private ContextSchemeVO selected1;
	private List<ContextSchemeValueVO> selected2;

	@PostConstruct
	private void init() {
		try {
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("BusinessContext");
			daoCC = df.getDAO("ContextCategory");
			daoCS = df.getDAO("ContextScheme");
			daoCV = df.getDAO("ContextSchemeValue");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String name;
	private String ccName;

	private List<SRTObject> contextCategories = new ArrayList<SRTObject>();
	private List<SRTObject> contextSchemes = new ArrayList<SRTObject>();
	private List<SRTObject> contextValues = new ArrayList<SRTObject>();
	
	public void chooseCV() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("business_context_select_cv", options, null);
    }
	
	public void closeDialog() {
		System.out.println("######## close!!!");
        RequestContext.getCurrentInstance().closeDialog(null);
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

	public void createContextCategory() {
		try {
			ContextCategoryVO ccVO = new ContextCategoryVO();
			ccVO.setName(this.name);
			ccVO.setContextCategoryGUID(Utility.generateGUID());
			daoCC.insertObject(ccVO);
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
 
    public void onRowUnselect2(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ContextCategoryVO) event.getObject()).getContextCategoryID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onRowSelect2(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ContextCategoryVO) event.getObject()).getName(), String.valueOf(((ContextCategoryVO) event.getObject()).getContextCategoryID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if(event.getObject() instanceof ContextCategoryVO) {
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
}