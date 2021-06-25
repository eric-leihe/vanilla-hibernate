import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.hibernate.query.NativeQuery;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import javax.persistence.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class HikariHibernate {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory = getSessionFactory();

    public static void main(String[] args) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Person person = new Person();
            person.setAge(18);
            person.setFirstName("Jack");
            person.setLastName("Unknown");

            session.save(person);

            transaction.commit();
        } catch (Throwable e) {
            e.printStackTrace();
            if(transaction != null) {
                transaction.rollback();
            }
        }

        // Try to query the data in a new transaction
        try(Session session = sessionFactory.openSession()) {
            NativeQuery<Person> query = session.createNativeQuery("SELECT * FROM Person", Person.class);
            query.list().forEach( p -> print(String.valueOf(p.getId()),
                                        p.getFirstName(), p.getLastName(), String.valueOf(p.getAge())));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        shutdown();
    }
    
    static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder();

            Map<String, Object> settings = new HashMap<>();
            settings.put(Environment.CONNECTION_PROVIDER, HikariCPConnectionProvider.class.getName());
            settings.put(Environment.DRIVER, "org.h2.Driver");
            settings.put(Environment.URL, "jdbc:h2:~/test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=1;");
            settings.put(Environment.USER, "sa");
            settings.put(Environment.PASS, "sa");
            settings.put(Environment.HBM2DDL_AUTO, "update");
            settings.put(Environment.SHOW_SQL, true);

            settings.put("hibernate.hikari.dataSource.cachePrepStmts", "true");
            settings.put("hibernate.hikari.dataSource.prepStmtCacheSize", "250");
            settings.put("hibernate.hikari.dataSource.prepStmtCacheSqlLimit", "2048");

            registryBuilder.applySettings(settings);

            registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(Person.class);
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

    static void print(String... msgs) {
        System.out.println(String.join("\n", msgs));
    }


    @Entity
    @Table(name = "PERSON")
    static class Person {

        private Long id;

        public void setId(Long id) {
            this.id = id;
        }

        @Id
        @GeneratedValue
        public Long getId() {
            return id;
        }

        @Column(nullable = false)
        private String firstName;

        @Column(nullable = false)
        private String lastName;

        @Column
        private int age;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
