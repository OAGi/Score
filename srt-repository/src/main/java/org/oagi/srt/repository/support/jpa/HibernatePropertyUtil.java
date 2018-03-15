package org.oagi.srt.repository.support.jpa;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class HibernatePropertyUtil {

    @Autowired
    private EntityManager entityManager;

    public String getProperty(String key) {
        Session session = (Session) entityManager.getDelegate();
        Object property;
        try {
            property =
                    org.apache.commons.beanutils.PropertyUtils.getProperty(
                            session.getSessionFactory(), key);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
        return property.toString();
    }
}
