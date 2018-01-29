package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public abstract class AbstractCoreComponentBean extends UIHandler {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AggregateCoreComponentRepository accRepository;
    @Autowired
    private AssociationCoreComponentRepository asccRepository;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

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
        List<AssociationCoreComponentProperty> asccpList = asccpRepository.findByRoleOfAccId(ueAccId);
        if (asccpList.size() != 1) {
            throw new IllegalStateException();
        }
        AssociationCoreComponentProperty asccp = asccpList.get(0);
        List<AssociationCoreComponent> asccList = asccRepository.findByToAsccpIdAndRevisionNum(asccp.getAsccpId(), 0);
        if (asccList.size() != 1) {
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

}
