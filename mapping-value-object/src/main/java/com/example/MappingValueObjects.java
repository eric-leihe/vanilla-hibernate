package com.example;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import javax.persistence.*;
import java.util.ArrayList;
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
            log.info("==> Found {} customer(s)", customers.size());
            assert customers.size() == 1 : "Customer should be found";
        } catch (Throwable e) {
            e.printStackTrace();
        }

        SessionFactoryManager.shutdown();
    }


    @Data
    @Entity(name = "Customer")
    @Table(name = "CUSTOMER")
    static class Customer {

        @Id
        @GeneratedValue
        private Long id;

        @Column(nullable = false)
        private String firstName;

        @Column(nullable = false)
        private String lastName;

        @ElementCollection
        @CollectionTable(
                name="PHONE",
                joinColumns=@JoinColumn(name="OWNER_ID")
        )
        @Setter(AccessLevel.NONE)
        private List<Phone> phones = new ArrayList<>();

        public void addPhone(Phone phone) {
            if(!this.phones.contains(phone)) {
                this.phones.add(phone);
            }
        }

        public void removePhone(Phone phone) {
            if(this.phones.contains(phone)) {
                this.phones.remove(phone);
            }
        }
    }

    @Value
    @NoArgsConstructor(force = true)
    @RequiredArgsConstructor(staticName = "of")
    @Embeddable
    static class Phone {

        @Column(name = "PHONE_TYPE")
        private String type;

        @Column(name = "COUNTRY_CODE")
        private String countryCode;

        @Column(name = "AREA_CODE")
        private String areaCode;

        @Column(name = "PHONE_NUMBER")
        private String number;

    }
}
