package org.oagi.srt.web.handler;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.oagi.srt.persistence.dto.ContextCategoryVO;
 
@ManagedBean
public class ContextCategoryHandler {
     
    private String name;
    private String description;
    
    private List<ContextCategoryVO> contextCategories;
 
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
    	System.out.println("FSFSFSFsfsfsfs");
        this.name = name;
    }
 
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

	public List<ContextCategoryVO> getContextCategories() {
		
		// TODO dummy data for demo
		contextCategories = new ArrayList<ContextCategoryVO>();
		int k;
		for(int i = 1; i < 20; i++) {
			ContextCategoryVO cvo = new ContextCategoryVO();
			if(i > 9) {
				k = i - 9;
			} else {
				k = i;
			}
			cvo.setContextCategoryGUID("oagis-id-" + k + "827340198ab273829fc12983462abcd");
			cvo.setName("Category ex " + i);
			cvo.setDescription("This is a dummy category for test.");
			contextCategories.add(cvo);
		}
		return contextCategories;
	}

	public void setContextCategories(List<ContextCategoryVO> contextCategories) {
		this.contextCategories = contextCategories;
	}
	
	public void detail(ActionEvent actionEvent) {
        addMessage("Data Details");
    }
	
	public void edit(ActionEvent actionEvent) {
        addMessage("Data Edited");
    }
     
    public void delete(ActionEvent actionEvent) {
        addMessage("Data deleted");
    }
     
    public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public List<String> completeInput(String query) {
        List<String> results = new ArrayList<String>();
         
        if(query.contains("Cat")) {
        	for(int i = 0; i < 20; i++) {
        		results.add("Category ex " + i);
        	}
        } 
         
        return results;
    }
    
    public List<String> completeDescription(String query) {
        List<String> results = new ArrayList<String>();
         
        if(query.contains("This")) {
        	for(int i = 0; i < 20; i++) {
        		results.add("This is a dummy category for test.");
        	}
        } 
         
        return results;
    }
}