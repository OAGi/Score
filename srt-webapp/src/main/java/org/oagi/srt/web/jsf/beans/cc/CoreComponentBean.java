package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CoreComponentBean {

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    private List<AssociationCoreComponentProperty> allAsccpList;
    private List<AssociationCoreComponentProperty> asccpList;

    private String selectedPropertyTerm;

    @PostConstruct
    public void init() {
        allAsccpList = asccpRepository.findAll();
        setAsccpList(
                allAsccpList.stream()
                        .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                        .collect(Collectors.toList())
        );
    }

    public List<AssociationCoreComponentProperty> getAsccpList() {
        return asccpList;
    }

    public void setAsccpList(List<AssociationCoreComponentProperty> asccpList) {
        this.asccpList = asccpList;
    }

    public String getSelectedPropertyTerm() {
        return selectedPropertyTerm;
    }

    public void setSelectedPropertyTerm(String selectedPropertyTerm) {
        this.selectedPropertyTerm = selectedPropertyTerm;
    }

    public List<String> completeInput(String query) {
        return allAsccpList.stream()
                .map(e -> e.getPropertyTerm())
                .distinct()
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setAsccpList(allAsccpList.stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setAsccpList(
                    allAsccpList.stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}
