package org.oagi.srt.web.handler;

import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.UserService;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;

@Component
public class UIHandler {

    @Autowired
    private UserService userService;

    private User currentUser;

    public User loadAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            currentUser = userService.findByAuthentication(authentication);
        }

        if (currentUser == null) {
            try {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                if (facesContext != null) {
                    ExternalContext externalContext = facesContext.getExternalContext();
                    if (externalContext != null) {
                        externalContext.dispatch("/login.xhtml");
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return currentUser;
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }
}
