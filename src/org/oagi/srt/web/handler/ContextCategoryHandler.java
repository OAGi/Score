package org.oagi.srt.web.handler;

import java.util.ArrayList;
import java.util.List;

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
import org.primefaces.event.RowEditEvent;

@ManagedBean
public class ContextCategoryHandler {

	private DAOFactory df;
	private SRTDAO dao;

	@PostConstruct
	private void init() {
		try {	
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("ContextCategory");
			contextCategories = dao.findObjects();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String name;
	private String description;
	private String GUID;
	private int id;

	private List<SRTObject> contextCategories;
	private SRTObject selectedCategory;
	
	private ContextCategoryVO contextCategory;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getGUID() {
		return GUID;
	}

	public void setGUID(String GUID) {
		this.GUID = GUID;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<SRTObject> getContextCategories() {
		return contextCategories;
	}

	public void setContextCategories(List<SRTObject> contextCategories) {
		this.contextCategories = contextCategories;
	}
	
	public void edit(ActionEvent actionEvent) {
		
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

	public ContextCategoryVO getContextCategory() {
		return contextCategory;
	}

	public void setContextCategory(ContextCategoryVO contextCategory) {
		this.contextCategory = contextCategory;
	}
	
    public SRTObject getselectedCategory() {
        return selectedCategory;
    }
 
    public void setselectedCategory(SRTObject selectedCategory) {
        this.selectedCategory = selectedCategory;
    }
    
    public void onEdit(SRTObject obj) {
    	ContextCategoryVO cVO = (ContextCategoryVO) obj;
    	this.id = cVO.getContextCategoryID();
    	this.GUID = cVO.getContextCategoryGUID();
    	this.name = cVO.getName();
    	this.description = cVO.getDescription();
    }
    
    public void save() {
    	ContextCategoryVO ccVO = new ContextCategoryVO();
		ccVO.setName(this.name);
		ccVO.setDescription(this.description);
		ccVO.setContextCategoryGUID(this.GUID);
		ccVO.setContextCategoryID(this.id);
		
		try {
			dao.updateObject(ccVO);
			contextCategories = dao.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		this.selectedCategory = ccVO;
    }
    
    public void delete(int id) {
    	ContextCategoryVO ccVO = new ContextCategoryVO();
		ccVO.setContextCategoryID(id);
		
		System.out.println("!!!" + id);
		
		try {
			dao.deleteObject(ccVO);
			contextCategories = dao.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
	
    public void onRowEdit(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Context Category Edited  "+ ((ContextCategoryVO) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);

        ContextCategoryVO ccVO = new ContextCategoryVO();
		ccVO.setName(((ContextCategoryVO) event.getObject()).getName());
		ccVO.setDescription(((ContextCategoryVO) event.getObject()).getDescription());
		ccVO.setContextCategoryGUID(((ContextCategoryVO) event.getObject()).getContextCategoryGUID());
		ccVO.setContextCategoryID(((ContextCategoryVO) event.getObject()).getContextCategoryID());
		try {
			dao.updateObject(ccVO);
		} catch (SRTDAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
     
    public void onRowCancel(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Edit Cancelled", ((SRTObject) event.getObject()).getObid());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}