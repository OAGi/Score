package org.oagi.srt.web.jsf.beans.cc.module;

import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.ModuleService;
import org.oagi.srt.service.UserService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ModuleBean extends UIHandler {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private UserService userService;

    private String moduleFilename;

    private List<Module> allModules;

    public List<Module> getAllModules() {
        if (Objects.isNull(allModules)){
            setAllModules(moduleService.findAll());
        }
        return allModules;
    }

    public void setAllModules(List<Module> allModules) {
        this.allModules = allModules;
    }

    public String getModuleFilename() {
        return moduleFilename;
    }

    public void setModuleFilename(String moduleFilename) {
        this.moduleFilename = moduleFilename;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return getAllModules().stream()
                    .map(e -> e.getModule())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return getAllModules().stream()
                    .map(e -> e.getModule())
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
        String module = getModuleFilename();
        if (StringUtils.isEmpty(module)) {
            setAllModules(moduleService.findAll());
        } else {
            setAllModules(
                    getAllModules().stream()
                            .filter(e -> e.getModule().toLowerCase().contains(module.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    public String getUser(long ownerId){
        User user = userService.findByUserId(ownerId);
        return Objects.nonNull(user) ? user.getLoginId() : "";
    }
}

