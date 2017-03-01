package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Controller
@Scope("view")
@ManagedBean
@SessionScoped
@Transactional(readOnly = true)
public class SelectBDTBean extends AbstractCoreComponentBean {

    @Autowired
    private DataTypeRepository dataTypeRepository;
    @Autowired
    private CoreComponentService coreComponentService;

    private List<DataType> bdtList;
    private String dataTypeTermQualifier;
    private DataType selectedBDT;

    @PostConstruct
    public void init() {
        setBdtList(allBDTs());
    }

    private List<DataType> allBDTs() {
        return dataTypeRepository.findAll(new Sort(Sort.Direction.DESC, "lastUpdateTimestamp")).stream()
                .filter(e -> Published == e.getState())
                .collect(Collectors.toList());
    }

    public List<DataType> getBdtList() {
        return bdtList;
    }

    public void setBdtList(List<DataType> bdtList) {
        this.bdtList = bdtList;
    }

    public String getDataTypeTermQualifier() {
        return dataTypeTermQualifier;
    }

    public void setDataTypeTermQualifier(String dataTypeTermQualifier) {
        this.dataTypeTermQualifier = dataTypeTermQualifier;
    }

    public DataType getSelectedBDT() {
        return selectedBDT;
    }

    public void setSelectedBDT(DataType selectedBDT) {
        this.selectedBDT = selectedBDT;
    }

    public void onBDTSelect(SelectEvent event) {
        setSelectedBDT((DataType) event.getObject());
    }

    public void onBDTUnselect(UnselectEvent event) {
        setSelectedBDT(null);
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allBDTs().stream()
                    .map(e -> e.getDataTypeTerm() + " " + e.getQualifier())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allBDTs().stream()
                    .map(e -> e.getDataTypeTerm() + " " + e.getQualifier())
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
        String dataTypeTermQualifier = getDataTypeTermQualifier();
        if (StringUtils.isEmpty(dataTypeTermQualifier)) {
            setBdtList(allBDTs());
        } else {
            setBdtList(
                    allBDTs().stream()
                            .filter(e -> (e.getDataTypeTerm() + " " + e.getQualifier()).toLowerCase().contains(dataTypeTermQualifier.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    @Transactional
    public String createBCCP() {
        User requester = getCurrentUser();
        DataType bdt = getSelectedBDT();
        BasicCoreComponentProperty bccp = coreComponentService.newBasicCoreComponentProperty(requester, bdt);

        return "/views/core_component/bccp_details.xhtml?bccpId=" + bccp.getBccpId() + "&faces-redirect=true";
    }
}
