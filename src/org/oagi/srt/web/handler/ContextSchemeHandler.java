package org.oagi.srt.web.handler;

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
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;
import org.oagi.srt.web.handler.BusinessContextHandler.BusinessContextValues;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

@ManagedBean
@ViewScoped
public class ContextSchemeHandler {

	private DAOFactory df;
	private SRTDAO daoCS;
	private SRTDAO daoCSV;
	private SRTDAO daoCC;

	@PostConstruct
	private void init() {
		try {
			df = DAOFactory.getDAOFactory();
			daoCS = df.getDAO("ContextScheme");
			daoCSV = df.getDAO("ContextSchemeValue");
			daoCC = df.getDAO("ContextCategory");
			System.out.println("### Called");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String value;
	private String meaning;
	private String name;
	private String description;
	private String schemeAgencyID;
	private String schemeAgencyName;
	private String schemeVersion;
	private int contextCategoryID;
	private int id;
	private String contextCategoryNameDesc; 
	private SRTObject selectedScheme;
	private String schemeName;
	private String guid;
	private String schemeId;
	
	private List<SRTObject> csValues = new ArrayList<SRTObject>();
	private List<SRTObject> selectedCSValues = new ArrayList<SRTObject>();
	//private List<SRTObject> contextCategories;
	private Map<String,String> contextCategories = new HashMap<String, String>();
	private List<SRTObject> contextSchemes;
	
	public List<SRTObject> getSelectedCSValues() {
		if(selectedScheme != null) {
			QueryCondition qc = new QueryCondition();
			qc.add("owner_context_scheme_id", ((ContextSchemeVO)selectedScheme).getContextSchemeID());
			try {
				selectedCSValues = daoCSV.findObjects(qc);
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
		}
		
		return selectedCSValues;
	}

	public void setSelectedCSValues(List<SRTObject> selectedCSValues) {
		this.selectedCSValues = selectedCSValues;
	}

	public void cancel() {
    	this.selectedScheme = null;
    }
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSchemeName() {
		return schemeName;
	}

	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getSchemeId() {
		return schemeId;
	}

	public void setSchemeId(String schemeId) {
		this.schemeId = schemeId;
	}

	public void onContextCategoryChange() {
		System.out.println("######### " + contextCategoryNameDesc);
    }
	
	public SRTObject getSelectedScheme() {
		return selectedScheme;
	}

	public void setSelectedScheme(SRTObject selectedScheme) {
		this.selectedScheme = selectedScheme;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setCsValues(List<SRTObject> csValues) {
		this.csValues = csValues;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}
	
	public void onEdit(SRTObject obj) {
    	ContextSchemeVO cVO = (ContextSchemeVO) obj;
    	description = cVO.getDescription();
    	schemeAgencyID = cVO.getSchemeAgencyID();
    	schemeAgencyName = cVO.getSchemeAgencyName();
    	schemeVersion = cVO.getSchemeVersion();
    	contextCategoryID = cVO.getContextCategoryID();
    	id = cVO.getContextSchemeID();
    	schemeName = cVO.getSchemeName();
    	guid = cVO.getSchemeGUID();
    	schemeId = cVO.getSchemeID();
    	
		csValues = selectedCSValues;
    }
	
	public void save() {
		ContextSchemeVO ccVO = new ContextSchemeVO();
		ccVO.setDescription(description);
		ccVO.setSchemeAgencyID(schemeAgencyID);
		ccVO.setSchemeAgencyName(schemeAgencyName);
		ccVO.setSchemeVersion(schemeVersion);
		ccVO.setContextCategoryID(contextCategoryID);
		ccVO.setContextSchemeID(id);
		ccVO.setSchemeName(schemeName);
		ccVO.setSchemeGUID(guid);
		ccVO.setSchemeID(schemeId);
		
		try {
			daoCS.updateObject(ccVO);
			contextSchemes = daoCS.findObjects();
			
			QueryCondition qc = new QueryCondition();
			qc.add("owner_context_scheme_id", ccVO.getContextSchemeID());
			for(SRTObject obj : daoCSV.findObjects(qc)) {
				daoCSV.deleteObject(((ContextSchemeValueVO)obj));
			}
			
			for(SRTObject obj : csValues) {
				ContextSchemeValueVO vo = (ContextSchemeValueVO) obj;
				vo.setOwnerContextSchemeID(ccVO.getContextSchemeID());
				daoCSV.insertObject(vo);
			}
			
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		this.selectedScheme = ccVO;
    }

	public Map<String, String> getContextCategories() {

		try {
			List<SRTObject> ccs = daoCC.findObjects();
			for(SRTObject obj : ccs) {
				ContextCategoryVO cc = (ContextCategoryVO)obj;
				contextCategories.put(cc.getName(), String.valueOf(cc.getContextCategoryID()));
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}

		return contextCategories;
	}

	public void setContextCategories(Map<String, String> contextCategories) {
		this.contextCategories = contextCategories;
	}

	public void search() {
		addMessage("Coming soon!!!");
	}
	
	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void createContextScheme() {
		try {
			ContextSchemeVO ccVO = new ContextSchemeVO();
			String guid = Utility.generateGUID();
			ccVO.setSchemeName(name);
			ccVO.setDescription(description);
			ccVO.setSchemeGUID(guid);
			ccVO.setSchemeID(Utility.generateGUID());
			ccVO.setSchemeAgencyID(schemeAgencyID);
			ccVO.setSchemeAgencyName(schemeAgencyName);
			ccVO.setSchemeVersion(schemeVersion);
			ccVO.setContextCategoryID(Integer.valueOf(contextCategoryNameDesc));
			daoCS.insertObject(ccVO);
			
			QueryCondition qc = new QueryCondition();
			qc.add("context_scheme_guid", guid);
			ContextSchemeVO cVO = (ContextSchemeVO)daoCS.findObject(qc);
			
			for(SRTObject obj : csValues) {
				ContextSchemeValueVO vo = (ContextSchemeValueVO) obj;
				vo.setOwnerContextSchemeID(cVO.getContextSchemeID());
				daoCSV.insertObject(vo);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			contextSchemes = daoCS.findObjects();
			for(SRTObject obj : contextSchemes) {
				ContextSchemeVO ccVO = (ContextSchemeVO)obj;
				if(ccVO.getSchemeName().contains(query)) {
					results.add(ccVO.getSchemeName());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public void addSchemeValue() {
		ContextSchemeValueVO csVO = new ContextSchemeValueVO();
		csVO.setValue(getValue());
		csVO.setMeaning(getMeaning());
		csVO.setContextSchemeValueGUID(Utility.generateGUID());
		setValue("");
		setMeaning("");
		setCsValues(csVO);
	}
	
	public void deleteCSV(String guid) {
		List<SRTObject> temp = new ArrayList<SRTObject>();
		for(SRTObject obj : csValues) {
			ContextSchemeValueVO vo = (ContextSchemeValueVO)obj;
			if(!vo.getContextSchemeValueGUID().equals(guid))
				temp.add(vo);
		}
		setValue("");
		setMeaning("");
		this.csValues = temp;
	}
	
	public void delete(int id) {
    	ContextSchemeVO ccVO = new ContextSchemeVO();
		ccVO.setContextSchemeID(id);
		setValue("");
		setMeaning("");
		try {
			daoCS.deleteObject(ccVO);
			contextSchemes = daoCS.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
	
	public void setCsValues(SRTObject csVO) {
		csValues.add(csVO);
	}
	
	public List<SRTObject> getCsValues() {
		return csValues;
	}

	public List<String> completeDescription(String query) {
		List<String> results = new ArrayList<String>();

		try {
			contextSchemes = daoCS.findObjects();
			for(SRTObject obj : contextSchemes) {
				ContextCategoryVO ccVO = (ContextCategoryVO)obj;
				if(ccVO.getDescription().contains(query)) {
					results.add(ccVO.getDescription());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}

		return results;
	}

	public String getSchemeAgencyID() {
		return schemeAgencyID;
	}

	public void setSchemeAgencyID(String schemeAgencyID) {
		this.schemeAgencyID = schemeAgencyID;
	}

	public String getSchemeAgencyName() {
		return schemeAgencyName;
	}

	public void setSchemeAgencyName(String schemeAgencyName) {
		this.schemeAgencyName = schemeAgencyName;
	}

	public String getSchemeVersion() {
		return schemeVersion;
	}

	public void setSchemeVersion(String schemeVersion) {
		this.schemeVersion = schemeVersion;
	}

	public int getContextCategoryID() {
		return contextCategoryID;
	}

	public void setContextCategoryID(int contextCategoryID) {
		this.contextCategoryID = contextCategoryID;
	}

	public String getContextCategoryNameDesc() {
		return contextCategoryNameDesc;
	}

	public void setContextCategoryNameDesc(String contextCategoryNameDesc) {
		this.contextCategoryNameDesc = contextCategoryNameDesc;
	}

	public List<SRTObject> getContextSchemes() {
		try {
			contextSchemes = daoCS.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}

		return contextSchemes;
	}

	public void setContextSchemes(List<SRTObject> contextSchemes) {
		this.contextSchemes = contextSchemes;
	}

}
