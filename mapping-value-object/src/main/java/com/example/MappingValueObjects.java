package com.example;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import java.util.List;

@Slf4j
public class MappingValueObjects {

    private static SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory(Customer.class, Phone.class);

    public static void main(String[] args) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Customer customer = new Customer();
            customer.setFirstName("Jack");
            customer.setLastName("Black");
            customer.addPhone(Phone.of("mobile", "010", "250", "777-8888"));
            customer.addPhone(Phone.of("office", "010", "250", "777-9999"));

            // This Phone instance is a Value Object equals to previous added one so it won't be added into the collection
            customer.addPhone(Phone.of("office", "010", "250", "777-9999"));
            session.save(customer);

            transaction.commit();
        } catch (Throwable e) {
            e.printStackTrace();
            if(transaction != null) {
                transaction.rollback();
            }
        }

        // Try to query the data in a new transaction
        try(Session session = sessionFactory.openSession()) {
            NativeQuery<Customer> query = session.createNativeQuery("SELECT * FROM Customer", Customer.class);
            query.list().forEach( p ->  {
                log.info("{} : {} ", p.getId(), p.getPhones().size());
                assert p.getPhones().size() == 2 : "2 Phone Numbers should present";
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // Search by Phone Number
        try(Session session = sessionFactory.openSession()) {

            org.hibernate.query.Query<Customer> query = session.createQuery(
                    "SElECT DISTINCT c FROM Customer c JOIN c.phones p WHERE p.number like '%777%'",
                    Customer.class);
            List<Customer> customers = query.getResultList();
            assert customers.size() == 1 : "There should be only one Customer record";
        } catch (Throwable e) {
            e.printStackTrace();
        }

        SessionFactoryManager.shutdown();
    }
}
