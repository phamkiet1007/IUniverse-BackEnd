package com.iuniverse.controller.request;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AddressRequest implements Serializable {

    private String street;
    private String city;
    private String country;
    private String building;
    private Integer addressType;
}
