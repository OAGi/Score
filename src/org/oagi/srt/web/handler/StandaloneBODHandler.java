package org.oagi.srt.web.handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.generate.standalone.StandaloneXMLSchema;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ASCCVO;
import org.oagi.srt.persistence.dto.BBIEPVO;
import org.oagi.srt.persistence.dto.BBIEVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.web.handler.BusinessContextHandler.BusinessContextValues;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@ManagedBean
@ViewScoped
public class StandaloneBODHandler extends UIHandler implements Serializable {

	private static final long serialVersionUID = 4424008438705914095L;

	private ABIEView selectedABIEView;
	private List<ABIEView> abieViewList = new ArrayList<ABIEView>();
	private List<ABIEView> abieViewListForSelection = new ArrayList<ABIEView>();
	private ABIEView selected;
	private String abieName;
	
	public ABIEView getSelectedABIEView() {
		return selectedABIEView;
	}

	public void setSelectedABIEView(ABIEView selectedABIEView) {
		this.selectedABIEView = selectedABIEView;
	}

	public List<ABIEView> getAbieViewList() {
		return abieViewList;
	}

	public void setAbieViewList(List<ABIEView> abieViewList) {
		this.abieViewList = abieViewList;
	}

	public void chooseABIE() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("oagis_expression_select_abie", options, null);
    }
	
	public void onABIEChosen(SelectEvent event) {
		StandaloneBODHandler bh = (StandaloneBODHandler) event.getObject();
		abieViewList.add(bh.getSelected());
		
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Top-level ABIEs are added", "Top-level ABIEs are added");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
	
	public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ABIEView) event.getObject()).getName(), String.valueOf(((ABIEView) event.getObject()).getId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = (ABIEView) event.getObject();
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ABIEView) event.getObject()).getId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = null;
    }
    
    public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO abieDao = df.getDAO("ABIE");
			SRTDAO asbiepDao = df.getDAO("ASBIEP");
			SRTDAO asccpDao = df.getDAO("ASCCP");
						
			QueryCondition qc_01 = new QueryCondition();
			qc_01.add("isTop_Level", 1); 
			List<SRTObject> list_01 = abieDao.findObjects(qc_01);
			for(SRTObject abie : list_01) {
				ABIEVO abieVO = (ABIEVO) abie;
				
				QueryCondition qc_03 = new QueryCondition();
				qc_03.add("role_of_abie_id", abieVO.getABIEID());
				ASBIEPVO asbiepVO = (ASBIEPVO)asbiepDao.findObject(qc_03);
				
				QueryCondition qc_04 = new QueryCondition();
				qc_04.add("asccp_id", asbiepVO.getBasedASCCPID());
				ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc_04);
				
				if(asccpVO.getPropertyTerm().contains(query)) {
					results.add(asccpVO.getPropertyTerm());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public void search() {
		try {
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO abieDao = df.getDAO("ABIE");
			SRTDAO asbiepDao = df.getDAO("ASBIEP");
			SRTDAO asccpDao = df.getDAO("ASCCP");
						
			QueryCondition qc_01 = new QueryCondition();
			qc_01.add("isTop_Level", 1); 
			List<SRTObject> list_01 = abieDao.findObjects(qc_01);
			for(SRTObject abie : list_01) {
				ABIEVO abieVO = (ABIEVO) abie;
				
				QueryCondition qc_03 = new QueryCondition();
				qc_03.add("role_of_abie_id", abieVO.getABIEID());
				ASBIEPVO asbiepVO = (ASBIEPVO)asbiepDao.findObject(qc_03);
				
				QueryCondition qc_04 = new QueryCondition();
				qc_04.add("asccp_id", asbiepVO.getBasedASCCPID());
				ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc_04);
				
				if(asccpVO.getPropertyTerm().equals(abieName)) {
					ABIEView av = new ABIEView(asccpVO.getPropertyTerm(), abieVO.getABIEID(), "ASBIE");
					av.setAsccpVO(asccpVO);
					av.setAbieVO(abieVO);
					av.setAsbiepVO(asbiepVO);
					av.setName(asccpVO.getPropertyTerm());
					abieViewListForSelection.add(av);
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}

	public List<ABIEView> getAbieViewListForSelection() {
		return abieViewListForSelection;
	}

	public void setAbieViewListForSelection(List<ABIEView> abieViewListForSelection) {
		this.abieViewListForSelection = abieViewListForSelection;
	}

	public ABIEView getSelected() {
		return selected;
	}

	public void setSelected(ABIEView selected) {
		this.selected = selected;
	}

	public String getAbieName() {
		return abieName;
	}

	public void setAbieName(String abieName) {
		this.abieName = abieName;
	}
	
	public void generate() {
		StandaloneXMLSchema schema = new StandaloneXMLSchema();
		ArrayList<Integer> al = new ArrayList<Integer>();
		for(ABIEView av : abieViewList) {
			al.add(av.getAbieVO().getABIEID());
		}
		try {
			String filePath = schema.generateXMLSchema(al, true);
			System.out.println("### " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
