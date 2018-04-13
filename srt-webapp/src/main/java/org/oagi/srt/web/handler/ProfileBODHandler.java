package org.oagi.srt.web.handler;

import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.AggregateBusinessInformationEntityRepository;
import org.oagi.srt.repository.AssociationBusinessInformationEntityPropertyRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.TopLevelAbie;
import org.oagi.srt.service.ProfileBODGenerateService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

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
import java.util.stream.Collectors;

import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaExpression.XML;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ProfileBODHandler extends UIHandler implements Serializable {

	private static final long serialVersionUID = 4424008438705914095L;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AssociationCoreComponentPropertyRepository asccpRepository;

	@Autowired
	private AggregateBusinessInformationEntityRepository abieRepository;

	@Autowired
	private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

	@Autowired
	private TopLevelAbieRepository topLevelAbieRepository;

	@Autowired
	private ProfileBODGenerateService profileBODGenerateService;

	private ABIEView selectedABIEView;
	private List<ABIEView> abieViewList = new ArrayList<ABIEView>();
	private List<ABIEView> abieViewListForSelection = new ArrayList<ABIEView>();
	private ABIEView selected;
	private String abieName;
	private ProfileBODGenerationOption generationOption = new ProfileBODGenerationOption();

	@PostConstruct
	public void init() {
		generationOption.setSchemaExpression(XML);
	}
	
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
		String q = (query != null) ? query.trim() : null;
		List<AssociationCoreComponentProperty> asccpList = asccpRepository.findAll();

		if (StringUtils.isEmpty(q)) {
			return asccpList.stream()
					.map(e -> e.getPropertyTerm())
					.collect(Collectors.toList());
		} else {
			String[] split = q.split(" ");

			return asccpList.stream()
					.map(e -> e.getPropertyTerm())
					.distinct()
					.filter(e -> {
						e = e.toLowerCase();
						for (String s : split) {
							if (!e.contains(s.toLowerCase())) {
								return false;
							}
						}
						return true;
					})
					.collect(Collectors.toList());
		}
	}
	
	public void search() {
		List<TopLevelAbie> topLevelAbieList = topLevelAbieRepository.findAll();
		for (TopLevelAbie topLevelAbie : topLevelAbieList) {
			AggregateBusinessInformationEntity abieVO = topLevelAbie.getAbie();
			if (abieVO == null) {
				continue;
			}
			AssociationBusinessInformationEntityProperty asbiepVO =
					asbiepRepository.findOneByRoleOfAbieId(abieVO.getAbieId());
			if (asbiepVO == null) {
				continue;
			}
			AssociationCoreComponentProperty asccpVO =
					asccpRepository.findOne(asbiepVO.getBasedAsccpId());
			if (asccpVO == null) {
				continue;
			}

			if (asccpVO.getPropertyTerm().equals(abieName)) {
				ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), abieVO.getAbieId(), "ASBIE");
				av.setAsccp(asccpVO);
				av.setTopLevelAbie(topLevelAbie);
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

	public void generate() throws Exception {
		List<Long> al = new ArrayList();
		for (ABIEView av : abieViewList) {
			al.add(av.getTopLevelAbie().getTopLevelAbieId());
		}

		generateSchemaFile = profileBODGenerateService.generateXMLSchema(al, generationOption);
		System.out.println("### " + generateSchemaFile);
	}
	
	private StreamedContent file;
	private File generateSchemaFile;

	public void setFile(StreamedContent file) {
		this.file = file;
	}

	public StreamedContent getFile() throws IOException {
		InputStream stream = new FileInputStream(generateSchemaFile);
		String filePath = generateSchemaFile.getCanonicalPath();
		file = new DefaultStreamedContent(stream, "text/xml", filePath.substring(filePath.lastIndexOf("/") + 1));
		return file;
	}
}
