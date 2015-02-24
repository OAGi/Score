package org.oagi.srt.web.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
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

@ManagedBean
public class ContextSchemeHandler {

	private DAOFactory df;
	private SRTDAO daoCS;
	private SRTDAO daoCC;

	@PostConstruct
	private void init() {
		try {
			df = DAOFactory.getDAOFactory();
			daoCS = df.getDAO("ContextScheme");
			daoCC = df.getDAO("ContextCategory");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String name;
	private String description;
	private String schemeAgencyID;
	private String schemeAgencyName;
	private String schemeVersion;
	private int contextCategoryID;
	private String contextCategoryNameDesc; 
	//private List<SRTObject> contextCategories;
	private Map<String,String> contextCategories = new HashMap<String, String>();
	private List<SRTObject> contextSchemes;
	
	public void onContextCategoryChange() {
		System.out.println("######### " + contextCategoryNameDesc);
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

	public void detail(ActionEvent actionEvent) {
		addMessage("Coming soon!!!");
	}

	public void edit(ActionEvent actionEvent) {
		addMessage("Coming soon!!!");
	}

	public void delete(ActionEvent actionEvent) {
		addMessage("Coming soon!!!");
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
			ccVO.setSchemeName(name);
			ccVO.setDescription(description);
			ccVO.setSchemeGUID(Utility.generateGUID());
			ccVO.setSchemeID(Utility.generateGUID());
			ccVO.setSchemeAgencyID(schemeAgencyID);
			ccVO.setSchemeAgencyName(schemeAgencyName);
			ccVO.setSchemeVersion(schemeVersion);
			ccVO.setContextCategoryID(Integer.valueOf(contextCategoryNameDesc));
			daoCS.insertObject(ccVO);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			contextSchemes = daoCS.findObjects();
			for(SRTObject obj : contextSchemes) {
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
