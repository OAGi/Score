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
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BusinessContextVO;
import org.oagi.srt.persistence.dto.BusinessContextValueVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;
import org.oagi.srt.persistence.dto.UserVO;
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
	private SRTDAO daoBCV;
	private SRTDAO daoBC;
	private SRTDAO daoUser;

	@PostConstruct
	private void init() {
		try {
			df = DAOFactory.getDAOFactory();
			daoCS = df.getDAO("ContextScheme");
			daoCSV = df.getDAO("ContextSchemeValue");
			daoCC = df.getDAO("ContextCategory");
			daoBCV = df.getDAO("BusinessContextValue");
			daoBC = df.getDAO("BusinessContext");
			daoUser = df.getDAO("User");
			
			QueryCondition qc = new QueryCondition();
			qc.add("name", "oagis");
			userId = ((UserVO)daoUser.findObject(qc)).getUserID();
			
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
	private int userId;
	
	private List<SRTObject> csValues = new ArrayList<SRTObject>();
	private List<SRTObject> selectedCSValues = new ArrayList<SRTObject>();
	//private List<SRTObject> contextCategories;
	private Map<String,String> contextCategories = new HashMap<String, String>();
	private List<SRTObject> contextSchemes;
	
	public List<SRTObject> getSelectedCSValues() {
		if(selectedScheme != null) {
			QueryCondition qc = new QueryCondition();
			qc.add("owner_ctx_scheme_id", ((ContextSchemeVO)selectedScheme).getContextSchemeID());
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
    	userId = cVO.getCreatedByUserId();
    	
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
		ccVO.setCreatedByUserId(userId);
		ccVO.setLastUpdatedByUserId(userId);
		
		try {
			daoCS.updateObject(ccVO);
			contextSchemes = daoCS.findObjects();
			
			QueryCondition qc = new QueryCondition();
			qc.add("owner_ctx_scheme_id", ccVO.getContextSchemeID());
			List<SRTObject> lists = daoCSV.findObjects(qc);
			
			HashMap<Integer, String> hm = new HashMap<Integer, String>();
	    	
			for(SRTObject obj : lists) {
				ContextSchemeValueVO vo = (ContextSchemeValueVO) obj;
				boolean deleted = true;
				for(SRTObject obj1 : csValues) {
					ContextSchemeValueVO vo1 = (ContextSchemeValueVO) obj1;
					if(vo.getContextSchemeValueGUID().equals(vo1.getContextSchemeValueGUID())) {
						deleted = false;
						break;
					}
				}
				if(deleted) {
					daoCSV.deleteObject(vo);
				}
			}
			
			for(SRTObject obj : csValues) {
				ContextSchemeValueVO vo = (ContextSchemeValueVO) obj;
				boolean newItem = true;
				for(SRTObject obj1 : lists) {
					ContextSchemeValueVO vo1 = (ContextSchemeValueVO) obj1;
					if(vo.getContextSchemeValueGUID().equals(vo1.getContextSchemeValueGUID())) {
						newItem = false;
						break;
					}
				}
				if(newItem) {
					vo.setOwnerContextSchemeID(ccVO.getContextSchemeID());
					daoCSV.insertObject(vo);
				}
			}
			
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		this.selectedScheme = ccVO;
    }
	
	public void deleteCSV(String guid, int id) {
		QueryCondition qc1 = new QueryCondition();
		qc1.add("ctx_scheme_value_id", id);
		String msg = "";
		try {
			List<SRTObject> list = daoBCV.findObjects(qc1);
			if(list.size() > 0) {
				msg = partResult(list);
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SRTConstants.CANNOT_DELETE_CONTEXT_SCHEME + msg,  null);
				FacesContext.getCurrentInstance().addMessage(null, message);
			} else {
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
		} catch (SRTDAOException e1) {
			e1.printStackTrace();
		}
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
			ccVO.setCreatedByUserId(userId);
			ccVO.setLastUpdatedByUserId(userId);
			daoCS.insertObject(ccVO);
			
			QueryCondition qc = new QueryCondition();
			qc.add("guid", guid);
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
	
	public void delete(int id) {
    	ContextSchemeVO ccVO = new ContextSchemeVO();
		ccVO.setContextSchemeID(id);
		setValue("");
		setMeaning("");
		try {
			QueryCondition qc = new QueryCondition();
			qc.add("owner_ctx_scheme_id", ccVO.getContextSchemeID());
			
			List<SRTObject> lists = daoCSV.findObjects(qc);
			for(SRTObject obj : lists) {
				QueryCondition qc1 = new QueryCondition();
				qc1.add("ctx_scheme_value_id", ((ContextSchemeValueVO)obj).getContextSchemeValueID());
				String msg = "";
				try {
					List<SRTObject> list = daoBCV.findObjects(qc1);
					if(list.size() > 0) {
						msg = partResult(list);
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SRTConstants.CANNOT_DELETE_CONTEXT_SCHEME + msg,  null);
						FacesContext.getCurrentInstance().addMessage(null, message);
						this.selectedScheme = null;
						return;
					}
				} catch (SRTDAOException e1) {
					e1.printStackTrace();
				}
			}
			
			for(SRTObject obj : lists) {
				daoCSV.deleteObject(((ContextSchemeValueVO)obj));
			}
			
			daoCS.deleteObject(ccVO);
			contextSchemes = daoCS.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
	
	private String partResult(List<SRTObject> list) {
    	StringBuffer sb = new StringBuffer();
    	HashMap<Integer, String> hm = new HashMap<Integer, String>();
    	for(SRTObject obj : list) {
    		BusinessContextValueVO vo = (BusinessContextValueVO)obj;
    		hm.put(vo.getBusinessContextID(), null);
    	}
    	
    	for(Integer i : hm.keySet()) {
    		QueryCondition qc = new QueryCondition();
    		qc.add("biz_ctx_id", i);
    		try {
				sb.append(((BusinessContextVO)daoBC.findObject(qc)).getName() + ", ");
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
    	}
    	String res = sb.toString();
    	return res.substring(0, res.lastIndexOf(","));
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
