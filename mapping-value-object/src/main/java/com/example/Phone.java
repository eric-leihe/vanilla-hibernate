package com.example;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Value
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
@Embeddable
public class Phone {

    @Column(name = "PHONE_TYPE")
    private String type;

    @Column(name = "COUNTRY_CODE")
    private String countryCode;

    @Column(name = "AREA_CODE")
    private String areaCode;

    @Column(name = "PHONE_NUMBER")
    private String number;

}
