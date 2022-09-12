package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CoffeeBatchContract;
import com.template.dto.CoffeeDetails;
import com.template.dto.FarmerDetails;
import com.template.dto.RoastedCoffee;
import com.template.states.CoffeeBatchState;
import com.template.states.FarmerDetailsState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class AddCoffeeBatchFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class AddCoffeeBatchFlowInitiator extends FlowLogic<SignedTransaction> {

        private String type;
        private String varietal;
        private String temperature;
        private String shadedCover;
        private String process;
        private String tradedGood;
        private boolean isSorted;
        private String unit;
        private int totalQuantity;
        private Date harvestedDate;

        public AddCoffeeBatchFlowInitiator(String type, String varietal, String temperature, String shadedCover,
                                           String process, String tradedGood, boolean isSorted, String unit,
                                           int totalQuantity, Date harvestedDate) {
            this.type = type;
            this.varietal = varietal;
            this.temperature = temperature;
            this.shadedCover = shadedCover;
            this.process = process;
            this.tradedGood = tradedGood;
            this.isSorted = isSorted;
            this.unit = unit;
            this.totalQuantity = totalQuantity;
            this.harvestedDate = harvestedDate;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            StateAndRef farmerDetailsStateStateAndRef = getServiceHub().getVaultService().queryBy(FarmerDetailsState.class).getStates().get(0);

            FarmerDetailsState farmerDetailsState = (FarmerDetailsState) farmerDetailsStateStateAndRef.getState().getData();

            CoffeeDetails coffeeDetails = new CoffeeDetails(type, varietal, temperature, shadedCover, process, tradedGood,
                    isSorted, unit,0, totalQuantity, 0, harvestedDate,null);

            UniqueIdentifier batchId = new UniqueIdentifier();

            RoastedCoffee roastedCoffee = new RoastedCoffee(0, null, null, null);

            FarmerDetails farmerDetails = new FarmerDetails(farmerDetailsState.getName(), farmerDetailsState.getLocation(), farmerDetailsState.getYearsInFarming(),
                    farmerDetailsState.getFarmSize(), farmerDetailsState.getElevation());

            CoffeeBatchState coffeeBatchState = new CoffeeBatchState(coffeeDetails, batchId, this.getOurIdentity(),
                    this.getOurIdentity(), 0, false, roastedCoffee, farmerDetails);

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(coffeeBatchState)
                    .addCommand(new CoffeeBatchContract.Commands.CreateCoffeeBatch(),
                            Arrays.asList(this.getOurIdentity().getOwningKey()));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            return subFlow(new FinalityFlow(signedTx, Collections.emptyList()));
        }
    }
}