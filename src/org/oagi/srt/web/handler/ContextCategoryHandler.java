package org.oagi.srt.web.handler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ContextCategoryVO;

@ManagedBean
public class ContextCategoryHandler {

	private DAOFactory df;
	private SRTDAO dao;

	@PostConstruct
	private void init() {
		try {
			//Utility.dbSetup(); // TODO use Context Initializer
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("ContextCategory");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String name;
	private String description;

	private List<SRTObject> contextCategories;

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

	public List<SRTObject> getContextCategories() {

		try {
			contextCategories = dao.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}

		return contextCategories;
	}

	public void setContextCategories(List<SRTObject> contextCategories) {
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

	public void createContextCategory() {
		try {
			ContextCategoryVO ccVO = new ContextCategoryVO();
			ccVO.setName(this.name);
			ccVO.setDescription(this.description);
			ccVO.setContextCategoryGUID(Utility.generateGUID());
			dao.insertObject(ccVO);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			contextCategories = dao.findObjects();
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

	public List<String> completeDescription(String query) {
		List<String> results = new ArrayList<String>();

		try {
			contextCategories = dao.findObjects();
			for(SRTObject obj : contextCategories) {
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
}