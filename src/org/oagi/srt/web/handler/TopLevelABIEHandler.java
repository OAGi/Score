package org.oagi.srt.web.handler;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.oagi.srt.persistence.dto.BusinessContextVO;
import org.oagi.srt.persistence.dto.ContextCategoryVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
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
			//Utility.dbSetup(); // TODO use Context Initializer
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
			
			DBAgent tx = new DBAgent();
			try {
				conn = tx.open();
				ABIEVO abieVO = createABIE(selected.getRoleOfACCID(), bCSelected.getBusinessContextID(), 1);
				// int abieId = getABIEID("abie_guid", abieVO.getAbieGUID());
				int abieId = abieVO.getABIEID();
				root = new DefaultTreeNode(new ABIEView(selected.getPropertyTerm(), abieVO.getAbieGUID()), null);
				createASBIEP(abieId, selected.getASCCPID());
				
				createBIEs(selected.getRoleOfACCID(), abieId, root);
				
				createBarModel();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(conn != null)
						conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		
		return event.getNewStep();
	}
	
	private void createBIEs(int acc, int abie, TreeNode tNode) {
		QueryCondition qc = new QueryCondition();
		qc.add("acc_id", acc);
		ACCVO accVO = null;
		try {
			accVO = (ACCVO)accDao.findObject(qc, conn);
			if(accVO.getBasedACCID() > 0) {
				createASBIEP(abie, accVO.getBasedACCID());
				createBIEs(accVO.getBasedACCID(), abie, tNode);
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		
		createBasicChildBIEs(acc, abie, tNode);
		createAggregateDescendantBIEs(acc, abie, tNode);
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
	
	private int getBBIEID(String key, String value) {
		QueryCondition qc = new QueryCondition();
		qc.add(key, value);
		int id = -1;
		try {
			id = ((BBIEVO)bbieDao.findObject(qc)).getBBIEID();
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
		abieVO.setABIEID(Utility.getRandomID(maxABIEId));
//		try {
//			abieDao.insertObject(abieVO);
//			abieCount++;
//		} catch (SRTDAOException e) {
//			e.printStackTrace();
//		}
		
		abieCount++;
		return abieVO;
	}
	
	private ASBIEPVO createASBIEP(int abie, int asccp) {
		ASBIEPVO asbiepVO = new ASBIEPVO();
		asbiepVO.setASBIEPGUID(Utility.generateGUID());
		asbiepVO.setBasedASCCPID(asccp);
		asbiepVO.setRoleOfABIEID(abie);
		asbiepVO.setCreatedByUserID(8); // TODO get from UserID
		asbiepVO.setLastUpdatedByUserID(8); // TODO get from UserID
		asbiepVO.setASBIEPID(Utility.getRandomID(maxASBIEPId));
//		try {
//			asbiepDao.insertObject(asbiepVO);
//			asbiepCount++;
//		} catch (SRTDAOException e) {
//			e.printStackTrace();
//		}
		
		asbiepCount++;
		
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
//		try {
//			asbieDao.insertObject(asbieVO);
//			asbieCount++;
//		} catch (SRTDAOException e) {
//			e.printStackTrace();
//		}
		asbieCount++;
		return asbieVO;
	}
	
	private void createAggregateDescendantBIEs(int gACC, int gABIE, TreeNode tNode) {
		QueryCondition qc = new QueryCondition();
		qc.add("assoc_from_acc_id", gACC);
		try {
			List<SRTObject> objs = asccDao.findObjects(qc, conn);
			for(SRTObject obj : objs) {
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				ASCCVO asccVO = (ASCCVO) obj;
				//if(asccVO.getDefinition() == null || !asccVO.getDefinition().equalsIgnoreCase("Group")) {
					qc = new QueryCondition();
					qc.add("asccp_id", asccVO.getAssocToASCCPID());
					
					ASCCPVO asccpVO = (ASCCPVO)asccpDao.findObject(qc, conn);
					ABIEVO abieVO = createABIE(asccpVO.getRoleOfACCID(), bCSelected.getBusinessContextID(), 0);
					//int abieId = getABIEID("abie_guid", abieVO.getAbieGUID());
					int abieId = abieVO.getABIEID();
					
					ASBIEPVO asbiepVO = createASBIEP(abieId, asccpVO.getASCCPID());
					//int asbiepId = getASBIEPID("asbiep_guid", asbiepVO.getASBIEPGUID());
					int asbiepId = asbiepVO.getASBIEPID();
					
					ASBIEVO asbieVO = createASBIE(asccVO.getASCCID(), gABIE, asbiepId);
					ABIEView av = new ABIEView(asccpVO.getPropertyTerm(), abieVO.getAbieGUID());
					av.setColor("blue");
					av.setMin(asbieVO.getCardinalityMin());
					av.setMax(asbieVO.getCardinalityMax());
					TreeNode tNode2 = new DefaultTreeNode(av, tNode);
					
					if(!asccpVO.getPropertyTerm().contains("Group")) { //.equalsIgnoreCase("References Group") && !asccpVO.getPropertyTerm().equalsIgnoreCase("Free Form Text Group")) // TODO check why freeformtext repeated
						createBIEs(asccpVO.getRoleOfACCID(), abieId, tNode2);
					} else {
						QueryCondition qc1 = new QueryCondition();
						qc1.add("acc_guid", asccpVO.getASCCPGUID());
						ACCVO accVO = (ACCVO)accDao.findObject(qc1, conn);
						createBasicChildBIEs(accVO.getACCID(), abieId, tNode2);
					}
				//} else {
					
				//}
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
		bbiepVO.setBBIEPID(Utility.getRandomID(maxBIEPID));
		
//		try {
//			bbiepDao.insertObject(bbiepVO);
//			bbiepCount++;
//		} catch (SRTDAOException e) {
//			e.printStackTrace();
//		}
		
		bbiepCount++;
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
		bbieVO.setBBIEID(Utility.getRandomID(maxBIEID));
		
//		try {
//			bbieDao.insertObject(bbieVO);
//			bbieCount++;
//		} catch (SRTDAOException e) {
//			e.printStackTrace();
//		}
		
		bbieCount++;
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
				bbiescVO.setBBIESCID(Utility.getRandomID(maxBBIESCID));
				
				//bbiescDao.insertObject(bbiescVO);
				bbiescCount++;
				hm.put(dtsc.getPropertyTerm(), dtsc.getDTSCGUID());
				
			}
			
			for(String key : hm.keySet()) {
				ABIEView av = new ABIEView(key, hm.get(key));
				av.setColor("orange");
				//av.setMin(bbiescVO.getMinCardinality()); // TODO this is temporary treatment to avoid duplicate list. should check dt_sc table to eliminate the duplicates
				//av.setMax(bbiescVO.getMaxCardinality());
				TreeNode tNode1 = new DefaultTreeNode(av, tNode);
			}
		} catch (SRTDAOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	private void createBasicChildBIEs(int gACC, int gABIE, TreeNode tNode) {
		QueryCondition qc = new QueryCondition();
		qc.add("assoc_from_acc_id", gACC);
		try {
			List<SRTObject> list = bccDao.findObjects(qc, conn);
			
			if(list != null) {
				for(SRTObject obj : list) {
					BCCVO bccVO = (BCCVO) obj;
					qc = new QueryCondition();
					qc.add("bccp_id", bccVO.getAssocToBCCPID());
					BCCPVO bccpVO = (BCCPVO)bccpDao.findObject(qc, conn);
					
					BBIEPVO bbiepVO = createBBIEP(bccpVO.getBCCPID(), gABIE);
					
					//int bbiepID = getBBIEPID("bbiep_guid", bbiepVO.getBBIEPGUID());
					int bbiepID = bbiepVO.getBBIEPID();
					
					BBIEVO bbieVO = createBBIE(bccVO.getBCCID(), gABIE, bbiepID);
					
					ABIEView av = new ABIEView(bccpVO.getPropertyTerm(), bbieVO.getBbieGuid());
					av.setColor("green");
					av.setMin(bbieVO.getCardinalityMin());
					av.setMax(bbieVO.getCardinalityMax());
					TreeNode tNode2 = new DefaultTreeNode(av, tNode);
					
					//int bbieID = getBBIEID("bbie_guid", bbieVO.getBbieGuid());
					int bbieID = bbieVO.getBBIEID();
					createBBIESC(bbieID, bccpVO.getBDTID(), tNode2);
					
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
    
    public class ABIEView implements Serializable, Comparable<ABIEView> {
    	 
        private String name;
        private String guid;
        private int min;
        private int max;
        private String primitive;
        private String fixedValue;
        private String color;
        private String type;
         
        public ABIEView(String name, String guid) {
            this.name = name;
            this.guid = guid;
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
     
        public String getGuid() {
			return guid;
		}

		public void setGuid(String guid) {
			this.guid = guid;
		}

		public int getMin() {
			return min;
		}

		public void setMin(int min) {
			this.min = min;
		}

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}

		public String getPrimitive() {
			return primitive;
		}

		public void setPrimitive(String primitive) {
			this.primitive = primitive;
		}

		public String getFixedValue() {
			return fixedValue;
		}

		public void setFixedValue(String fixedValue) {
			this.fixedValue = fixedValue;
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
            result = prime * result + ((guid == null) ? 0 : guid.hashCode());
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
            if (guid == null) {
                if (other.guid != null)
                    return false;
            } else if (!guid.equals(other.guid))
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