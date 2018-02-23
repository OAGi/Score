package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.ProfileBODRepository;
import org.oagi.srt.repository.entity.ProfileBOD;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.ProfileBODGenerateService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ProfileBODGenerationBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileBODRepository profileBODRepository;
    @Autowired
    private ProfileBODGenerateService standaloneXMLSchema;

    @Autowired
    private BusinessInformationEntityService bieService;

    private ProfileBODGenerationOption option = new ProfileBODGenerationOption();

    private List<ProfileBOD> allProfileBODs;
    private String selectedPropertyTerm;
    private List<ProfileBOD> profileBODs;
    private List<ProfileBOD> selectedProfileBODs;

    @PostConstruct
    public void init() {
        User currentUser = getCurrentUser();
        allProfileBODs = profileBODRepository.findAll();
        allProfileBODs = allProfileBODs.stream()
                .filter(e -> bieService.canUserGenerateThisProfileBOD(e, currentUser)).collect(Collectors.toList());
        search();
    }

    public ProfileBODGenerationOption getOption() {
        return option;
    }

    public void setOption(ProfileBODGenerationOption option) {
        this.option = option;
    }

    public List<ProfileBOD> getAllProfileBODs() {
        return allProfileBODs;
    }

    public void setAllProfileBODs(List<ProfileBOD> allProfileBODs) {
        this.allProfileBODs = allProfileBODs;
    }

    public String getSelectedPropertyTerm() {
        return selectedPropertyTerm;
    }

    public void setSelectedPropertyTerm(String selectedPropertyTerm) {
        this.selectedPropertyTerm = selectedPropertyTerm;
    }

    public List<ProfileBOD> getProfileBODs() {
        return profileBODs;
    }

    public void setProfileBODs(List<ProfileBOD> profileBODs) {
        this.profileBODs = profileBODs;
    }

    public List<ProfileBOD> getSelectedProfileBODs() {
        return selectedProfileBODs;
    }

    public void setSelectedProfileBODs(List<ProfileBOD> selectedProfileBODs) {
        this.selectedProfileBODs = selectedProfileBODs;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allProfileBODs.stream()
                    .map(e -> e.getPropertyTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allProfileBODs.stream()
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
        List<ProfileBOD> profileBODs;
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            profileBODs = allProfileBODs.stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList());
        } else {
            profileBODs = allProfileBODs.stream()
                    .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }

        Collections.sort(profileBODs, (a, b) ->
                (int) (b.getCreationTimestamp().getTime() - a.getCreationTimestamp().getTime()));
        setProfileBODs(profileBODs);
    }

    private File generateSchemaFile;

    public void generate() throws Exception {
        List<Long> topLevelAbieIds = new ArrayList();
        for (ProfileBOD selectedProfileBOD : getSelectedProfileBODs()) {
            topLevelAbieIds.add(selectedProfileBOD.getTopLevelAbieId());
        }

        ProfileBODGenerationOption option = getOption();
        generateSchemaFile = standaloneXMLSchema.generateXMLSchema(topLevelAbieIds, option);
    }

    public StreamedContent getFile() throws Exception {
        return toStreamedContent(generateSchemaFile);
    }

    public StreamedContent toStreamedContent(File file) throws IOException {
        InputStream stream = new FileInputStream(file);
        String filePath = file.getCanonicalPath();
        return new DefaultStreamedContent(stream, "text/xml", filePath.substring(filePath.lastIndexOf("/") + 1));
    }
}
