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
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ABIEVO;
import org.oagi.srt.persistence.dto.ASBIEPVO;
import org.oagi.srt.persistence.dto.ASBIEVO;
import org.oagi.srt.persistence.dto.ASCCPVO;
import org.oagi.srt.persistence.dto.ASCCVO;
import org.oagi.srt.persistence.dto.BBIEPVO;
import org.oagi.srt.persistence.dto.BBIEVO;
import org.oagi.srt.persistence.dto.BBIE_SCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.BusinessContextVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

@ManagedBean
@ViewScoped
public class TopLevelABIEHandler implements Serializable {

	private static final long serialVersionUID = -2650693005373031742L;
	
	private DAOFactory df;
	private SRTDAO dao;
	private SRTDAO asccDao;
	private SRTDAO bccDao;
	private SRTDAO asccpDao;
	private SRTDAO bccpDao;
	private SRTDAO abieDao;
	private SRTDAO asbiepDao;
	private SRTDAO asbieDao;
	private SRTDAO bbiepDao;
	private SRTDAO bbieDao;
	private SRTDAO bbiescDao;
	
	private int abieCount = 0;
	private int bbiescCount = 0;
	private int asbiepCount = 0;
	private int asbieCount = 0;
	private int bbiepCount = 0;
	private int bbieCount = 0;
	
	private BarChartModel barModel;
	
	private String propertyTerm;
	private List<SRTObject> asccpVOs;
	
	private ASCCPVO selected;
	private BusinessContextVO bCSelected;

	@PostConstruct
	private void init() {
		try {
			//Utility.dbSetup(); // TODO use Context Initializer
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("ASCCP");
			asccDao = df.getDAO("ASCC");
			bccDao = df.getDAO("BCC");
			abieDao = df.getDAO("ABIE");
			asbiepDao = df.getDAO("ASBIEP");
			asbieDao = df.getDAO("ASBIE");
			asccpDao = df.getDAO("ASCCP");
			bccpDao = df.getDAO("BCCP");
			bbiepDao = df.getDAO("BBIEP");
			bbieDao = df.getDAO("BBIE");
			bbiescDao = df.getDAO("BBIE_SC");
			
			try {
				asccpVOs = dao.findObjects();
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BarChartModel getBarModel() {
        return barModel;
    }
	
	private void createBarModel() {
        barModel = initBarModel();
         
        barModel.setTitle("Number of items created");
        barModel.setLegendPosition("ne");
         
        Axis xAxis = barModel.getAxis(AxisType.X);
        xAxis.setLabel("Tables");
         
        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("");
        yAxis.setMin(0);
        yAxis.setMax(30);
        yAxis.setTickInterval("5");
    }
	
	
	private BarChartModel initBarModel() {
        BarChartModel model = new BarChartModel();
 
        ChartSeries tabie = new ChartSeries();
        tabie.setLabel("ABIE");
        tabie.set("", abieCount);
        
        ChartSeries tasbiep = new ChartSeries();
        tasbiep.setLabel("ASBIEP");
        tasbiep.set("", asbiepCount);
        
        ChartSeries tasbie = new ChartSeries();
        tasbie.setLabel("ASBIE");
        tasbie.set("", asbieCount);
        
        ChartSeries tbbie = new ChartSeries();
        tbbie.setLabel("BBIE");
        tbbie.set("", bbieCount);
        
        ChartSeries tbbiep = new ChartSeries();
        tbbiep.setLabel("BBIEP");
        tbbiep.set("", bbiepCount);

        ChartSeries tbbiesc = new ChartSeries();
        tbbiesc.setLabel("BBIE_SC");
        tbbiesc.set("", bbiescCount);
 
        model.addSeries(tabie);
        model.addSeries(tasbiep);
        model.addSeries(tasbie);
        
        model.addSeries(tbbie);
        model.addSeries(tbbiep);
        model.addSeries(tbbiesc);
         
        return model;
    }
	
	public void itemSelect(ItemSelectEvent event) {
		String str = "";
		switch (event.getSeriesIndex()) {
			case 0:
				str = "ABIE: " +  abieCount;
				break;
			case 1:
				str = "ASBIEP: " +  asbiepCount;
				break;
			case 2:
				str = "ASBIE: " +  asbieCount;
				break;
			case 3:
				str = "BBIE: " +  bbieCount;
				break;
			case 4:
				str = "BBIEP: " +  bbiepCount;
				break;
			case 5:
				str = "BBIE_SC: " +  bbiescCount;
				break;
			default:
				str = "";
				break;
		}
			
        FacesMessage msg = new FacesMessage(str,
                        "Item Index: " + event.getItemIndex() + ", Series Index:" + event.getSeriesIndex());
         
        FacesContext.getCurrentInstance().addMessage(null, msg);
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
		
		if(event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {
			
		} else if(event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_CREATE_UC_BIE)) {
			// TODO if go back from the confirmation page? avoid that situation
			
			ABIEVO abieVO = createABIE(selected.getRoleOfACCID(), bCSelected.getBusinessContextID(), 1);
			int abieId = getABIEID("abie_guid", abieVO.getAbieGUID());
			
			createASBIEP(abieId, selected.getASCCPID());
			
			createBIEs(selected.getRoleOfACCID(), abieId);
			
			createBarModel();
		} 
		
		return event.getNewStep();
	}
	
	private void createBIEs(int acc, int abie) {
		createAggregateDescendantBIEs(acc, abie);
		createBasicChildBIEs(acc, abie);
	}
	
	private int getABIEID(String key, String value) {
		QueryCondition qc = new QueryCondition();
		qc.add(key, value);
		int id = -1;
		try {
			id = ((ABIEVO)abieDao.findObject(qc)).getABIEID();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return id;
	}

	private int getASBIEPID(String key, String value) {
		QueryCondition qc = new QueryCondition();
		qc.add(key, value);
		int id = -1;
		try {
			id = ((ASBIEPVO)asbiepDao.findObject(qc)).getASBIEPID();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	private int getBBIEPID(String key, String value) {
		QueryCondition qc = new QueryCondition();
		qc.add(key, value);
		int id = -1;
		try {
			id = ((BBIEPVO)bbiepDao.findObject(qc)).getBBIEPID();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	private ABIEVO createABIE(int acc, int bc, int topLevel) {
		ABIEVO abieVO = new ABIEVO();
		String abieGuid = Utility.generateGUID();
		abieVO.setAbieGUID(abieGuid);
		abieVO.setBasedACCID(acc);
		abieVO.setIsTopLevel(topLevel);
		abieVO.setBusinessContextID(bc);
		abieVO.setCreatedByUserID(8); // TODO get from UserID
		abieVO.setLastUpdatedByUserID(8); // TODO get from UserID
		abieVO.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
		try {
			abieDao.insertObject(abieVO);
			abieCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return abieVO;
	}
	
	private ASBIEPVO createASBIEP(int abie, int asccp) {
		ASBIEPVO asbiepVO = new ASBIEPVO();
		asbiepVO.setASBIEPGUID(Utility.generateGUID());
		asbiepVO.setBasedASCCPID(asccp);
		asbiepVO.setRoleOfABIEID(abie);
		asbiepVO.setCreatedByUserID(8); // TODO get from UserID
		asbiepVO.setLastUpdatedByUserID(8); // TODO get from UserID
		try {
			asbiepDao.insertObject(asbiepVO);
			asbiepCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		return asbiepVO;
	}
	
	private ASBIEVO createASBIE(int ascc, int abie, int asbiep) {
		ASBIEVO asbieVO = new ASBIEVO();
		asbieVO.setAsbieGuid(Utility.generateGUID());
		asbieVO.setAssocFromABIEID(abie);
		asbieVO.setAssocToASBIEPID(asbiep);
		asbieVO.setBasedASCC(ascc);
		asbieVO.setCreatedByUserId(8); // TODO get from UserID
		asbieVO.setLastUpdatedByUserId(8); // TODO get from UserID
		try {
			asbieDao.insertObject(asbieVO);
			asbieCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		return asbieVO;
	}
	
	private void createAggregateDescendantBIEs(int gACC, int gABIE) {
		QueryCondition qc = new QueryCondition();
		qc.add("assoc_from_acc_id", gACC);
		try {
			for(SRTObject obj : asccDao.findObjects(qc)) {
				ASCCVO asccVO = (ASCCVO) obj;
				qc = new QueryCondition();
				qc.add("asccp_id", asccVO.getAssocToASCCPID());
				
				ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc);
				ABIEVO abieVO = createABIE(asccpVO.getRoleOfACCID(), bCSelected.getBusinessContextID(), 0);
				int abieId = getABIEID("abie_guid", abieVO.getAbieGUID());
				ASBIEPVO asbiepVO = createASBIEP(abieId, asccpVO.getASCCPID());
				int asbiepId = getASBIEPID("asbiep_guid", asbiepVO.getASBIEPGUID());
				
				ASBIEVO asbieVO = createASBIE(asccVO.getASCCID(), gABIE, asbiepId);
				
				createBIEs(asccpVO.getRoleOfACCID(), abieId);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
	}
	
	private BBIEPVO createBBIEP(int bccp, int abie) {
		BBIEPVO bbiepVO = new BBIEPVO();
		bbiepVO.setBBIEPGUID(Utility.generateGUID());
		bbiepVO.setBasedBCCPID(bccp);
		bbiepVO.setCreatedByUserID(8); // TODO get from UserID
		bbiepVO.setLastUpdatedbyUserID(8); // TODO get from UserID
		
		try {
			bbiepDao.insertObject(bbiepVO);
			bbiepCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return bbiepVO;
	}
	
	private BBIEVO createBBIE(int bcc, int abie, int bbiep) {
		BBIEVO bbieVO = new BBIEVO();
		bbieVO.setBbieGuid(Utility.generateGUID());
		bbieVO.setBasedBCCID(bcc);
		bbieVO.setAssocFromABIEID(abie);
		bbieVO.setAssocToBBIEPID(bbiep);
		bbieVO.setisNillable(0);
		bbieVO.setCreatedByUserId(8); // TODO get from UserID
		bbieVO.setLastUpdatedByUserId(8); // TODO get from UserID
		try {
			bbieDao.insertObject(bbieVO);
			bbieCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return bbieVO;
	}
	
	private BBIE_SCVO createBBIESC(int bbie) {
		BBIE_SCVO bbiescVO = new BBIE_SCVO();
		bbiescVO.setBBIEID(bbie);
		//bbiescVO.setDTSCID(dTSCID); // TODO ask to Serm whether get DT_SC directly from BCCP.BDT_ID or get DT first and get Based_DT_ID and then get DT_SC
		
		try {
			bbiescDao.insertObject(bbiescVO);
			bbiescCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return bbiescVO;
	}
	
	private void createBasicChildBIEs(int gACC, int gABIE) {
		QueryCondition qc = new QueryCondition();
		qc.add("assoc_from_acc_id", gACC);
		try {
			List<SRTObject> list = bccDao.findObjects(qc);
			
			if(list != null) {
				for(SRTObject obj : list) {
					BCCVO bccVO = (BCCVO) obj;
					qc = new QueryCondition();
					qc.add("bccp_id", bccVO.getAssocToBCCPID());
					BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc);
					
					BBIEPVO bbiepVP = createBBIEP(bccpVO.getBCCPID(), gABIE);
					
					
					
					int bbiepID = getBBIEPID("bbiep_guid", bbiepVP.getBBIEPGUID());
					
					System.out.println("################ bccVO.getBCCID(): " + bccVO.getBCCID() + " | " + gABIE + " | " + bbiepID);
					BBIEVO bbieVO = createBBIE(bccVO.getBCCID(), gABIE, bbiepID);
					
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
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
    
	public BusinessContextVO getbCSelected() {
		return bCSelected;
	}

	public void setbCSelected(BusinessContextVO bCSelected) {
		this.bCSelected = bCSelected;
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
	
	public void onBCSelect(BusinessContextVO bcVO) {
		bCSelected = bcVO;
		FacesMessage msg = new FacesMessage(bCSelected.getName(), bCSelected.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ASCCPVO) event.getObject()).getASCCPID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
}