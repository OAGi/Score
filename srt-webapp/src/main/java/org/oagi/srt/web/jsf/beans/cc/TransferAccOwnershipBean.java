package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class TransferAccOwnershipBean extends UIHandler {

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    private AggregateCoreComponent acc;
    private List<User> allUsers;
    private List<User> users;
    private String selectedLoginId;
    private User selectedUser;

    @PostConstruct
    public void init() {
        String paramAccId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("accId");
        Long accId = Long.parseLong(paramAccId);
        if (accId != null) {
            AggregateCoreComponent acc = accRepository.findOne(accId);
            setAcc(acc);
        }

        User currentUser = getCurrentUser();
        allUsers = userRepository.findAll().stream()
                .filter(e -> e.getAppUserId() != currentUser.getAppUserId())
                .filter(e -> !e.isOagisDeveloperIndicator())
                .collect(Collectors.toList());

        setUsers(allUsers);
    }

    public AggregateCoreComponent getAcc() {
        return acc;
    }

    public void setAcc(AggregateCoreComponent acc) {
        this.acc = acc;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getSelectedLoginId() {
        return selectedLoginId;
    }

    public void setSelectedLoginId(String selectedLoginId) {
        this.selectedLoginId = selectedLoginId;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allUsers.stream()
                    .map(e -> e.getLoginId())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allUsers.stream()
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
        String selectedLoginId = getSelectedLoginId();
        if (StringUtils.isEmpty(selectedLoginId)) {
            setUsers(allUsers);
        } else {
            setUsers(
                    allUsers.stream()
                            .filter(e -> e.getLoginId().toLowerCase().contains(selectedLoginId.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    public void onUserSelect(SelectEvent event) {
        setSelectedUser((User) event.getObject());
    }

    public void onUserUnselect(UnselectEvent event) {
        setSelectedUser(null);
    }

    @Transactional(rollbackFor = Throwable.class)
    public String transfer() {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "User must be selected."));
            return null;
        }

        coreComponentService.transferOwner(acc, selectedUser);

        return "/views/core_component/list.jsf?faces-redirect=true";
    }
}
