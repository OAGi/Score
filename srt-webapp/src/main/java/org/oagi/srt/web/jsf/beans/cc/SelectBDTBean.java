package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Controller
@Scope(SCOPE_SESSION)
@ManagedBean
@SessionScoped
@Transactional(readOnly = true)
public class SelectBDTBean extends UIHandler {

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @PostConstruct
    public void init() {

    }
}
