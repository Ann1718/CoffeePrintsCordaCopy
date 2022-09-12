package com.template.states;

import com.template.contracts.CoffeeBatchContract;
import com.template.dto.*;
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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@BelongsToContract(CoffeeBatchContract.class)
public class CoffeeBatchState implements LinearState {
    private CoffeeDetails coffeeDetails;
    private UniqueIdentifier batchId;
    private Party owner;
    private Party seller;
    private int price;
    private boolean isSold;
    private RoastedCoffee roastedCoffee;
    private FarmerDetails farmerDetails;


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.owner, this.seller);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return batchId;
    }

}