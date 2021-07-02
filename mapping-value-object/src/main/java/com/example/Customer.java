package com.example;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /**
     *  Explicit declare table name and join columns is also doable:
        @ElementCollection
        @CollectionTable(
                name="PHONE",
                joinColumns=@JoinColumn(name="OWNER_ID")
        )
     */
    @ElementCollection
    @CollectionTable
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
