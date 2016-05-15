package org.oagi.srt.web.handler;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.ContextCategoryRepository;
import org.oagi.srt.repository.ContextSchemeRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.entity.ContextScheme;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextCategoryHandler extends UIHandler {

	@Autowired
	private RepositoryFactory repositoryFactory;
	private ContextCategoryRepository contextCategoryRepository;
	private ContextSchemeRepository contextSchemeRepository;

	private String name;
	private String description;
	private String GUID;
	private int id;

	private List<ContextCategory> contextCategories;
	private ContextCategory selectedCategory;
	
	private ContextCategory contextCategory;

	@PostConstruct
	public void init() {
		contextCategoryRepository = repositoryFactory.contextCategoryRepository();
		contextSchemeRepository = repositoryFactory.contextSchemeRepository();
	}

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

	public List<ContextCategory> getContextCategories() {
		contextCategories = contextCategoryRepository.findAll();
		return contextCategories;
	}

	public void refreshContextCategories() {
		getContextCategories();
	}

	public void setContextCategories(List<ContextCategory> contextCategories) {
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

	@Transactional(rollbackFor = Throwable.class)
	public void createContextCategory() {
		ContextCategory contextCategory = new ContextCategory();
		contextCategory.setName(this.name);
		contextCategory.setDescription(this.description);
		contextCategory.setGuid(Utility.generateGUID());
		contextCategoryRepository.save(contextCategory);
	}
	
	public List<String> completeInput(String query) {
		return getContextCategories().stream()
				.filter(contextCategory -> contextCategory.getName().contains(query))
				.map(contextCategory -> contextCategory.getName()).collect(Collectors.toList());
	}

	public List<String> completeDescription(String query) {
		return getContextCategories().stream()
				.filter(contextCategory -> contextCategory.getDescription().contains(query))
				.map(contextCategory -> contextCategory.getDescription()).collect(Collectors.toList());
	}

	public ContextCategory getContextCategory() {
		return contextCategory;
	}

	public void setContextCategory(ContextCategory contextCategory) {
		this.contextCategory = contextCategory;
	}

	public ContextCategory getSelectedCategory() {
		return selectedCategory;
	}

	public void setSelectedCategory(ContextCategory selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	public void onEdit(ContextCategory contextCategory) {
    	this.id = contextCategory.getCtxCategoryId();
    	this.GUID = contextCategory.getGuid();
    	this.name = contextCategory.getName();
    	this.description = contextCategory.getDescription();
    }

	@Transactional(rollbackFor = Throwable.class)
    public void save() {
    	ContextCategory contextCategory = new ContextCategory();
		contextCategory.setName(this.name);
		contextCategory.setDescription(this.description);
		contextCategory.setGuid(this.GUID);
		contextCategory.setCtxCategoryId(this.id);

		contextCategoryRepository.update(contextCategory);
		refreshContextCategories();
		setSelectedCategory(contextCategory);
    }
    
    public void cancel() {
    	this.selectedCategory = null;
    }

	@Transactional(rollbackFor = Throwable.class)
    public void delete(int id) {
		try {
			contextCategoryRepository.deleteByContextCategoryId(id);
			refreshContextCategories();
		} catch (DataAccessException e) {
			if (e.getLocalizedMessage().contains(SRTConstants.FOREIGNKEY_ERROR_MSG)) {
				List<ContextScheme> contextSchemes = contextSchemeRepository.findByContextCategoryId(id);
				String msg = partResult(contextSchemes);

				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SRTConstants.CANNOT_DELETE_CONTEXT_CATEGORTY + msg, null);
				FacesContext.getCurrentInstance().addMessage(null, message);
				this.selectedCategory = null;
			} else {
				e.printStackTrace();
			}
		}
    }
    
    private String partResult(List<ContextScheme> list) {
    	StringBuffer sb = new StringBuffer();
    	for(ContextScheme contextScheme : list) {
    		sb.append(contextScheme.getSchemeName() + ", ");
    	}
    	String res = sb.toString();
    	return res.substring(0, res.lastIndexOf(","));
    }

	@Transactional(rollbackFor = Throwable.class)
    public void onRowEdit(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Context Category Edited  "+ ((ContextCategory) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);

        ContextCategory contextCategory = new ContextCategory();
		contextCategory.setName(((ContextCategory) event.getObject()).getName());
		contextCategory.setDescription(((ContextCategory) event.getObject()).getDescription());
		contextCategory.setGuid(((ContextCategory) event.getObject()).getGuid());
		contextCategory.setCtxCategoryId(((ContextCategory) event.getObject()).getCtxCategoryId());
		contextCategoryRepository.update(contextCategory);
    }
     
    public void onRowCancel(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Edit Cancelled", ((ContextCategory) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}