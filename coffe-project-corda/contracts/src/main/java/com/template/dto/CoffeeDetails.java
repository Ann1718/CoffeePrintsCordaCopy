package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CordaSerializable
public class CoffeeDetails {
    private String type;
    private String varietal;
    private String temperature; //int
    private String shadedCover;
    private String process;
    private String tradedGood;
    private boolean sorted;
    private String unit;
    private int convertedProducts;
    private int totalQuantity;
    private int soldQuantity;
    private Date harvestedDate;
    private UUID txId;
}