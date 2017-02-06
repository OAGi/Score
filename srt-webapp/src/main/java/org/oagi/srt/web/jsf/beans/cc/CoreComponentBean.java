package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Controller
@Scope(SCOPE_SESSION)
@ManagedBean
@SessionScoped
@Transactional(readOnly = true)
public class CoreComponentBean extends UIHandler {

    @Autowired
    private AggregateCoreComponentRepository accRepository;
    @Autowired
    private AssociationCoreComponentRepository asccRepository;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private BasicCoreComponentRepository bccRepository;
    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CoreComponentService coreComponentService;

    private List<AggregateCoreComponent> accList;
    private List<AssociationCoreComponent> asccList;
    private List<AssociationCoreComponentProperty> asccpList;
    private List<BasicCoreComponent> bccList;
    private List<BasicCoreComponentProperty> bccpList;

    private String type = "ACC";
    private List<CoreComponentState> selectedStates;

    private String searchText;

    @PostConstruct
    public void init() {
        selectedStates = Arrays.asList(CoreComponentState.Editing);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void onTypeChange() {

    }

    public String[] getSelectedStates() {
        if (selectedStates == null || selectedStates.isEmpty()) {
            return new String[0];
        }

        String[] selectedStateStrings = new String[selectedStates.size()];
        int index = 0;
        for (CoreComponentState coreComponentState : selectedStates) {
            selectedStateStrings[index++] = coreComponentState.toString();
        }
        return selectedStateStrings;
    }

    public void setSelectedStates(String[] selectedStates) {
        if (selectedStates != null && selectedStates.length > 0) {
            this.selectedStates = new ArrayList();
            for (String selectedState : selectedStates) {
                this.selectedStates.add(CoreComponentState.valueOf(selectedState));
            }
        }
    }

    public void onStateChange() {
        reset();
    }

    public List<AggregateCoreComponent> getAccList() {
        if (accList == null) {
            if (selectedStates == null || selectedStates.isEmpty()) {
                accList = accRepository.findAllByRevisionNum(0);
            }
            accList = accRepository.findAllByRevisionNumAndStates(0, selectedStates);
        }

        if (searchText != null) {
            return accList.stream()
                    .filter(e -> e.getObjectClassTerm().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            return accList;
        }
    }

    public List<AssociationCoreComponent> getAsccList() {
        if (asccList == null) {
            if (selectedStates == null || selectedStates.isEmpty()) {
                asccList = asccRepository.findAllByRevisionNum(0);
            }
            asccList = asccRepository.findAllByRevisionNumAndStates(0, selectedStates);
        }

        if (searchText != null) {
            return asccList.stream()
                    .filter(e -> e.getDen().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            return asccList;
        }
    }

    public List<AssociationCoreComponentProperty> getAsccpList() {
        if (asccpList == null) {
            if (selectedStates == null || selectedStates.isEmpty()) {
                asccpList = asccpRepository.findAllByRevisionNum(0);
            }
            asccpList = asccpRepository.findAllByRevisionNumAndStates(0, selectedStates);
        }

        if (searchText != null) {
            return asccpList.stream()
                    .filter(e -> e.getPropertyTerm().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            return asccpList;
        }
    }

    public List<BasicCoreComponent> getBccList() {
        if (bccList == null) {
            if (selectedStates == null || selectedStates.isEmpty()) {
                bccList = bccRepository.findAllByRevisionNum(0);
            }
            bccList = bccRepository.findAllByRevisionNumAndStates(0, selectedStates);
        }

        if (searchText != null) {
            return bccList.stream()
                    .filter(e -> e.getDen().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            return bccList;
        }
    }

    public List<BasicCoreComponentProperty> getBccpList() {
        if (bccpList == null) {
            if (selectedStates == null || selectedStates.isEmpty()) {
                bccpList = bccpRepository.findAllByRevisionNum(0);
            }
            bccpList = bccpRepository.findAllByRevisionNumAndStates(0, selectedStates);
        }

        if (searchText != null) {
            return bccpList.stream()
                    .filter(e -> e.getPropertyTerm().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            return bccpList;
        }
    }

    private Map<Long, String> userNameMap = new HashMap();

    public String getUserName(Long appUserId) {
        if (!userNameMap.containsKey(appUserId)) {
            User user = userRepository.findOne(appUserId);
            userNameMap.put(appUserId, (user != null) ? user.getLoginId() : "");
        }
        return userNameMap.get(appUserId);
    }

    private Map<Long, String> accObjectClassTermMap = new HashMap();

    public String getObjectClassTermByAccId(Long accId) {
        if (!accObjectClassTermMap.containsKey(accId)) {
            AggregateCoreComponent acc = accRepository.findOne(accId);
            if (OagisComponentType.UserExtensionGroup == acc.getOagisComponentType()) {
                Long parentAccId = getParentAccIdOfUserExtensionGroupAcc(acc.getAccId());
                return getObjectClassTermByAccId(parentAccId);
            }
            accObjectClassTermMap.put(accId, (acc != null) ? acc.getObjectClassTerm() : "");
        }
        return accObjectClassTermMap.get(accId);
    }

    public Long getParentAccIdOfUserExtensionGroupAcc(Long ueAccId) {
        AssociationCoreComponentProperty asccp = asccpRepository.findOneByRoleOfAccId(ueAccId);
        List<AssociationCoreComponent> asccList = asccRepository.findByToAsccpIdAndRevisionNum(asccp.getAsccpId(), 0);
        if (asccList.isEmpty() || asccList.size() > 1) {
            throw new IllegalStateException();
        }
        AssociationCoreComponent ascc = asccList.get(0);
        return ascc.getFromAccId();
    }

    private Map<Long, String> asccpPropertyTermMap = new HashMap();

    public String getPropertyTermByAsccpId(Long asccpId) {
        if (!asccpPropertyTermMap.containsKey(asccpId)) {
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(asccpId);
            asccpPropertyTermMap.put(asccpId, (asccp != null) ? asccp.getPropertyTerm() : "");
        }
        return asccpPropertyTermMap.get(asccpId);
    }

    private Map<Long, String> bccpPropertyTermMap = new HashMap();

    public String getPropertyTermByBccpId(Long bccpId) {
        if (!bccpPropertyTermMap.containsKey(bccpId)) {
            BasicCoreComponentProperty bccp = bccpRepository.findOne(bccpId);
            bccpPropertyTermMap.put(bccpId, (bccp != null) ? bccp.getPropertyTerm() : "");
        }
        return bccpPropertyTermMap.get(bccpId);
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            switch (getType()) {
                case "ACC":
                    return getAccList().stream()
                            .map(e -> e.getObjectClassTerm())
                            .collect(Collectors.toList());
                case "ASCC":
                    return getAsccList().stream()
                            .map(e -> e.getDen())
                            .collect(Collectors.toList());
                case "ASCCP":
                    return getAsccpList().stream()
                            .map(e -> e.getPropertyTerm())
                            .collect(Collectors.toList());
                case "BCC":
                    return getBccList().stream()
                            .map(e -> e.getDen())
                            .collect(Collectors.toList());
                case "BCCP":
                    return getBccpList().stream()
                            .map(e -> e.getPropertyTerm())
                            .collect(Collectors.toList());
                default:
                    throw new IllegalStateException();
            }
        } else {
            String[] split = q.split(" ");

            switch (getType()) {
                case "ACC":
                    return getAccList().stream()
                            .map(e -> e.getObjectClassTerm())
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
                case "ASCC":
                    return getAsccList().stream()
                            .map(e -> e.getDen())
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
                case "ASCCP":
                    return getAsccpList().stream()
                            .map(e -> e.getPropertyTerm())
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
                case "BCC":
                    return getBccList().stream()
                            .map(e -> e.getDen())
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
                case "BCCP":
                    return getBccpList().stream()
                            .map(e -> e.getPropertyTerm())
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
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public void search() {
        reset();
    }

    private void reset() {
        accList = null;
        asccList = null;
        asccpList = null;
        bccList = null;
        bccpList = null;
    }

    @Transactional
    public String createACC() {
        User requester = getCurrentUser();
        AggregateCoreComponent acc = coreComponentService.newAggregateCoreComponent(requester);

        return "/views/core_component/acc_details.xhtml?accId=" + acc.getAccId() + "&faces-redirect=true";
    }
}
