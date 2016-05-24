package org.oagi.srt.web.handler;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextSchemeHandler {

	@Autowired
	private RepositoryFactory repositoryFactory;

	@Autowired
	private UserRepository userRepository;

	private ContextSchemeRepository contextSchemeRepository;
	private ContextSchemeValueRepository contextSchemeValueRepository;
	private BusinessContextRepository businessContextRepository;
	private BusinessContextValueRepository businessContextValueRepository;
	private ContextCategoryRepository contextCategoryRepository;

	@PostConstruct
	private void init() {
		userId = userRepository.findOneByLoginId("oagis").getAppUserId();

		contextSchemeRepository = repositoryFactory.contextSchemeRepository();
		contextSchemeValueRepository = repositoryFactory.contextSchemeValueRepository();
		businessContextRepository = repositoryFactory.businessContextRepository();
		businessContextValueRepository = repositoryFactory.businessContextValueRepository();
		contextCategoryRepository = repositoryFactory.contextCategoryRepository();
	}

	private String value;
	private String meaning;
	private String name;
	private String description;
	private String schemeAgencyID;
	private String schemeVersion;
	private int contextCategoryID;
	private int id;
	private String contextCategoryNameDesc; 
	private ContextScheme selectedScheme;
	private String schemeName;
	private String guid;
	private String schemeId;
	private int userId;

	private List<ContextSchemeValue> csValues = new ArrayList();
	private List<ContextSchemeValue> selectedCSValues = Collections.emptyList();

	private Map<String, String> contextCategories = new HashMap<String, String>();
	private List<ContextScheme> contextSchemes;

	public List<ContextSchemeValue> getSelectedCSValues() {
		if (selectedScheme != null) {
			selectedCSValues = contextSchemeValueRepository.findByContextSchemeId(
					selectedScheme.getClassificationCtxSchemeId());
		}
		return selectedCSValues;
	}

	public void setSelectedCSValues(List<ContextSchemeValue> selectedCSValues) {
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
	
	public ContextScheme getSelectedScheme() {
		return selectedScheme;
	}

	public void setSelectedScheme(ContextScheme selectedScheme) {
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
	
	public void setCsValues(List<ContextSchemeValue> csValues) {
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
	
	public void onEdit(ContextScheme contextScheme) {
    	description = contextScheme.getDescription();
    	schemeAgencyID = contextScheme.getSchemeAgencyId();
    	schemeVersion = contextScheme.getSchemeVersionId();
    	contextCategoryID = contextScheme.getCtxCategoryId();
    	id = contextScheme.getClassificationCtxSchemeId();
    	schemeName = contextScheme.getSchemeName();
    	guid = contextScheme.getGuid();
    	schemeId = contextScheme.getSchemeId();
    	userId = contextScheme.getCreatedBy();
    	
		csValues = selectedCSValues;
    }

	@Transactional(rollbackFor = Throwable.class)
	public void save() {
		ContextScheme contextScheme = new ContextScheme();
		contextScheme.setDescription(description);
		contextScheme.setSchemeAgencyId(schemeAgencyID);
		contextScheme.setSchemeVersionId(schemeVersion);
		contextScheme.setCtxCategoryId(contextCategoryID);
		contextScheme.setClassificationCtxSchemeId(id);
		contextScheme.setSchemeName(schemeName);
		contextScheme.setGuid(guid);
		contextScheme.setSchemeId(schemeId);
		contextScheme.setCreatedBy(userId);
		contextScheme.setLastUpdatedBy(userId);

		contextSchemeRepository.update(contextScheme);
		contextSchemes = contextSchemeRepository.findAll();

		List<ContextSchemeValue> contextSchemeValues =
				contextSchemeValueRepository.findByContextSchemeId(contextScheme.getClassificationCtxSchemeId());
		for (ContextSchemeValue source : contextSchemeValues) {
			boolean deleted = true;
			for (ContextSchemeValue target : csValues) {
				if (source.getGuid().equals(target.getGuid())) {
					deleted = false;
					break;
				}
			}
			if (deleted) {
				contextSchemeValueRepository.deleteByContextSchemeId(source.getCtxSchemeValueId());
			}
		}

		for (ContextSchemeValue source : csValues) {
			boolean newItem = true;
			for (ContextSchemeValue target : contextSchemeValues) {
				if (source.getGuid().equals(target.getGuid())) {
					newItem = false;
					break;
				}
			}
			if (newItem) {
				source.setOwnerCtxSchemeId(contextScheme.getClassificationCtxSchemeId());
				contextSchemeValueRepository.save(source);
			}
		}

		this.selectedScheme = contextScheme;
    }

	@Transactional(rollbackFor = Throwable.class)
	public void deleteCSV(String guid, int id) {
		List<BusinessContextValue> businessContextValues = businessContextValueRepository.findByContextSchemeValueId(id);
		if (!businessContextValues.isEmpty()) {
			String msg = partResult(businessContextValues);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SRTConstants.CANNOT_DELETE_CONTEXT_SCHEME + msg, null);
			FacesContext.getCurrentInstance().addMessage(null, message);
		} else {
			this.csValues =
					csValues.stream().filter(contextSchemeValue -> !contextSchemeValue.getGuid().equals(guid))
							.collect(Collectors.toList());
			setValue("");
			setMeaning("");
		}
	}

	public Map<String, String> getContextCategories() {
		for (ContextCategory contextCategory : contextCategoryRepository.findAll()) {
			contextCategories.put(contextCategory.getName(), String.valueOf(contextCategory.getCtxCategoryId()));
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

	@Transactional(rollbackFor = Throwable.class)
	public void createContextScheme() {
		ContextScheme contextScheme = new ContextScheme();
		String guid = Utility.generateGUID();
		contextScheme.setSchemeName(name);
		contextScheme.setDescription(description);
		contextScheme.setGuid(guid);
		contextScheme.setSchemeId(Utility.generateGUID());
		contextScheme.setSchemeAgencyId(schemeAgencyID);
		contextScheme.setSchemeVersionId(schemeVersion);
		contextScheme.setCtxCategoryId(Integer.valueOf(contextCategoryNameDesc));
		contextScheme.setCreatedBy(userId);
		contextScheme.setLastUpdatedBy(userId);
		contextSchemeRepository.save(contextScheme);

		for (ContextSchemeValue contextSchemeValue : csValues) {
			contextSchemeValue.setOwnerCtxSchemeId(contextScheme.getClassificationCtxSchemeId());
			contextSchemeValueRepository.save(contextSchemeValue);
		}
	}

	public List<String> completeInput(String query) {
		contextSchemes = contextSchemeRepository.findAll();
		return contextSchemes.stream().filter(contextScheme -> contextScheme.getSchemeName().contains(query))
				.map(contextScheme -> contextScheme.getSchemeName()).collect(Collectors.toList());
	}
	
	public void addSchemeValue() {
		ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
		contextSchemeValue.setValue(getValue());
		contextSchemeValue.setMeaning(getMeaning());
		contextSchemeValue.setGuid(Utility.generateGUID());
		setValue("");
		setMeaning("");
		setCsValues(contextSchemeValue);
	}

	@Transactional(rollbackFor = Throwable.class)
	public void delete(int contextSchemeId) {
    	ContextScheme contextScheme = new ContextScheme();
		contextScheme.setClassificationCtxSchemeId(contextSchemeId);
		setValue("");
		setMeaning("");

		List<ContextSchemeValue> contextSchemeValues =
				contextSchemeValueRepository.findByContextSchemeId(contextSchemeId);
		for (ContextSchemeValue contextSchemeValue : contextSchemeValues) {
			List<BusinessContextValue> businessContextValues =
					businessContextValueRepository.findByContextSchemeValueId(contextSchemeValue.getCtxSchemeValueId());
			if (!businessContextValues.isEmpty()) {
				String msg = partResult(businessContextValues);
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, SRTConstants.CANNOT_DELETE_CONTEXT_SCHEME + msg,  null);
				FacesContext.getCurrentInstance().addMessage(null, message);
				this.selectedScheme = null;
				return;
			}
		}

		for (ContextSchemeValue contextSchemeValue : contextSchemeValues) {
			contextSchemeValueRepository.deleteByContextSchemeValueId(contextSchemeValue.getCtxSchemeValueId());
		}
		contextSchemeRepository.deleteByContextSchemeId(contextScheme.getClassificationCtxSchemeId());
		contextSchemes = contextSchemeRepository.findAll();
    }

	private String partResult(List<BusinessContextValue> businessContextValues) {
		StringBuffer sb = new StringBuffer();
		HashMap<Integer, String> hm = new HashMap<Integer, String>();
		for (BusinessContextValue businessContextValue : businessContextValues) {
			hm.put(businessContextValue.getBizCtxId(), null);
		}

		for (int businessContextId : hm.keySet()) {
			BusinessContext businessContext =
					businessContextRepository.findOneByBusinessContextId(businessContextId);
			sb.append(businessContext.getName() + ", ");
		}

		String res = sb.toString();
		return res.substring(0, res.lastIndexOf(","));
	}
	
	public void setCsValues(ContextSchemeValue contextSchemeValue) {
		csValues.add(contextSchemeValue);
	}

	public List<ContextSchemeValue> getCsValues() {
		return csValues;
	}

	public List<String> completeDescription(String query) {
		contextSchemes = contextSchemeRepository.findAll();
		return contextSchemes.stream()
				.filter(contextScheme -> contextScheme.getDescription().contains(query))
				.map(contextScheme -> contextScheme.getDescription()).collect(Collectors.toList());
	}

	public String getSchemeAgencyID() {
		return schemeAgencyID;
	}

	public void setSchemeAgencyID(String schemeAgencyID) {
		this.schemeAgencyID = schemeAgencyID;
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

	public List<ContextScheme> getContextSchemes() {
		contextSchemes = contextSchemeRepository.findAll();
		return contextSchemes;
	}

	public void setContextSchemes(List<ContextScheme> contextSchemes) {
		this.contextSchemes = contextSchemes;
	}

}
