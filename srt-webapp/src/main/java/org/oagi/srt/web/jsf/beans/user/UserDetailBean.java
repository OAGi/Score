package org.oagi.srt.web.jsf.beans.user;

import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.UserService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.transaction.Transactional;
import java.io.IOException;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class UserDetailBean extends UIHandler {

    @Autowired
    private UserService userService;

    private User user;

    @PostConstruct
    public void init() throws IOException {
        User oagisUser = getCurrentUser();
        if (!oagisUser.isOagisDeveloperIndicator()) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect("/");
            return;
        }

        String paramLoginId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("loginId");
        if (StringUtils.isEmpty(paramLoginId)) {
            setUser(new User());
        } else {
            setUser(userService.findByLoginId(paramLoginId));
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Transactional
    public String update() {
        String loginId = user.getLoginId();
        if (userService.findByLoginId(loginId) != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Login ID is already taken."));
            return null;
        }

        try {
            userService.update(user);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        return "/views/user/manage_account.jsf?faces-redirect=true";
    }
}
