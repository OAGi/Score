package org.oagi.srt.web.jsf.beans.context.category;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Controller
@Scope(SCOPE_SESSION)
@ManagedBean
@SessionScoped
public class ContextCategoryBean extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    private List<ContextCategory> allContextCategories;
    private List<ContextCategory> contextCategories;
    private String name;

    @PostConstruct
    public void init() {
        setContextCategories(allContextCategories());
    }

    public List<ContextCategory> allContextCategories() {
        allContextCategories = contextCategoryService.findAll(Sort.Direction.DESC, "ctxCategoryId");
        return allContextCategories;
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
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allContextCategories().stream()
                    .map(e -> e.getName())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allContextCategories().stream()
                    .map(e -> e.getName())
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
        if (StringUtils.isEmpty(name)) {
            setContextCategories(allContextCategories());
        } else {
            setContextCategories(
                    allContextCategories().stream()
                            .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}