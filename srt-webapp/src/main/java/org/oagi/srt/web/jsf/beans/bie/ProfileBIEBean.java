package org.oagi.srt.web.jsf.beans.bie;

import org.oagi.srt.repository.ProfileBIERepository;
import org.oagi.srt.repository.entity.ProfileBIE;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.UserService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ProfileBIEBean extends UIHandler {

    @Autowired
    private ProfileBIERepository profileBIERepository;

    @Autowired
    private BusinessInformationEntityService bieService;

    @Autowired
    private UserService userService;

    private List<ProfileBIE> allProfileBIEs;
    private String selectedPropertyTerm;
    private List<ProfileBIE> profileBIEs;

    private ProfileBIE selectedProfileBIE;

    @PostConstruct
    public void init() {
        allProfileBIEs = profileBIERepository.findAll();
        search();
    }

    public List<ProfileBIE> getProfileBIEs() {
        return profileBIEs;
    }

    public void setProfileBIEs(List<ProfileBIE> profileBIEs) {
        this.profileBIEs = profileBIEs;
    }

    public String getSelectedPropertyTerm() {
        return selectedPropertyTerm;
    }

    public void setSelectedPropertyTerm(String selectedPropertyTerm) {
        this.selectedPropertyTerm = selectedPropertyTerm;
    }

    public ProfileBIE getSelectedProfileBIE() {
        return selectedProfileBIE;
    }

    public void setSelectedProfileBIE(ProfileBIE selectedProfileBIE) {
        this.selectedProfileBIE = selectedProfileBIE;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allProfileBIEs.stream()
                    .map(e -> e.getPropertyTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allProfileBIEs.stream()
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
        }
    }

    public void search() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setProfileBIEs(allProfileBIEs.stream()
                    .sorted(Comparator.comparing(ProfileBIE::getLastUpdateTimestamp).reversed())
                    .collect(Collectors.toList()));
        } else {
            setProfileBIEs(
                    allProfileBIEs.stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    @Transactional
    public void deleteProfileBIE() {
        ProfileBIE profileBIE = getSelectedProfileBIE();
        if (profileBIE == null) {
            return;
        }

        bieService.deleteProfileBIE(profileBIE.getTopLevelAbieId());
        init();
    }

    public boolean canCurrentUserSeeThisProfileBIE(ProfileBIE profileBIE) {
        return bieService.canUserSeeThisProfileBIE(profileBIE.getTopLevelAbieId(), getCurrentUser());
    }
}
