package org.oagi.srt.web.jsf.beans.namespace;

import org.oagi.srt.repository.entity.Namespace;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.NamespaceService;
import org.oagi.srt.service.UserService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Controller
@Scope(SCOPE_SESSION)
@ManagedBean
@SessionScoped
public class NamespaceBean extends UIHandler {

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private UserService userService;

    private List<Namespace> allNamespaceList;
    private Map<Long, User> userMap;

    private List<Namespace> namespaceList;
    private String uri;

    @PostConstruct
    public void init() {
        allNamespaceList = namespaceService.findAll(Sort.Direction.DESC, "creationTimestamp");
        setNamespaceList(allNamespaceList);

        userMap = userService.findByUserIds(
            allNamespaceList.stream()
                    .map(Namespace::getOwnerUserId)
                    .distinct().collect(Collectors.toList())
        );
    }

    private List<Namespace> getAllNamespaceList() {
        return allNamespaceList;
    }

    public List<Namespace> getNamespaceList() {
        return namespaceList;
    }

    public void setNamespaceList(List<Namespace> namespaceList) {
        this.namespaceList = namespaceList;
    }

    public String getUserName(long userId) {
        User user = userMap.get(userId);
        return (user != null) ? user.getLoginId() : "";
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return getAllNamespaceList().stream()
                    .map(e -> e.getUri())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return getAllNamespaceList().stream()
                    .map(e -> e.getUri())
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
        String uri = getUri();
        if (StringUtils.isEmpty(uri)) {
            setNamespaceList(getAllNamespaceList());
        } else {
            setNamespaceList(
                    getAllNamespaceList().stream()
                            .filter(e -> e.getUri().toLowerCase().contains(uri.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}
