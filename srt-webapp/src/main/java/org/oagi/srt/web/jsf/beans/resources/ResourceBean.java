package org.oagi.srt.web.jsf.beans.resources;

import org.oagi.srt.repository.entity.User;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import java.util.ResourceBundle;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Controller
@Scope(SCOPE_SESSION)
@ManagedBean
@SessionScoped
public class ResourceBean extends UIHandler {

    public String termKeyPrefix() {
        User user = getCurrentUser();
        return user.getProperty("termKeyPrefix", "ccts");
    }

    public void termKeyPrefix(String termKeyPrefix) {
        User user = getCurrentUser();
        user.setProperty("termKeyPrefix", termKeyPrefix);
    }

    public ResourceBundle termBundle() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getResourceBundle(context, "term");
    }

    public String term(String key) {
        String termKeyPrefix = termKeyPrefix();
        ResourceBundle bundle = termBundle();
        return bundle.getString(termKeyPrefix + "." + key);
    }
}
