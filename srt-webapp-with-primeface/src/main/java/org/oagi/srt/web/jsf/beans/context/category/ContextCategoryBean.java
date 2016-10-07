package org.oagi.srt.web.jsf.beans.context.category;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextCategoryBean extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    private List<ContextCategory> allContextCategories;
    private List<ContextCategory> contextCategories;
    private String name;

    @PostConstruct
    public void init() {
        allContextCategories = contextCategoryService.findAll(Sort.Direction.DESC, "ctxCategoryId");
        setContextCategories(allContextCategories);
    }

    public List<ContextCategory> getContextCategories() {
        return contextCategories;
    }

    public void setContextCategories(List<ContextCategory> contextCategories) {
        this.contextCategories = contextCategories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> completeInput(String query) {
        return allContextCategories.stream()
                .map(e -> e.getName())
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        setContextCategories(
                allContextCategories.stream()
                        .filter(e -> e.getName().toLowerCase().contains(name))
                        .collect(Collectors.toList())
        );
    }
}