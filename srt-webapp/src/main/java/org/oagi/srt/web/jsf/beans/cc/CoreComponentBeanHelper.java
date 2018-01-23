package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Component
public class CoreComponentBeanHelper {

    @Autowired
    private UserRepository userRepository;

    private long getOwnerUserId(Object obj, Method getOwnerUserId) {
        try {
            return (Long) getOwnerUserId.invoke(obj, new Object[0]);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    private CoreComponentState getState(Object obj, Method getState) {
        try {
            return (CoreComponentState) getState.invoke(obj, new Object[0]);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    public <T> List<T> filterByUser(List<T> list, User currentUser, Class<T> clazz) {
        Method getOwnerUserIdMethod, getStateMethod;
        try {
            getOwnerUserIdMethod = clazz.getMethod("getOwnerUserId", new Class[]{});
            getStateMethod = clazz.getMethod("getState", new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }

        Map<Long, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getAppUserId, Function.identity()));

        if (currentUser.isOagisDeveloperIndicator()) {
            list = list.stream()
                    .filter(e -> {
                        return userMap.get(getOwnerUserId(e, getOwnerUserIdMethod)).isOagisDeveloperIndicator();
                    })
                    .collect(Collectors.toList());
        } else {
            list = list.stream()
                    .filter(e -> {
                        // End-Users should be able to access only released OAGi components.
                        User owner = userMap.get(getOwnerUserId(e, getOwnerUserIdMethod));
                        if (owner.isOagisDeveloperIndicator()) {
                            return Published == getState(e, getStateMethod);
                        } else {
                            return true;
                        }
                    })
                    .collect(Collectors.toList());
        }

        return list;
    }
}
