package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.OagisComponentType;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public abstract class BaseCoreComponentDetailBean extends UIHandler {

    @Autowired
    private UserRepository userRepository;

    public User getOwnerUser(AggregateCoreComponent acc) {
        long ownerUserId = acc.getOwnerUserId();
        return userRepository.findOne(ownerUserId);
    }

    public Map<String, OagisComponentType> availableOagisComponentTypes(AggregateCoreComponent acc) {
        User owner = getOwnerUser(acc);

        Map<String, OagisComponentType> availableOagisComponentTypes = new LinkedHashMap();
        availableOagisComponentTypes.put("Base", OagisComponentType.Base);
        availableOagisComponentTypes.put("Semantics", OagisComponentType.Semantics);
        if (owner.isOagisDeveloperIndicator()) {
            availableOagisComponentTypes.put("Extension", OagisComponentType.Extension);
        }
        availableOagisComponentTypes.put("Semantic Group", OagisComponentType.SemanticGroup);

        return availableOagisComponentTypes;
    }

}
