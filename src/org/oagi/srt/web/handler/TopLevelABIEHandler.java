package org.oagi.srt.web.handler;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
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
import org.oagi.srt.persistence.dto.BBIE_SCVO;
import org.oagi.srt.persistence.dto.BCCPVO;
import org.oagi.srt.persistence.dto.BCCVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.BusinessContextVO;
import org.oagi.srt.persistence.dto.BusinessContextValueVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.ContextSchemeValueVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.web.handler.BusinessContextHandler.BusinessContextValues;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
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
	private SRTDAO accDao;
	private SRTDAO asccpDao;
	private SRTDAO bccpDao;
	private SRTDAO abieDao;
	private SRTDAO asbiepDao;
	private SRTDAO asbieDao;
	private SRTDAO bbiepDao;
	private SRTDAO bbieDao;
	private SRTDAO bbiescDao;
	private SRTDAO dtscDao;
	private SRTDAO dtDao;
	private SRTDAO daoBC;
	private SRTDAO daoBCV;
	private SRTDAO userDao;
	private SRTDAO bdtPrimitiveRestrictionDao;
	private SRTDAO daoCL;
	
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
	
	private int maxABIEId;
	private int maxASBIEPId;
	private int maxBIEPID;
	private int maxBIEID;
	private int maxBBIESCID;
	
	private Connection conn = null;

	@PostConstruct
	private void init() {
		try {
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("ASCCP");
			asccDao = df.getDAO("ASCC");
			bccDao = df.getDAO("BCC");
			accDao = df.getDAO("ACC");
			abieDao = df.getDAO("ABIE");
			asbiepDao = df.getDAO("ASBIEP");
			asbieDao = df.getDAO("ASBIE");
			asccpDao = df.getDAO("ASCCP");
			bccpDao = df.getDAO("BCCP");
			bbiepDao = df.getDAO("BBIEP");
			bbieDao = df.getDAO("BBIE");
			bbiescDao = df.getDAO("BBIE_SC");
			dtscDao = df.getDAO("DTSC");
			dtDao = df.getDAO("DT");
			daoBC = df.getDAO("BusinessContext");
			daoBCV = df.getDAO("BusinessContextValue");
			userDao = df.getDAO("User");
			bdtPrimitiveRestrictionDao = df.getDAO("BDTPrimitiveRestriction");
			daoCL = df.getDAO("CodeList");
			
			maxABIEId = asbieDao.findMaxId();
			maxASBIEPId = asbiepDao.findMaxId();
			maxBIEPID = bbiepDao.findMaxId();
			maxBIEID = bbieDao.findMaxId();
			maxBBIESCID = bbiescDao.findMaxId();
			
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
	
	private int barCount = 20;
	
	private int getMax() {
		int max = 0;
		if(abieCount > max)
			max = abieCount;
		if(bbiescCount > max)
			max = bbiescCount;
		if(asbiepCount > max)
			max = asbiepCount;
		if(asbieCount > max)
			max = asbieCount;
		if(bbiepCount > max)
			max = bbiepCount;
		if(bbieCount > max)
			max = bbieCount;
		return max;
	}
	
	private void createBarModel() {
		barCount = getMax();
				
        barModel = initBarModel();
         
        barModel.setTitle("Number of items created");
        barModel.setLegendPosition("ne");
         
        Axis xAxis = barModel.getAxis(AxisType.X);
        xAxis.setLabel("Tables");
         
        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("");
        yAxis.setMin(0);
        yAxis.setMax(barCount + barCount/10);
        yAxis.setTickInterval(String.valueOf(barCount/10));
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
	
	private List<ABIEView> bodList = new ArrayList<ABIEView>();
	private ABIEView selectedBod;
	
	public ABIEView getSelectedBod() {
		return selectedBod;
	}

	public void setSelectedBod(ABIEView selectedBod) {
		this.selectedBod = selectedBod;
	}

	public List<ABIEView> getBodList() {
		if(bodList.size() == 0) {
			QueryCondition qc = new QueryCondition();
			qc.add("isTop_level", 1);
			
			try {
				List<SRTObject> list = abieDao.findObjects(qc);
				
				for(SRTObject obj : list) {
					ABIEVO abieVO = (ABIEVO)obj;
					QueryCondition qc_01 = new QueryCondition();
					qc_01.add("role_of_abie_id", abieVO.getABIEID());
					ASBIEPVO asbiepVO = (ASBIEPVO)asbiepDao.findObject(qc_01);
					
					QueryCondition qc_02 = new QueryCondition();
					qc_02.add("asccp_id", asbiepVO.getBasedASCCPID());
					ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc_02);
					
					QueryCondition qc_03 = new QueryCondition();
					qc_03.add("business_context_id", abieVO.getBusinessContextID());
					BusinessContextVO aBusinessContextVO = (BusinessContextVO)daoBC.findObject(qc_03);
					abieVO.setBusinessContextName(aBusinessContextVO.getName());
					
					ABIEView aABIEView = new ABIEView(asccpVO.getPropertyTerm(), abieVO.getABIEID(), "ABIE");
					aABIEView.setAbieVO(abieVO);
					bodList.add(aABIEView);
				}
				
				//root = new DefaultTreeNode(aABIEView, null);
				
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
		}
		return bodList;
	}

	public void setBodList(List<ABIEView> bodList) {
		this.bodList = bodList;
	}

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
	
	private ABIEVO topAbieVO;
	private ASBIEPVO asbiepVO;
	private BBIEVO bieVO;
	
	public String onFlowProcess(FlowEvent event) {
		
		if(event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {
			
		} else if(event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_CREATE_UC_BIE)) {
			// TODO if go back from the confirmation page? avoid that situation
			
			DBAgent tx = new DBAgent();
			try {
				conn = tx.open();
				topAbieVO = createABIE(selected.getRoleOfACCID(), bCSelected.getBusinessContextID(), 1, 0);
				// int abieId = getABIEID("abie_guid", abieVO.getAbieGUID());
				int abieId = topAbieVO.getABIEID();
				ABIEView aABIEView = new ABIEView(selected.getPropertyTerm(), abieId, "ABIE");
				aABIEView.setAbieVO(topAbieVO);
				root = new DefaultTreeNode(aABIEView, null);
				
				asbiepVO = createASBIEP(selected.getASCCPID(), abieId, -1);
				
				createBIEs(selected.getRoleOfACCID(), abieId, -1, root);
				
				createBarModel();
				tx.commit();
			} catch (Exception e) {
				e.printStackTrace();
				tx.rollback();
			} finally {
				try {
					if(conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				tx.close();
			}
		} 
		
		return event.getNewStep();
	}
	
	public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));
    }
	
	private Integer progress;
	 
    public Integer getProgress() {
        if(progress == null) {
            progress = 0;
        }
        else {
            progress = progress + (int)(Math.random() * 35);
             
            if(progress > 100)
                progress = 100;
        }
         
        return progress;
    }
 
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
	
	private void createBIEs(int accId, int abie, int groupPosition, TreeNode tNode) throws SRTDAOException {
		Stack<ACCVO> accList = new Stack<ACCVO>();
		ACCVO acc = getACC(accId);
		accList.push(acc);
		while(acc.getBasedACCID() > 0) {
			acc = getACC(acc.getBasedACCID());
			accList.push(acc);
		}
		
		int seq_base = 1;
		while(!accList.isEmpty()) {
			ACCVO accVO = accList.pop();
			
			ArrayList<SRTObject> bccObjects = getBCC(accVO.getACCID());
			for(SRTObject bccObject : bccObjects) {
				BCCVO bccVO = (BCCVO)bccObject;
				//BCCPVO bccpVO = getBCCP(bccVO.getAssocToBCCPID());
				BBIEPVO bbiepVO = createBBIEP(bccVO.getAssocToBCCPID(), abie);
				
				String seqKey = "";
				if(groupPosition > 0) { // Group
					seqKey = groupPosition + "." + bccVO.getSequencingKey();
				} else { // not group
					seqKey = seq_base + "." + bccVO.getSequencingKey();
				}
				
				QueryCondition qc = new QueryCondition();
				qc.add("bccp_id", bccVO.getAssocToBCCPID());
				BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc, conn);
				
				QueryCondition qc_02 = new QueryCondition();
				qc_02.add("bdt_id", bccpVO.getBDTID());
				qc_02.add("isDefault", 1);
				BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)bdtPrimitiveRestrictionDao.findObject(qc_02, conn);
				int bdtPrimitiveRestrictionId = aBDTPrimitiveRestrictionVO.getBDTPrimitiveRestrictionID();
				
				BBIEVO bbieVO = createBBIE(bccVO, abie, bbiepVO.getBBIEPID(), seqKey, bdtPrimitiveRestrictionId);
				
				ABIEView av = new ABIEView(bccpVO.getPropertyTerm(), bbieVO.getBBIEID(), "BBIE");
				av.setBccVO(bccVO);
				bbiepVO.setDefinition(bccpVO.getDefinition());
				av.setBbiepVO(bbiepVO);
				av.setBbieVO(bbieVO);
				av.setBccpVO(bccpVO);
				
				QueryCondition qc_01 = new QueryCondition();
				qc_01.add("dt_id", bccpVO.getBDTID());
				DTVO dtVO = (DTVO)dtDao.findObject(qc_01, conn);
				av.setBdtName(dtVO.getDEN());
				
				av.setColor("green");
				TreeNode tNode2 = new DefaultTreeNode(av, tNode);
				
				int bbieID = bbieVO.getBBIEID();
				createBBIESC(bbieID, bccpVO.getBDTID(), tNode2);
			}
			
			ArrayList<SRTObject> asccObjects = getASCC(accVO.getACCID());
			for(SRTObject asccObject : asccObjects) {
				ASCCVO asccVO = (ASCCVO)asccObject;
				ASCCPVO asccpVO = getASCCP(asccVO.getAssocToASCCPID());
				ACCVO accVOFromASCCP = getACC(asccpVO.getRoleOfACCID());
				
				if(accVOFromASCCP.getOAGISComponentType() == 3) {
					createBIEs(accVOFromASCCP.getACCID(), abie, seq_base, tNode);
				} else {
					ABIEVO abieVO = createABIE(accVOFromASCCP.getACCID(), bCSelected.getBusinessContextID(), 0, abie);
					ASBIEPVO asbiepVO = createASBIEP(asccpVO.getASCCPID(), abieVO.getABIEID(), abie);
					
					String seqKey = "";
					if(groupPosition > 0) { // Group
						seqKey = groupPosition + "." + asccVO.getSequencingKey();
					} else { // not group
						seqKey = seq_base + "." + asccVO.getSequencingKey();
					}
					
					ASBIEVO asbieVO = createASBIE(asccVO.getASCCID(), abie, asbiepVO.getASBIEPID(), seqKey);
					
					ABIEView av = new ABIEView(asccpVO.getPropertyTerm(), asbieVO.getASBIEID(), "ASBIE");
					av.setColor("blue");
					asbieVO.setCardinalityMax(asccVO.getCardinalityMax());
					asbieVO.setCardinalityMin(asccVO.getCardinalityMin());
					asbieVO.setDefinition(asccVO.getDefinition());
					av.setAsccVO(asccVO);
					av.setAsccpVO(asccpVO);
					av.setAccVO(accVOFromASCCP);
					abieVO.setDefinition(accVO.getDefinition());
					av.setAbieVO(abieVO);
					asbiepVO.setDefinition(asccpVO.getDefinition());
					av.setAsbiepVO(asbiepVO);
					av.setAsbieVO(asbieVO);
					TreeNode tNode2 = new DefaultTreeNode(av, tNode);
					
					createBIEs(accVOFromASCCP.getACCID(), abieVO.getABIEID(), -1, tNode2);
				}
			}
			seq_base++;
		}
		

	}
	
	private ArrayList<SRTObject> getBCC(int accId) {
		QueryCondition qc = new QueryCondition();
		qc.add("assoc_from_acc_id", accId);
		ArrayList<SRTObject> res = new ArrayList<SRTObject>();
		try {
			res = bccDao.findObjects(qc, conn);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private ArrayList<SRTObject> getASCC(int accId) {
		QueryCondition qc = new QueryCondition();
		qc.add("assoc_from_acc_id", accId);
		ArrayList<SRTObject> res = new ArrayList<SRTObject>();
		try {
			res = asccDao.findObjects(qc, conn);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private ASCCPVO getASCCP(int asccpId) {
		QueryCondition qc = new QueryCondition();
		qc.add("asccp_id", asccpId);
		ASCCPVO asccpVO = null;
		try {
			asccpVO = (ASCCPVO)asccpDao.findObject(qc, conn);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return asccpVO;
	}
	
	private BCCPVO getBCCP(int bccpId) {
		QueryCondition qc = new QueryCondition();
		qc.add("bccp_id", bccpId);
		BCCPVO bccpVO = null;
		try {
			bccpVO = (BCCPVO)bccpDao.findObject(qc, conn);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return bccpVO;
	}
	
	private ACCVO getACC(int accId) {
		QueryCondition qc = new QueryCondition();
		qc.add("acc_id", accId);
		ACCVO accVO = null;
		try {
			accVO = (ACCVO)accDao.findObject(qc, conn);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return accVO;
	}
	
	private ABIEVO createABIE(int acc, int bc, int topLevel, int abie) throws SRTDAOException {
		ABIEVO abieVO = new ABIEVO();
		String abieGuid = Utility.generateGUID();
		abieVO.setAbieGUID(abieGuid);
		abieVO.setBasedACCID(acc);
		abieVO.setIsTopLevel(topLevel);
		abieVO.setBusinessContextID(bc);
		int userId = getUserId();
		abieVO.setCreatedByUserID(userId); 
		abieVO.setLastUpdatedByUserID(userId); 
		if(topLevel == 1)
			abieVO.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
		//abieVO.setABIEID(Utility.getRandomID(maxABIEId));
		try {
			int key = abieDao.insertObject(abieVO, conn);
			abieVO.setABIEID(key);
			abieCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		//abieCount++;
		return abieVO;
	}
	
	private ASBIEPVO createASBIEP(int asccp, int tAabie, int gAabie) throws SRTDAOException {
		ASBIEPVO asbiepVO = new ASBIEPVO();
		asbiepVO.setASBIEPGUID(Utility.generateGUID());
		asbiepVO.setBasedASCCPID(asccp);
		asbiepVO.setRoleOfABIEID(tAabie);
		int userId = getUserId();
		asbiepVO.setCreatedByUserID(userId); 
		asbiepVO.setLastUpdatedByUserID(userId); 
		//asbiepVO.setASBIEPID(Utility.getRandomID(maxASBIEPId));
		try {
			int key = asbiepDao.insertObject(asbiepVO, conn);
			asbiepVO.setASBIEPID(key);
			asbiepCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		//asbiepCount++;
		
		return asbiepVO;
	}
	
	private ASBIEVO createASBIE(int ascc, int abie, int asbiep, String seqKey) throws SRTDAOException {
		ASBIEVO asbieVO = new ASBIEVO();
		asbieVO.setAsbieGuid(Utility.generateGUID());
		asbieVO.setAssocFromABIEID(abie);
		asbieVO.setAssocToASBIEPID(asbiep);
		asbieVO.setBasedASCC(ascc);
		int userId = getUserId();
		asbieVO.setCreatedByUserId(userId); 
		asbieVO.setLastUpdatedByUserId(userId); 
		asbieVO.setSequencingKey(Double.parseDouble(seqKey));
		try {
			int key = asbieDao.insertObject(asbieVO, conn);
			asbieVO.setASBIEID(key);
			asbieCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		//asbieCount++;
		return asbieVO;
	}
	
	private BBIEPVO createBBIEP(int bccp, int abie) throws SRTDAOException {
		BBIEPVO bbiepVO = new BBIEPVO();
		bbiepVO.setBBIEPGUID(Utility.generateGUID());
		bbiepVO.setBasedBCCPID(bccp);
		int userId = getUserId();
		bbiepVO.setCreatedByUserID(userId); 
		bbiepVO.setLastUpdatedbyUserID(userId); 
		//bbiepVO.setBBIEPID(Utility.getRandomID(maxBIEPID));
		
		try {
			int key = bbiepDao.insertObject(bbiepVO, conn);
			bbiepVO.setBBIEPID(key);
			bbiepCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		//bbiepCount++;
		return bbiepVO;
	}
	
	private BBIEVO createBBIE(BCCVO bccVO, int abie, int bbiep, String seqKey, int bdtPrimitiveRestrictionId) throws SRTDAOException {
		BBIEVO bbieVO = new BBIEVO();
		bbieVO.setBbieGuid(Utility.generateGUID());
		bbieVO.setBasedBCCID(bccVO.getBCCID());
		bbieVO.setAssocFromABIEID(abie);
		bbieVO.setAssocToBBIEPID(bbiep);
		bbieVO.setNillable(0);
		bbieVO.setCardinalityMax(bccVO.getCardinalityMax());
		bbieVO.setCardinalityMin(bccVO.getCardinalityMin());
		bbieVO.setBdtPrimitiveRestrictionId(bdtPrimitiveRestrictionId);
		int userId = getUserId();
		bbieVO.setCreatedByUserId(userId); 
		bbieVO.setLastUpdatedByUserId(userId); 
		//bbieVO.setBBIEID(Utility.getRandomID(maxBIEID));
		bbieVO.setSequencing_key(Double.parseDouble(seqKey));
		
		try {
			int key = bbieDao.insertObject(bbieVO, conn);
			bbieVO.setBBIEID(key);
			bbieCount++;
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		//bbieCount++;
		return bbieVO;
	}
	
	private void createBBIESC(int bbie, int bdt, TreeNode tNode) {
		QueryCondition qc = new QueryCondition();
		qc.add("owner_dt_id", bdt);
		try {
			List<SRTObject> list = dtscDao.findObjects(qc, conn);
			HashMap<String, String> hm = new HashMap<String, String>();
			for(SRTObject obj : list) {
				DTSCVO dtsc = (DTSCVO) obj;
				BBIE_SCVO bbiescVO = new BBIE_SCVO();
				bbiescVO.setBBIEID(bbie);
				bbiescVO.setDTSCID(dtsc.getDTSCID()); 
				//bbiescVO.setBBIESCID(Utility.getRandomID(maxBBIESCID));
				
				int key = bbiescDao.insertObject(bbiescVO, conn);
				bbiescCount++;
				
				ABIEView av = new ABIEView(dtsc.getPropertyTerm(), key, "BBIESC");
				bbiescVO.setMaxCardinality(dtsc.getMaxCardinality());
				bbiescVO.setMinCardinality(dtsc.getMinCardinality());
				bbiescVO.setDefinition(dtsc.getDefinition());
				av.setDtscVO(dtsc);
				
				av.setBbiescVO(bbiescVO);
				av.setColor("orange");
				TreeNode tNode1 = new DefaultTreeNode(av, tNode);
				
				hm.put(dtsc.getPropertyTerm(), dtsc.getDTSCGUID());
				
			}

		} catch (SRTDAOException e1) {
			e1.printStackTrace();
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
	
	public void onBODRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ABIEView) event.getObject()).getName(), String.valueOf(((ABIEView) event.getObject()).getName()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectedBod = (ABIEView) event.getObject();
    }
	
	public void onBCSelect(BusinessContextVO bcVO) {
		bCSelected = bcVO;
		FacesMessage msg = new FacesMessage(bCSelected.getName(), bCSelected.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void onBCSelect(BusinessContextHandler bcH) {
		try {
			BusinessContextVO bcVO = new BusinessContextVO();
			bcVO.setName(bcH.getName());
			String guid = Utility.generateGUID();
			bcVO.setBusinessContextGUID(guid);
			daoBC.insertObject(bcVO);
			
			QueryCondition qc = new QueryCondition();
			qc.add("Business_Context_GUID", guid);
			BusinessContextVO bvVO1 = (BusinessContextVO)daoBC.findObject(qc);
			
			for(BusinessContextValues bcv : bcH.getBcValues()) {
				for(ContextSchemeValueVO cVO : bcv.getCsList()) {
					BusinessContextValueVO bcvVO = new BusinessContextValueVO();
					bcvVO.setBusinessContextID(bvVO1.getBusinessContextID());
					bcvVO.setContextSchemeValueID(cVO.getContextSchemeValueID());
					daoBCV.insertObject(bcvVO);
				}
			}
			
			bCSelected = bcVO;
			FacesMessage msg = new FacesMessage(bCSelected.getName(), bCSelected.getName());
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
 
    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ASCCPVO) event.getObject()).getASCCPID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onBODRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ABIEView) event.getObject()).getName()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
    private ABIEView selectedDocument;
         
    private TreeNode root;
     
    public TreeNode getRoot() {
        return root;
    }
 
    public ABIEView getSelectedDocument() {
        return selectedDocument;
    }
 
    public void setSelectedDocument(ABIEView selectedDocument) {
        this.selectedDocument = selectedDocument;
    }
    
    private int getUserId() throws SRTDAOException {
    	QueryCondition qc = new QueryCondition();
    	qc.add("user_name", "oagis");
    	return ((UserVO)userDao.findObject(qc, conn)).getUserID();
    }
    
    private int min;
    
    public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
	
	private TreeNode selectedTreeNode;
	
	private ABIEView aABIEView;
	
	public ABIEView getaABIEView() {
		if(aABIEView == null)
			aABIEView = new ABIEView();
		return aABIEView;
	}

	public void setaABIEView(ABIEView aABIEView) {
		this.aABIEView = aABIEView;
	}

	public TreeNode getSelectedTreeNode() {
		if(selectedTreeNode == null)
			selectedTreeNode = root;
		aABIEView = (ABIEView)selectedTreeNode.getData();
		return selectedTreeNode;
    }
 
    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

	public void updateTree() {
		ABIEView aABIEView = (ABIEView)selectedTreeNode.getData();
		System.out.println("### " + aABIEView.getName());
    }
	
	public void showDetails() {
		aABIEView = (ABIEView)selectedTreeNode.getData();
		codeListVO = null;
		
		if(aABIEView.getType().equalsIgnoreCase("BBIE")) {
			try {
				aABIEView.getBdtPrimitiveRestrictions();
				
				QueryCondition qc_01 = new QueryCondition();
				qc_01.add("bdt_primitive_restriction_id", aABIEView.getBdtPrimitiveRestrictionId());
				BDTPrimitiveRestrictionVO  aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)bdtPrimitiveRestrictionDao.findObject(qc_01);
				
				QueryCondition qc = new QueryCondition();
		        qc.add("based_code_list_id", aBDTPrimitiveRestrictionVO.getCodeListID());
		        codeLists = daoCL.findObjects(qc);
		        
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void saveChanges() {
		aABIEView = (ABIEView)selectedTreeNode.getData();
		if(aABIEView.type.equals("ASBIE")) {
			saveASBIEChanges(aABIEView);
		} else if(aABIEView.type.equals("BBIE")) {
			saveBBIEChanges(aABIEView);
		} else if(aABIEView.type.equals("BBIESC")) {
			saveBBIESCChanges(aABIEView);
		}
		
		FacesMessage msg = new FacesMessage("Changes on '" + aABIEView.name + "' are just saved!", "Changes on '" + aABIEView.name + "' are just saved!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	private void saveBBIEChanges(ABIEView aABIEView) {
		BBIEVO bbieVO = aABIEView.getBbieVO();
		BBIEPVO bbiepVO = aABIEView.getBbiepVO();
		try {
			
			if(aABIEView.getRestrictionType().equalsIgnoreCase("Primitive")) {
				bbieVO.setBdtPrimitiveRestrictionId(aABIEView.getBdtPrimitiveRestrictionId());
				bbieVO.setCodeListId(0);
			} else if(aABIEView.getRestrictionType().equalsIgnoreCase("Code")) {
				bbieVO.setCodeListId(codeListVO.getCodeListID());
				bbieVO.setBdtPrimitiveRestrictionId(0);
			}
			bbieDao.updateObject(bbieVO);
			bbiepDao.updateObject(bbiepVO);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public void chooseCodeForTLBIE() {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("top_level_abie_create_code_select", options, null);
    }
	
	private List<SRTObject> codeLists = new ArrayList<SRTObject>();
	
	private List<SRTObject> codeLists2 = new ArrayList<SRTObject>();
	
	public List<SRTObject> getCodeLists() {
		return codeLists;
	}

	public void setCodeLists(List<SRTObject> codeLists) {
		this.codeLists = codeLists;
	}
	
	public List<SRTObject> getCodeLists2() {
		return codeLists2;
	}

	public void setCodeLists2(List<SRTObject> codeLists2) {
		this.codeLists2 = codeLists2;
	}

	public void chooseDerivedCodeForTLBIE(int bdtPrimitiveRestrictionId) {
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        
		try {
			
			QueryCondition qc_01 = new QueryCondition();
			qc_01.add("bdt_primitive_restriction_id", bdtPrimitiveRestrictionId);
			BDTPrimitiveRestrictionVO  aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)bdtPrimitiveRestrictionDao.findObject(qc_01);
			
			QueryCondition qc = new QueryCondition();
	        qc.add("based_code_list_id", aBDTPrimitiveRestrictionVO.getCodeListID());
	        codeLists = daoCL.findObjects(qc);
	        
	        System.out.println("##### " + codeLists);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		RequestContext context = RequestContext.getCurrentInstance();
		context.openDialog("top_level_abie_create_derived_code_select", options, null);
    }
	
	CodeListVO codeListVO;
	CodeListVO selectedCodeList;
	
	public CodeListVO getSelectedCodeList() {
		return selectedCodeList;
	}

	public void setSelectedCodeList(CodeListVO selectedCodeList) {
		this.selectedCodeList = selectedCodeList;
	}

	public void onCodeListRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((CodeListVO) event.getObject()).getName(), String.valueOf(((CodeListVO) event.getObject()).getCodeListID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        codeListVO = (CodeListVO) event.getObject();
    }
 
    public void onCodeListRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((CodeListVO) event.getObject()).getCodeListID()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        codeListVO = null;
    }
	
	public CodeListVO getCodeListVO() {
		return codeListVO;
	}

	public void setCodeListVO(CodeListVO codeListVO) {
		this.codeListVO = codeListVO;
	}

	public void onCodeListChosen(SelectEvent event) {
		CodeListHandler ch = (CodeListHandler) event.getObject();
		codeListVO = (CodeListVO)ch.getSelected();
		System.out.println(codeListVO.getName());
    }
	
	public void onDerivedCodeListChosen(SelectEvent event) {
		TopLevelABIEHandler ch = (TopLevelABIEHandler) event.getObject();
		codeListVO = (CodeListVO)ch.getSelectedCodeList();
    }
	
	String codeListName;
	
	public String getCodeListName() {
		return codeListName;
	}

	public void setCodeListName(String codeListName) {
		this.codeListName = codeListName;
	}
	
	public void searchCodeList() {
		try {
			QueryCondition qc = new QueryCondition();
			qc.addLikeClause("name", "%" + getCodeListName() + "%");
			qc.add("state", SRTConstants.CODE_LIST_STATE_PUBLISHED);
			qc.add("extensible_indicator", 1);
			codeLists2 = daoCL.findObjects(qc);
			if(codeLists2.size() == 0) {
				FacesMessage msg = new FacesMessage("[" + getCodeListName() + "] No such Code List exists or not yet published or not extensible", "[" + getCodeListName() + "] No such Code List exists or not yet published or not extensible");
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> completeCodeListInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			QueryCondition qc = new QueryCondition();
			qc.addLikeClause("name", "%" + query + "%");
			codeLists2 = daoCL.findObjects(qc);
			for(SRTObject obj : codeLists2) {
				CodeListVO clVO = (CodeListVO)obj;
				results.add(clVO.getName());
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	private void saveBBIESCChanges(ABIEView aABIEView) {
		BBIE_SCVO bbiescVO = aABIEView.getBbiescVO();
		try {
			bbiescDao.updateObject(bbiescVO);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }
	
	private void saveASBIEChanges(ABIEView aABIEView) {
		ASBIEVO asbieVO = aABIEView.getAsbieVO();
		ABIEVO abieVO = aABIEView.getAbieVO();
		ASBIEPVO asbiepVO = aABIEView.getAsbiepVO();
		
		try {
			asbieDao.updateObject(asbieVO);
			asbiepDao.updateObject(asbiepVO);
			abieDao.updateObject(abieVO);
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
	}
	
    public class ABIEView implements Serializable, Comparable<ABIEView> {

    	private ASCCVO asccVO;
		private ASCCPVO asccpVO;
		private ACCVO accVO;
		private ABIEVO abieVO;
		private ASBIEPVO asbiepVO;
		private ASBIEVO asbieVO;
		private BCCVO bccVO;
		private BBIEPVO bbiepVO;
		private BBIEVO bbieVO;
		private BCCPVO bccpVO;
		private DTSCVO dtscVO;
		private BBIE_SCVO bbiescVO;
		private Map<String, Integer> bdtPrimitiveRestrictions = new HashMap<String, Integer>();
		private String bdtName;
		private BDTPrimitiveRestrictionVO bdtPrimitiveRestrictionVO;
        private String name;
        private int id;
        private String color;
        private String type;
        private String primitiveType;
        private int bdtPrimitiveRestrictionId;
        private int codeListId;
        private String restrictionType;

		public ABIEView() {
			
		}
		
		public int getCodeListId() {
			return codeListId;
		}

		public void setCodeListId(int codeListId) {
			this.codeListId = codeListId;
		}

		public String getRestrictionType() {
			return restrictionType;
		}

		public void setRestrictionType(String restrictionType) {
			this.restrictionType = restrictionType;
		}

		public void onBDTPrimitiveChange() {
			System.out.println(bdtPrimitiveRestrictionId);
		}
		
		public int getBdtPrimitiveRestrictionId() {
			return bdtPrimitiveRestrictionId;
		}

		public void setBdtPrimitiveRestrictionId(int bdtPrimitiveRestrictionId) {
			this.bdtPrimitiveRestrictionId = bdtPrimitiveRestrictionId;
		}

		public BDTPrimitiveRestrictionVO getBdtPrimitiveRestrictionVO() {
			return bdtPrimitiveRestrictionVO;
		}

		public void setBdtPrimitiveRestrictionVO(
				BDTPrimitiveRestrictionVO bdtPrimitiveRestrictionVO) {
			this.bdtPrimitiveRestrictionVO = bdtPrimitiveRestrictionVO;
		}

		public String getPrimitiveType() {
			return primitiveType;
		}

		public void setPrimitiveType(String primitiveType) {
			this.primitiveType = primitiveType;
		}

		public Map<String, Integer> getBdtPrimitiveRestrictions() {
			try {
				QueryCondition qc_02 = new QueryCondition();
				qc_02.add("bdt_id", bccpVO.getBDTID());
				List<SRTObject> ccs = bdtPrimitiveRestrictionDao.findObjects(qc_02);
				bdtPrimitiveRestrictionId = ((BDTPrimitiveRestrictionVO)ccs.get(0)).getBDTPrimitiveRestrictionID();
				for(SRTObject obj : ccs) {
					BDTPrimitiveRestrictionVO cc = (BDTPrimitiveRestrictionVO)obj;
					
					if(cc.getCDTPrimitiveExpressionTypeMapID() > 0) {
						primitiveType = "XSD Builtin Type";
						
						SRTDAO cdtAllowedPrimitiveExpressionTypeMapDao = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
						QueryCondition qc_03 = new QueryCondition();
						qc_03.add("cdt_primitive_expression_type_map_id", cc.getCDTPrimitiveExpressionTypeMapID());
						CDTAllowedPrimitiveExpressionTypeMapVO vo = (CDTAllowedPrimitiveExpressionTypeMapVO)cdtAllowedPrimitiveExpressionTypeMapDao.findObject(qc_03);
						
						SRTDAO xsdBuiltInTypeDao = df.getDAO("XSDBuiltInType");
						QueryCondition qc_04 = new QueryCondition();
						qc_04.add("xsd_builtin_type_id", vo.getXSDBuiltInTypeID());
						XSDBuiltInTypeVO xbt = (XSDBuiltInTypeVO)xsdBuiltInTypeDao.findObject(qc_04);
						bdtPrimitiveRestrictions.put(xbt.getName(), cc.getBDTPrimitiveRestrictionID());
					} else {
						primitiveType = "Code List";
						
						SRTDAO codeListDao = df.getDAO("CodeList");
						QueryCondition qc_04 = new QueryCondition();
						qc_04.add("code_list_id", cc.getCodeListID());
						CodeListVO code = (CodeListVO)codeListDao.findObject(qc_04);
						bdtPrimitiveRestrictions.put(code.getName(), cc.getBDTPrimitiveRestrictionID());
					}
				}
			} catch (SRTDAOException e) {
				e.printStackTrace();
			}
			return bdtPrimitiveRestrictions;
		}
		
		public void setBdtPrimitiveRestrictions(Map<String, Integer> bdtPrimitiveRestrictions) {
			this.bdtPrimitiveRestrictions = bdtPrimitiveRestrictions;
		}

		public String getBdtName() {
			return bdtName;
		}

		public void setBdtName(String bdtName) {
			this.bdtName = bdtName;
		}

		public ABIEView(String name, int id, String type) {
            this.name = name;
            this.id = id;
            this.type = type;
        }

		public ASBIEVO getAsbieVO() {
			return asbieVO;
		}

		public void setAsbieVO(ASBIEVO asbieVO) {
			this.asbieVO = asbieVO;
		}

		public ASCCVO getAsccVO() {
			return asccVO;
		}

		public void setAsccVO(ASCCVO asccVO) {
			this.asccVO = asccVO;
		}

		public ASCCPVO getAsccpVO() {
			return asccpVO;
		}

		public void setAsccpVO(ASCCPVO asccpVO) {
			this.asccpVO = asccpVO;
		}

		public ACCVO getAccVO() {
			return accVO;
		}

		public void setAccVO(ACCVO accVO) {
			this.accVO = accVO;
		}

		public ABIEVO getAbieVO() {
			return abieVO;
		}

		public void setAbieVO(ABIEVO abieVO) {
			this.abieVO = abieVO;
		}

		public ASBIEPVO getAsbiepVO() {
			return asbiepVO;
		}

		public void setAsbiepVO(ASBIEPVO asbiepVO) {
			this.asbiepVO = asbiepVO;
		}

		public BCCVO getBccVO() {
			return bccVO;
		}

		public void setBccVO(BCCVO bccVO) {
			this.bccVO = bccVO;
		}

		public BBIEPVO getBbiepVO() {
			return bbiepVO;
		}

		public void setBbiepVO(BBIEPVO bbiepVO) {
			this.bbiepVO = bbiepVO;
		}

		public BBIEVO getBbieVO() {
			return bbieVO;
		}

		public void setBbieVO(BBIEVO bbieVO) {
			this.bbieVO = bbieVO;
		}

		public BCCPVO getBccpVO() {
			return bccpVO;
		}

		public void setBccpVO(BCCPVO bccpVO) {
			this.bccpVO = bccpVO;
		}

		public DTSCVO getDtscVO() {
			return dtscVO;
		}

		public void setDtscVO(DTSCVO dtscVO) {
			this.dtscVO = dtscVO;
		}

		public BBIE_SCVO getBbiescVO() {
			return bbiescVO;
		}

		public void setBbiescVO(BBIE_SCVO bbiescVO) {
			this.bbiescVO = bbiescVO;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public String getName() {
            return name;
        }
     
        public void setName(String name) {
            this.name = name;
        }
     
        public String getType() {
            return type;
        }
     
        public void setType(String type) {
            this.type = type;
        }
     
		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		//Eclipse Generated hashCode and equals
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((String.valueOf(id) == null) ? 0 : String.valueOf(id).hashCode());
            return result;
        }
     
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ABIEView other = (ABIEView) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (id <= 0) {
                if (other.id > 0)
                    return false;
            } else if (id != other.id)
                return false;
            return true;
        }
     
        @Override
        public String toString() {
            return name;
        }
     
        public int compareTo(ABIEView document) {
            return this.getName().compareTo(document.getName());
        }
    } 
}