package org.oagi.srt.web.jsf.beans.user;

import org.oagi.srt.web.handler.UIHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class AuthenticationBean extends UIHandler {

    public boolean isOagisDeveloper() {
        return getCurrentUser().isOagisDeveloperIndicator();
    }
}
