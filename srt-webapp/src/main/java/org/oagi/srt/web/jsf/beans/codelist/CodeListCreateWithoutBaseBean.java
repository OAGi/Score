package org.oagi.srt.web.jsf.beans.codelist;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListCreateWithoutBaseBean extends CodeListBaseBean {

    @PostConstruct
    public void init() {
        super.init();
        setAllUsedIndicator(false);
    }
}
