package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CoreComponentBean {

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

    private List<AggregateCoreComponent> accList;
    private List<AssociationCoreComponent> asccList;
    private List<AssociationCoreComponentProperty> asccpList;
    private List<BasicCoreComponent> bccList;
    private List<BasicCoreComponentProperty> bccpList;

    private String type = "ACC";
    private List<CoreComponentState> selectedStates = Arrays.asList(CoreComponentState.Editing);

    private String searchText;

    @PostConstruct
    public void init() {
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
                accList = accRepository.findAll(new Sort(Sort.Direction.DESC, "creationTimestamp"));
            }
            accList = accRepository.findByStates(selectedStates);
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
                asccList = asccRepository.findAll(new Sort(Sort.Direction.DESC, "creationTimestamp"));
            }
            asccList = asccRepository.findByStates(selectedStates);
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
                asccpList = asccpRepository.findAll(new Sort(Sort.Direction.DESC, "creationTimestamp"));
            }
            asccpList = asccpRepository.findByStates(selectedStates);
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
                bccList = bccRepository.findAll(new Sort(Sort.Direction.DESC, "creationTimestamp"));
            }
            bccList = bccRepository.findByStates(selectedStates);
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
                bccpList = bccpRepository.findAll(new Sort(Sort.Direction.DESC, "creationTimestamp"));
            }
            bccpList = bccpRepository.findByStates(selectedStates);
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
            accObjectClassTermMap.put(accId, (acc != null) ? acc.getObjectClassTerm() : "");
        }
        return accObjectClassTermMap.get(accId);
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
}
