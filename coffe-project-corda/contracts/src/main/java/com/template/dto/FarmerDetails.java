package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CordaSerializable
public class FarmerDetails {
    private String name;
    private String location;
    private int yearsInFarming;
    private int farmSize;
    private int elevation;

}