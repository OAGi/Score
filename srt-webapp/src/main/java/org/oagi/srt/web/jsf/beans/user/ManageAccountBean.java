package org.oagi.srt.web.jsf.beans.user;

import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.UserService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ManageAccountBean extends UIHandler {

    @Autowired
    private UserService userService;

    private List<User> allUserList;

    private List<User> userList;
    private String searchLoginId;

    @PostConstruct
    public void init() throws IOException {
        User oagisUser = getCurrentUser();
        if (!oagisUser.isOagisDeveloperIndicator()) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect("/");
            return;
        }

        search();
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public String getSearchLoginId() {
        return searchLoginId;
    }

    public void setSearchLoginId(String searchLoginId) {
        this.searchLoginId = searchLoginId;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allUserList.stream()
                    .map(e -> e.getLoginId())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allUserList.stream()
                    .map(e -> e.getLoginId())
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
        this.allUserList = userService.findAll();

        String searchLoginId = getSearchLoginId();
        if (StringUtils.isEmpty(searchLoginId)) {
            setUserList(allUserList);
        } else {
            setUserList(
                    allUserList.stream()
                            .filter(e -> e.getLoginId().toLowerCase().contains(searchLoginId.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}
