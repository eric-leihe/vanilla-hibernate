package com.example;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import javax.persistence.*;

@Slf4j
public class HikariHibernate {
    private static SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory(Person.class);

    public static void main(String[] args) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Person person = new Person();
            person.setAge(18);
            person.setFirstName("Jack");
            person.setLastName("Unknown");
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
            query.list().forEach( p -> log.info("{}, {}, {}, {}", p.getId(),
                                        p.getFirstName(), p.getLastName(), p.getAge()));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        SessionFactoryManager.shutdown();
    }

    @Data
    @Entity
    @Table(name = "PERSON")
    static class Person {

        @Id
        @GeneratedValue
        private Long id;

        @Column(nullable = false)
        private String firstName;

        @Column(nullable = false)
        private String lastName;

        @Column
        private int age;
    }
}
