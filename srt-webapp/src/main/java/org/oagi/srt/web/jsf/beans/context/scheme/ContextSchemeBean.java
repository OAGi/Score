package org.oagi.srt.web.jsf.beans.context.scheme;

import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.service.ContextSchemeService;
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
public class ContextSchemeBean {

    @Autowired
    private ContextSchemeService contextSchemeService;

    private List<ContextScheme> allContextSchemes;
    private List<ContextScheme> contextSchemes;
    private String name;

    @PostConstruct
    public void init() {
        setContextSchemes(allContextSchemes());
    }

    public List<ContextScheme> allContextSchemes() {
        allContextSchemes = contextSchemeService.findAll(Sort.Direction.DESC, "ctxSchemeId");
        return allContextSchemes;
    }

    public List<ContextScheme> getContextSchemes() {
        return contextSchemes;
    }

    public void setContextSchemes(List<ContextScheme> contextSchemes) {
        this.contextSchemes = contextSchemes;
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
            return allContextSchemes().stream()
                    .map(e -> e.getSchemeName())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allContextSchemes().stream()
                    .map(e -> e.getSchemeName())
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
            setContextSchemes(allContextSchemes());
        } else {
            setContextSchemes(
                    allContextSchemes().stream()
                            .filter(e -> e.getSchemeName().toLowerCase().contains(name.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}
