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
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@ManagedBean
@ViewScoped
public class CodeListHandler extends UIHandler {

	private List<SRTObject> codeLists = new ArrayList<SRTObject>();
	private List<SRTObject> codeListValues = new ArrayList<SRTObject>();
	private List<SRTObject> newCodeListsWOBase = new ArrayList<SRTObject>();
	private CodeListVO codeListVO = new CodeListVO();
	private boolean extensible; 
	private String basedCodeListName;
	protected CodeListVO selected;

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
	
	public void onBasedCodeChosen(SelectEvent event) {
		CodeListHandler ch = (CodeListHandler) event.getObject();
		if(ch.getSelected() != null) {
			QueryCondition qc = new QueryCondition();
			qc.add("code_list_id", ch.getSelected().getCodeListID());
			try {
				codeListValues = daoCLV.findObjects(qc);
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
		}
		
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Code Values are loaded", "Code Values are loaded");
        FacesContext.getCurrentInstance().addMessage(null, message);
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
			codeLists = daoCL.findObjects(qc);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((CodeListVO) event.getObject()).getName(), String.valueOf(((CodeListVO) event.getObject()).getCodeListID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        selected = (CodeListVO) event.getObject();
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((CodeListVO) event.getObject()).getCodeListID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = null;
    }

	public CodeListVO getSelected() {
		return selected;
	}

	public void setSelected(CodeListVO selected) {
		this.selected = selected;
	}
    
}
