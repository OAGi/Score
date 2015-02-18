package org.oagi.srt.web.handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@ManagedBean
@ViewScoped
public class TopLevelABIEHandler implements Serializable {

	private static final long serialVersionUID = -2650693005373031742L;
	
	private DAOFactory df;
	private SRTDAO dao;
	
	private String propertyTerm;
	private List<SRTObject> asccpVOs;
	
	private ASCCPVO selected;

	@PostConstruct
	private void init() {
		try {
			//Utility.dbSetup(); // TODO use Context Initializer
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("ASCCP");
			
			try {
				asccpVOs = dao.findObjects();
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean skip;

	public void save() {        
		FacesMessage msg = new FacesMessage("Successful", "Welcome :" + "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public void search() {
		try {
			QueryCondition qc = new QueryCondition();
			qc.addLikeClause("Property_Term", "%" + getPropertyTerm() + "%");
			asccpVOs = dao.findObjects(qc);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}

		//return asccpVOs;
	}
	
	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public String onFlowProcess(FlowEvent event) {
		if(skip) {
			skip = false;   //reset in case user goes back
			return "confirm";
		}
		else {
			return event.getNewStep();
		}
	}
	
	public List<SRTObject> getAsccpVOs() {
		return asccpVOs;
	}
	
	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			asccpVOs = dao.findObjects();
			for(SRTObject obj : asccpVOs) {
				ASCCPVO ccVO = (ASCCPVO)obj;
				if(ccVO.getPropertyTerm().contains(query)) {
					results.add(ccVO.getPropertyTerm());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public ASCCPVO getSelected() {
        return selected;
    }
 
    public void setSelected(ASCCPVO selected) {
        this.selected = selected;
    }

	public String getPropertyTerm() {
		return propertyTerm;
	}

	public void setPropertyTerm(String propertyTerm) {
		this.propertyTerm = propertyTerm;
	}
	
	public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ASCCPVO) event.getObject()).getPropertyTerm(), String.valueOf(((ASCCPVO) event.getObject()).getASCCPID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = (ASCCPVO) event.getObject();
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ASCCPVO) event.getObject()).getASCCPID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
}