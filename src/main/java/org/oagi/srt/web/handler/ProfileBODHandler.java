package org.oagi.srt.web.handler;

import org.oagi.srt.generate.standalone.StandaloneXMLSchema;
import org.oagi.srt.repository.AggregateBusinessInformationEntityRepository;
import org.oagi.srt.repository.AssociationBusinessInformationEntityPropertyRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ProfileBODHandler extends UIHandler implements Serializable {

	private static final long serialVersionUID = 4424008438705914095L;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RepositoryFactory repositoryFactory;

	@Autowired
	private AssociationCoreComponentPropertyRepository asccpRepository;

	private AggregateBusinessInformationEntityRepository abieRepository;
	private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

	@PostConstruct
	public void init() {
		abieRepository = repositoryFactory.aggregateBusinessInformationEntityRepository();
		asbiepRepository = repositoryFactory.associationBusinessInformationEntityPropertyRepository();
	}

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
		ProfileBODHandler bh = (ProfileBODHandler) event.getObject();
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

		List<AggregateBusinessInformationEntity> list_01 = abieRepository.findByTopLevel(true);
		for (AggregateBusinessInformationEntity abieVO : list_01) {
			AssociationBusinessInformationEntityProperty asbiepVO =
					asbiepRepository.findOneByRoleOfAbieId(abieVO.getAbieId());
			AssociationCoreComponentProperty asccpVO =
					asccpRepository.findOne(asbiepVO.getBasedAsccpId());

			if (asccpVO.getPropertyTerm().contains(query)) {
				results.add(asccpVO.getPropertyTerm());
			}
		}

		return results;
	}
	
	public void search() {
		List<AggregateBusinessInformationEntity> list_01 = abieRepository.findByTopLevel(true);
		for (AggregateBusinessInformationEntity abieVO : list_01) {
			AssociationBusinessInformationEntityProperty asbiepVO =
					asbiepRepository.findOneByRoleOfAbieId(abieVO.getAbieId());
			AssociationCoreComponentProperty asccpVO =
					asccpRepository.findOne(asbiepVO.getBasedAsccpId());

			if (asccpVO.getPropertyTerm().equals(abieName)) {
				ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), abieVO.getAbieId(), "ASBIE");
				av.setAsccp(asccpVO);
				av.setAbie(abieVO);
				av.setAsbiep(asbiepVO);
				av.setName(asccpVO.getPropertyTerm());
				abieViewListForSelection.add(av);
			}
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
		for (ABIEView av : abieViewList) {
			al.add(av.getAbie().getAbieId());
		}
		try {
			filePath = schema.generateXMLSchema(al, true);
			System.out.println("### " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private StreamedContent file;
	private String filePath;
    
    public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}

	public StreamedContent getFile() {
    	InputStream stream;
		try {
			stream = new FileInputStream(new File(filePath));
			file = new DefaultStreamedContent(stream, "text/xml", filePath.substring(filePath.lastIndexOf("/") + 1));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return file;
    }
}
