package org.oagi.srt.web.jsf.beans.bie;

import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.ProfileBIERepository;
import org.oagi.srt.repository.entity.ProfileBIE;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.expression.ProfileBIEGenerateService;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaExpression.JSON;
import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaExpression.XML;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ProfileBIEGenerationBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileBIERepository profileBIERepository;
    @Autowired
    private ProfileBIEGenerateService profileBIEGenerateService;

    @Autowired
    private BusinessInformationEntityService bieService;

    private ProfileBODGenerationOption option = new ProfileBODGenerationOption();

    private List<ProfileBIE> allProfileBIEs;
    private String selectedPropertyTerm;
    private List<ProfileBIE> profileBIEs;
    private List<ProfileBIE> selectedProfileBIEs;

    @PostConstruct
    public void init() {
        User currentUser = getCurrentUser();
        allProfileBIEs = profileBIERepository.findAll();
        allProfileBIEs = allProfileBIEs.stream()
                .filter(e -> bieService.canUserGenerateThisProfileBOD(e, currentUser)).collect(Collectors.toList());
        search();
    }

    public ProfileBODGenerationOption getOption() {
        return option;
    }

    public void setOption(ProfileBODGenerationOption option) {
        this.option = option;
    }

    public List<ProfileBIE> getAllProfileBIEs() {
        return allProfileBIEs;
    }

    public void setAllProfileBIEs(List<ProfileBIE> allProfileBIEs) {
        this.allProfileBIEs = allProfileBIEs;
    }

    public String getSelectedPropertyTerm() {
        return selectedPropertyTerm;
    }

    public void setSelectedPropertyTerm(String selectedPropertyTerm) {
        this.selectedPropertyTerm = selectedPropertyTerm;
    }

    public List<ProfileBIE> getProfileBIEs() {
        return profileBIEs;
    }

    public void setProfileBIEs(List<ProfileBIE> profileBIEs) {
        this.profileBIEs = profileBIEs;
    }

    public List<ProfileBIE> getSelectedProfileBIEs() {
        return selectedProfileBIEs;
    }

    public void setSelectedProfileBIEs(List<ProfileBIE> selectedProfileBIEs) {
        this.selectedProfileBIEs = selectedProfileBIEs;
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
        List<ProfileBIE> profileBIES;
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            profileBIES = allProfileBIEs.stream()
                    .sorted(Comparator.comparing(ProfileBIE::getPropertyTerm))
                    .collect(Collectors.toList());
        } else {
            profileBIES = allProfileBIEs.stream()
                    .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }

        Collections.sort(profileBIES, (a, b) ->
                (int) (b.getCreationTimestamp().getTime() - a.getCreationTimestamp().getTime()));
        setProfileBIEs(profileBIES);
    }

    private ProfileBODGenerationOption backupOption;

    public void onChangeSchemaExpressionOption() {
        switch (this.option.getSchemaExpression()) {
            case XML:
                if (backupOption != null) {
                    this.option = backupOption;
                    backupOption = null;

                    this.option.setSchemaExpression(XML);
                }

                break;

            case JSON:
                backupOption = option.clone();
                this.option = new ProfileBODGenerationOption();
                this.option.setSchemaExpression(JSON);
                this.option.setSchemaPackage(backupOption.getSchemaPackage());

                break;
        }
    }

    private File generateSchemaFile;

    public void generate() throws Exception {
        List<Long> topLevelAbieIds = new ArrayList();
        for (ProfileBIE selectedProfileBIE : getSelectedProfileBIEs()) {
            topLevelAbieIds.add(selectedProfileBIE.getTopLevelAbieId());
        }

        ProfileBODGenerationOption option = getOption();
        generateSchemaFile = profileBIEGenerateService.generateSchema(topLevelAbieIds, option);
    }

    public StreamedContent getFile() throws Exception {
        return toStreamedContent(generateSchemaFile);
    }

    public StreamedContent toStreamedContent(File file) throws IOException {
        InputStream stream = new FileInputStream(file);
        String fileName = file.getName();

        String contentType;
        if (fileName.endsWith(".xsd")) {
            contentType = "text/xml";
        } else if (fileName.endsWith(".json")) {
            contentType = "application/json";
        } else if (fileName.endsWith(".zip")) {
            contentType = "application/zip";
        } else {
            contentType = "application/octet-stream";
        }

        return new DefaultStreamedContent(stream, contentType, fileName);
    }
}
