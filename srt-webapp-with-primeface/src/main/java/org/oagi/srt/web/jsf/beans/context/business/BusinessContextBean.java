package org.oagi.srt.web.jsf.beans.context.business;

import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.service.BusinessContextService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
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
public class BusinessContextBean extends UIHandler {

    @Autowired
    private BusinessContextService businessContextService;

    private List<BusinessContext> allBusinessContexts;
    private List<BusinessContext> businessContexts;
    private String name;

    @PostConstruct
    public void init() {
        setBusinessContexts(allBusinessContexts());
    }

    public List<BusinessContext> allBusinessContexts() {
        allBusinessContexts = businessContextService.findAll(Sort.Direction.DESC, "bizCtxId");
        return allBusinessContexts;
    }

    public List<BusinessContext> getBusinessContexts() {
        return businessContexts;
    }

    public void setBusinessContexts(List<BusinessContext> businessContexts) {
        this.businessContexts = businessContexts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> completeInput(String query) {
        return allBusinessContexts().stream()
                .map(e -> e.getName())
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        if (StringUtils.isEmpty(name)) {
            setBusinessContexts(allBusinessContexts());
        } else {
            setBusinessContexts(
                    allBusinessContexts().stream()
                            .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}