package org.oagi.srt.web.jsf.beans.cc.release;

import org.oagi.srt.repository.entity.Releases;
import org.oagi.srt.service.ReleaseService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
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
public class ReleaseBean extends UIHandler {

    @Autowired
    private ReleaseService releaseService;

    private List<Releases> allReleases;
    private String releaseNum;
    private Releases selectedReleases;

    @PostConstruct
    public void init() {
        setAllReleases(releaseService.findAllReleases());
    }

    public void makeReleaseFinal(Releases release, boolean purge) {
        setAllReleases(null); // trigger refresh
        releaseService.makeReleaseFinal(release, purge);
    }

    public void makeReleaseFinal (boolean purge) {
        if (selectedReleases != null) {
            setAllReleases(null); // trigger refresh
            releaseService.makeReleaseFinal(selectedReleases, purge);
        }
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return getAllReleases().stream()
                    .map(e -> e.getReleaseNum())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return getAllReleases().stream()
                    .map(e -> e.getReleaseNum())
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
        String releaseNum = getReleaseNum();
        if (StringUtils.isEmpty(releaseNum)) {
            setAllReleases(getAllReleases());
        } else {
            setAllReleases(
                    getAllReleases().stream()
                            .filter(e -> e.getReleaseNum().toLowerCase().contains(releaseNum.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    public List<Releases> getAllReleases() {
        if (allReleases == null) {
            setAllReleases(releaseService.findAllReleases());
        }
        return allReleases;
    }

    public void setAllReleases(List<Releases> allReleases) {
        this.allReleases = allReleases;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    public Releases getSelectedReleases() {
        return selectedReleases;
    }

    public void setSelectedReleases(Releases selectedReleases) {
        this.selectedReleases = selectedReleases;
    }
}
