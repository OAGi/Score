package org.oagi.srt.repository.support.jpa;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.ExportableProducer;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

public class ByDialectIdentifierGenerator implements IdentifierGenerator, Configurable {

    private MutableIdentifierGeneratorFactory identifierGeneratorFactory;
    private Type type;
    private Properties config;
    private Database database;

    private IdentifierGenerator identifierGenerator;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        this.identifierGeneratorFactory =
                serviceRegistry.getService(MutableIdentifierGeneratorFactory.class);
        this.type = type;
        this.config = params;

        MetadataBuildingOptions buildingOptions =
                new MetadataBuilderImpl.MetadataBuildingOptionsImpl((StandardServiceRegistry) serviceRegistry);
        this.database = new Database(buildingOptions);
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        IdentifierGenerator identifierGenerator = getIdentifierGenerator();
        return identifierGenerator.generate(session, object);
    }

    private IdentifierGenerator getIdentifierGenerator() {
        if (identifierGenerator != null) {
            return identifierGenerator;
        }

        Dialect dialect = identifierGeneratorFactory.getDialect();
        String strategy;
        if (dialect instanceof MySQLDialect || dialect instanceof HSQLDialect) {
            strategy = "increment";
        } else {
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator";
        }
        identifierGenerator = identifierGeneratorFactory.createIdentifierGenerator(strategy, type, config);
        if (identifierGenerator instanceof ExportableProducer) {
            ((ExportableProducer) identifierGenerator).registerExportables(database);
        }

        return identifierGenerator;
    }
}
