package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.repository.ProfileBODRepository;
import org.oagi.srt.repository.entity.ProfileBOD;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ProfileBODBean extends UIHandler {

    @Autowired
    private ProfileBODRepository profileBODRepository;

    @Autowired
    private BusinessInformationEntityService bieService;

    private List<ProfileBOD> allProfileBODs;
    private String selectedPropertyTerm;
    private List<ProfileBOD> profileBODs;

    private ProfileBOD selectedProfileBOD;

    @PostConstruct
    public void init() {
        User user = loadAuthentication();
        allProfileBODs = profileBODRepository.findAllByCreatedBy(user.getAppUserId());
        setProfileBODs(
                allProfileBODs.stream()
                        .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                        .collect(Collectors.toList())
        );
    }

    public List<ProfileBOD> getProfileBODs() {
        return profileBODs;
    }

    public void setProfileBODs(List<ProfileBOD> profileBODs) {
        this.profileBODs = profileBODs;
    }

    public String getSelectedPropertyTerm() {
        return selectedPropertyTerm;
    }

    public void setSelectedPropertyTerm(String selectedPropertyTerm) {
        this.selectedPropertyTerm = selectedPropertyTerm;
    }

    public ProfileBOD getSelectedProfileBOD() {
        return selectedProfileBOD;
    }

    public void setSelectedProfileBOD(ProfileBOD selectedProfileBOD) {
        this.selectedProfileBOD = selectedProfileBOD;
    }

    public List<String> completeInput(String query) {
        return allProfileBODs.stream()
                .map(e -> e.getPropertyTerm())
                .distinct()
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setProfileBODs(allProfileBODs.stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setProfileBODs(
                    allProfileBODs.stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    public void deleteProfileBOD() {
        ProfileBOD profileBOD = getSelectedProfileBOD();
        if (profileBOD == null) {
            return;
        }

        bieService.deleteProfileBOD(profileBOD.getTopLevelAbieId());
        init();
    }
}
