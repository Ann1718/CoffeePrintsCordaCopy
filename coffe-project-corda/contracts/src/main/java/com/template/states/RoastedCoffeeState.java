package com.template.states;


import com.template.contracts.CoffeeBatchContract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@BelongsToContract(CoffeeBatchContract.class)
public class RoastedCoffeeState implements LinearState {

    private String roastType;
    private String grade;
    private String flavor;
    private int price;
    private UniqueIdentifier coffeeId;
    private UniqueIdentifier batchId;
    private UniqueIdentifier invoiceId;
    private Date purchaseDate;
    private Date roastedDate;
    private boolean isSold;
    private String unit;
    private String status;
    private String description;
    private Party owner;
    private Party seller;

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.owner, this.seller);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.batchId;
    }

}