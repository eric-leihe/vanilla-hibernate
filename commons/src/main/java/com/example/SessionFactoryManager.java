package com.example;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.TargetType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class SessionFactoryManager {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public static org.hibernate.SessionFactory getSessionFactory(Class... mappingClazz) {
        if(sessionFactory == null) {
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder();

            Map<String, Object> settings = new HashMap<>();
            settings.put(Environment.CONNECTION_PROVIDER, HikariCPConnectionProvider.class.getName());
            settings.put(Environment.DRIVER, "org.h2.Driver");
            settings.put(Environment.URL, "jdbc:h2:~/test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=1;");
            settings.put(Environment.USER, "sa");
            settings.put(Environment.PASS, "sa");
            settings.put(Environment.HBM2DDL_AUTO, Action.CREATE);
            settings.put(Environment.SHOW_SQL, true);

            settings.put("hibernate.hikari.dataSource.cachePrepStmts", "true");
            settings.put("hibernate.hikari.dataSource.prepStmtCacheSize", "250");
            settings.put("hibernate.hikari.dataSource.prepStmtCacheSqlLimit", "2048");

            registryBuilder.applySettings(settings);

            registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry);
            for(Class clazz : mappingClazz) {
                sources.addAnnotatedClass(clazz);
            }
            Metadata metadata = sources.getMetadataBuilder().build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();

            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setHaltOnError(true);
            schemaExport.setFormat(true);
            schemaExport.setDelimiter(";");
            schemaExport.execute(EnumSet.of(TargetType.STDOUT), SchemaExport.Action.CREATE, metadata);
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
