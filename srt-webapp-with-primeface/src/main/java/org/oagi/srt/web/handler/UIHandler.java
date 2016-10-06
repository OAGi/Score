package org.oagi.srt.web.handler;

import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.UserService;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UIHandler {

    @Autowired
    private UserService userService;

    protected User currentUser;
    protected long userId;

    @PostConstruct
    public void init() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            currentUser = userService.findByAuthentication(authentication);
            userId = currentUser.getAppUserId();
        }
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }
}
