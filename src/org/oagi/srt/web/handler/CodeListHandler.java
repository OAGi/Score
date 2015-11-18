package org.oagi.srt.web.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.CodeListValueVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@ManagedBean
@ViewScoped
public class CodeListHandler extends UIHandler {

	private List<SRTObject> codeLists = new ArrayList<SRTObject>();
	private List<SRTObject> codeListsForList = new ArrayList<SRTObject>();
	private List<SRTObject> codeListValues = new ArrayList<SRTObject>();
	private List<SRTObject> newCodeListsWOBase = new ArrayList<SRTObject>();
	private CodeListVO codeListVO = new CodeListVO();
	private CodeListValueVO codeListValueVO = new CodeListValueVO();
	private boolean extensible; 
	private String basedCodeListName;
	private CodeListVO selected;
	private List<CodeListValueVO> selectedCodeListValue;
	
	public CodeListValueVO getCodeListValueVO() {
		return codeListValueVO;
	}

	public void setCodeListValueVO(CodeListValueVO codeListValueVO) {
		this.codeListValueVO = codeListValueVO;
	}

	public List<SRTObject> getCodeListsForList() {
		try {
			codeListsForList = daoCL.findObjects();
			for(SRTObject obj : codeListsForList) {
				CodeListVO cVO = (CodeListVO)obj;
				if(cVO.getState().equals(SRTConstants.CODE_LIST_STATE_EDITING)) {
					cVO.setEditDisabled(false);
					cVO.setDiscardDisabled(false);
					cVO.setDeleteDisabled(true);
				} else if(cVO.getState().equals(SRTConstants.CODE_LIST_STATE_PUBLISHED)) {
					cVO.setEditDisabled(true);
					cVO.setDiscardDisabled(true);
					cVO.setDeleteDisabled(false);
				} else {
					cVO.setEditDisabled(true);
					cVO.setDiscardDisabled(true);
					cVO.setDeleteDisabled(true);
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return codeListsForList;
	}

	public void setCodeListsForList(List<SRTObject> codeListsForList) {
		this.codeListsForList = codeListsForList;
	}

	public List<CodeListValueVO> getSelectedCodeListValue() {
		return selectedCodeListValue;
	}

	public void setSelectedCodeListValue(List<CodeListValueVO> selectedCodeListValue) {
		this.selectedCodeListValue = selectedCodeListValue;
	}

	public List<SRTObject> getCodeListValues() {
		return codeListValues;
	}

	public void setCodeListValues(List<SRTObject> codeListValues) {
		this.codeListValues = codeListValues;
	}

	public List<SRTObject> getCodeLists() {
		return codeLists;
	}

	public void setCodeLists(List<SRTObject> codeLists) {
		this.codeLists = codeLists;
	}

	public CodeListVO getCodeListVO() {
		return codeListVO;
	}

	public void setCodeListVO(CodeListVO codeListVO) {
		this.codeListVO = codeListVO;
	}

	public boolean isExtensible() {
		return extensible;
	}

	public void setExtensible(boolean extensible) {
		this.extensible = extensible;
	}

	public List<SRTObject> getNewCodeListsWOBase() {
		return newCodeListsWOBase;
	}

	public void setNewCodeListsWOBase(List<SRTObject> newCodeListsWOBase) {
		this.newCodeListsWOBase = newCodeListsWOBase;
	}
	
	public String getBasedCodeListName() {
		return basedCodeListName;
	}

	public void setBasedCodeListName(String basedCodeListName) {
		this.basedCodeListName = basedCodeListName;
	}

	public void chooseBasedCode() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("code_list_create_select", options, null);
    }
	
	public void addCodeListValue() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 300);
        RequestContext.getCurrentInstance().openDialog("code_list_add_code_list_value", options, null);
    }
	
	public void addNewCodeListValue() {
		codeListValueVO.setExtensionIndicator(true);
		codeListValueVO.setUsedIndicator(true);
		codeListValueVO.setDisabled(false);
		codeListValueVO.setColor("green");
		closeDialog();
	}
	
	public void onCodeListValueAdded(SelectEvent event) {
		CodeListHandler ch = (CodeListHandler) event.getObject();
		
		codeListValues.add(ch.getCodeListValueVO());
		
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Code Values are loaded", "Code Values are loaded");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
	
	public void onBasedCodeChosen(SelectEvent event) {
		CodeListHandler ch = (CodeListHandler) event.getObject();
		if(ch.getSelected() != null) {
			QueryCondition qc = new QueryCondition();
			qc.add("code_list_id", ((CodeListVO)ch.getSelected()).getCodeListID());
			selected = (CodeListVO)ch.getSelected();
			try {
				codeListValues = daoCLV.findObjects(qc);
				for(SRTObject vo : codeListValues) {
					CodeListValueVO codelistvalueVO = (CodeListValueVO)vo;
					if(codelistvalueVO.getUsedIndicator() && !codelistvalueVO.getLockedIndicator())
						codelistvalueVO.setColor("blue");
					else if((!codelistvalueVO.getUsedIndicator() && !codelistvalueVO.getLockedIndicator()) || (codelistvalueVO.getLockedIndicator()))
						codelistvalueVO.setColor("red");
				}
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
		}
		
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Code Values are loaded", "Code Values are loaded");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
	
	public void onEdit(SRTObject obj) {
		codeListVO = (CodeListVO)obj;
		QueryCondition qc = new QueryCondition();
		qc.add("code_list_id", codeListVO.getCodeListID());
		try {
			codeListValues = daoCLV.findObjects(qc);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
	
	public void onDiscard(SRTObject obj) {
		codeListVO = (CodeListVO)obj;
		codeListVO.setState(SRTConstants.CODE_LIST_STATE_DISCARDED);
		try {
			daoCL.updateObject(codeListVO);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
	
	public void onDelete(SRTObject obj) {
		codeListVO = (CodeListVO)obj;
		codeListVO.setState(SRTConstants.CODE_LIST_STATE_DELETED);
		try {
			daoCL.updateObject(codeListVO);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
	
	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			QueryCondition qc = new QueryCondition();
			qc.addLikeClause("name", "%" + query + "%");
			codeLists = daoCL.findObjects(qc);
			for(SRTObject obj : codeLists) {
				CodeListVO clVO = (CodeListVO)obj;
				results.add(clVO.getName());
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public void search() {
		try {
			QueryCondition qc = new QueryCondition();
			qc.addLikeClause("name", "%" + getBasedCodeListName() + "%");
			qc.add("state", SRTConstants.CODE_LIST_STATE_PUBLISHED);
			qc.add("extensible_indicator", 1);
			codeLists = daoCL.findObjects(qc);
			if(codeLists.size() == 0) {
				FacesMessage msg = new FacesMessage("[" + getBasedCodeListName() + "] No such Code List exists or not yet published or not extensible", "[" + getBasedCodeListName() + "] No such Code List exists or not yet published or not extensible");
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public void searchDerived(String id) {
		try {
			QueryCondition qc = new QueryCondition();
			qc.add("code_list_id", id);
			codeLists = daoCL.findObjects(qc);
			if(codeLists.size() == 0) {
				FacesMessage msg = new FacesMessage("[" + getBasedCodeListName() + "] No such Code List exists.", "[" + getBasedCodeListName() + "] No such Code List exists.");
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((CodeListVO) event.getObject()).getName(), String.valueOf(((CodeListVO) event.getObject()).getCodeListID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = (CodeListVO) event.getObject();
        QueryCondition qc = new QueryCondition();
		qc.add("code_list_id", selected.getCodeListID());
		try {
			codeListValues = daoCLV.findObjects(qc);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((CodeListVO) event.getObject()).getCodeListID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = null;
    }
    
    public void save() {
    	try {
    		codeListVO.setExtensibleIndicator(extensible);
    		codeListVO.setCodeListGUID(Utility.generateGUID());
    		codeListVO.setEnumerationTypeGUID(Utility.generateGUID());
    		if(selected != null)
    			codeListVO.setBasedCodeListID(selected.getCodeListID());
    		codeListVO.setState(SRTConstants.CODE_LIST_STATE_EDITING);
    		codeListVO.setCreatedByUserID(userId);
    		codeListVO.setLastUpdatedByUserID(userId);
			daoCL.insertObject(codeListVO);
			
			QueryCondition qc = new QueryCondition();
			qc.add("code_list_guid", codeListVO.getCodeListGUID());
			qc.add("enumeration_type_guid", codeListVO.getEnumerationTypeGUID());
			qc.add("name", codeListVO.getName());
			qc.add("definition", codeListVO.getDefinition());
			int clId = ((CodeListVO)daoCL.findObject(qc)).getCodeListID();
			for(SRTObject vo : codeListValues) {
				CodeListValueVO cVO = (CodeListValueVO)vo;
				cVO.setOwnerCodeListID(clId);
				setIndicators(cVO);
				daoCLV.insertObject(cVO);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
    
    private void setIndicators(CodeListValueVO cVO) {
    	if(cVO.getColor().equalsIgnoreCase("blue")) {
			cVO.setUsedIndicator(true);
			cVO.setLockedIndicator(false);
			cVO.setExtensionIndicator(false);
		} else if(cVO.getColor().equalsIgnoreCase("red")) {
			cVO.setUsedIndicator(false);
			cVO.setLockedIndicator(true);
			cVO.setExtensionIndicator(false);
		} else if(cVO.getColor().equalsIgnoreCase("orange")) {
			cVO.setUsedIndicator(false);
			cVO.setLockedIndicator(false);
			cVO.setExtensionIndicator(false);
		} else if(cVO.getColor().equalsIgnoreCase("green")) {
			cVO.setUsedIndicator(true);
			cVO.setLockedIndicator(false);
			cVO.setExtensionIndicator(true);
		}
    }
    
    public void updateSave() {
    	try {
			daoCL.updateObject(codeListVO);
			
			for(SRTObject vo : codeListValues) {
				CodeListValueVO cVO = (CodeListValueVO)vo;
				setIndicators(cVO);
				cVO.setOwnerCodeListID(codeListVO.getCodeListID());
				daoCLV.deleteObject(cVO);
				daoCLV.insertObject(cVO);
			}
			selected = codeListVO;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
    
    public void updatePublish() {
    	try {
    		codeListVO.setState(SRTConstants.CODE_LIST_STATE_PUBLISHED);
			daoCL.updateObject(codeListVO);
			
			for(SRTObject vo : codeListValues) {
				CodeListValueVO cVO = (CodeListValueVO)vo;
				setIndicators(cVO);
				cVO.setOwnerCodeListID(codeListVO.getCodeListID());
				daoCLV.deleteObject(cVO);
				daoCLV.insertObject(cVO);
			}
			selected = codeListVO;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
    
    public void publish() {
    	try {
    		codeListVO.setExtensibleIndicator(extensible);
    		codeListVO.setCodeListGUID(Utility.generateGUID());
    		codeListVO.setEnumerationTypeGUID(Utility.generateGUID());
    		codeListVO.setBasedCodeListID(selected.getCodeListID());
    		codeListVO.setState(SRTConstants.CODE_LIST_STATE_PUBLISHED);
    		codeListVO.setCreatedByUserID(userId);
    		codeListVO.setLastUpdatedByUserID(userId);
			daoCL.insertObject(codeListVO);
			
			QueryCondition qc = new QueryCondition();
			qc.add("code_list_guid", codeListVO.getCodeListGUID());
			qc.add("enumeration_type_guid", codeListVO.getEnumerationTypeGUID());
			qc.add("based_code_list_id", codeListVO.getBasedCodeListID());
			qc.add("name", codeListVO.getName());
			qc.add("definition", codeListVO.getDefinition());
			int clId = ((CodeListVO)daoCL.findObject(qc)).getCodeListID();
			for(SRTObject vo : codeListValues) {
				CodeListValueVO cVO = (CodeListValueVO)vo;
				cVO.setOwnerCodeListID(clId);
				setIndicators(cVO);
				daoCLV.insertObject(cVO);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
    }
    
    public void cancel() {
    	this.selected = null;
    }

	public CodeListVO getSelected() {
		return selected;
	}

	public void setSelected(CodeListVO selected) {
		this.selected = selected;
	}
	
	public void updateCodeListValue(int codeListValueId) {
		for(SRTObject vo : codeListValues) {
			CodeListValueVO cVO = (CodeListValueVO)vo;
			if(cVO.getCodeListValueID() == codeListValueId) {
				cVO.setColor((cVO.getColor().equals("blue")) ? "orange" : "blue"); 
			} 
		}
	}
    
}
