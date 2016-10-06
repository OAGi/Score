package org.oagi.srt.web.jsf.beans;

import org.oagi.srt.repository.entity.CodeList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListCreateFromAnother {

    private CodeList codeList = new CodeList();
}
