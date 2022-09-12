package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CordaSerializable
public class RoastedCoffee {
    private int roastedCoffeeQuantity;
    private String roastType;
    private String grade;
    private String flavor;


}
