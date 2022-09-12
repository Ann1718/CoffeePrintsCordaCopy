package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CoffeeBatchContract;
import com.template.dto.RoastedCoffee;
import com.template.states.CoffeeBatchState;
import com.template.states.RoastedCoffeeState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.*;

public class MakeRoastedCoffeeFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class MakeRoastedCoffeeFlowInitiator extends FlowLogic<SignedTransaction> {

        private int roastedCoffeeQuantity;
        private String roastType;
        private String grade;
        private String flavor;
        private int price;
        private Party customer;
        private String unit;
        private UUID txId;

        public MakeRoastedCoffeeFlowInitiator(int roastedCoffeeQuantity, String roastType, String grade, String flavor, int price, Party customer, String unit, UUID txId) {
            this.roastedCoffeeQuantity = roastedCoffeeQuantity;
            this.roastType = roastType;
            this.grade = grade;
            this.flavor = flavor;
            this.price = price;
            this.customer = customer;
            this.unit = unit;
            this.txId = txId;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {


            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateStateAndRef = getServiceHub().getVaultService()
                    .queryBy(CoffeeBatchState.class).getStates();

            CoffeeBatchState coffeeBatchState = null;

            int index = 0;
            for (int i=0; i<coffeeBatchStateStateAndRef.size(); i++) {
                if (coffeeBatchStateStateAndRef.get(i).getState().getData().getCoffeeDetails().getTxId().equals(this.txId)){
                    coffeeBatchState = coffeeBatchStateStateAndRef.get(i).getState().getData();
                    index = i;
                }
            }
//            StateAndRef coffeeBatchStateStateAndRef = getServiceHub().getVaultService().queryBy(CoffeeBatchState.class).getStates().get(0);

//            CoffeeBatchState coffeeBatchState = (CoffeeBatchState) coffeeBatchStateStateAndRef.getState().getData();


            coffeeBatchState.getCoffeeDetails().setConvertedProducts(coffeeBatchState.getCoffeeDetails().getConvertedProducts() + roastedCoffeeQuantity);
            coffeeBatchState.getCoffeeDetails().setSoldQuantity(coffeeBatchState.getCoffeeDetails().getSoldQuantity() + roastedCoffeeQuantity);

                RoastedCoffee roastedCoffee = new RoastedCoffee(roastedCoffeeQuantity, roastType, grade, flavor);

                CoffeeBatchState newCoffeeBatch = new CoffeeBatchState(coffeeBatchState.getCoffeeDetails(), coffeeBatchState.getBatchId(), this.customer,
                        this.getOurIdentity(), coffeeBatchState.getPrice(), coffeeBatchState.isSold(), roastedCoffee, coffeeBatchState.getFarmerDetails());

                String description = coffeeBatchState.getCoffeeDetails().getType().toUpperCase() + "-" + coffeeBatchState.getCoffeeDetails().getVarietal().toUpperCase() + "-"
                        + coffeeBatchState.getFarmerDetails().getLocation().toUpperCase();

            for (int i = 1; i <= roastedCoffeeQuantity; i++) {
                RoastedCoffeeState roastedCoffeeState = new RoastedCoffeeState(roastType, grade, flavor, price, new UniqueIdentifier(), coffeeBatchState.getBatchId(),null,
                        null,new Date(),false,this.unit,"NEW",description, this.customer, this.getOurIdentity());

                TransactionBuilder txBuilder = new TransactionBuilder(coffeeBatchStateStateAndRef.get(0).getState().getNotary())

                        .addOutputState(roastedCoffeeState)
                        .addCommand(new CoffeeBatchContract.Commands.CreateCoffeeBatch(),
                                Arrays.asList(this.getOurIdentity().getOwningKey(), this.customer.getOwningKey()));

                txBuilder.verify(getServiceHub());

                SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

                FlowSession session = initiateFlow(this.customer);

                SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx, Arrays.asList(session)));

                subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(session)));
            }

            TransactionBuilder txBuilder = new TransactionBuilder(coffeeBatchStateStateAndRef.get(0).getState().getNotary())
                    .addInputState(coffeeBatchStateStateAndRef.get(index))
                    .addOutputState(newCoffeeBatch)
                    .addCommand(new CoffeeBatchContract.Commands.MakeRoastedCoffee(),
                            Arrays.asList(this.getOurIdentity().getOwningKey(), this.customer.getOwningKey(), coffeeBatchState.getSeller().getOwningKey()));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession session = initiateFlow(this.customer);
            FlowSession sessionFarmer = initiateFlow(coffeeBatchState.getSeller());

            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx, Arrays.asList(session, sessionFarmer)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(session, sessionFarmer)));
        }
    }

    @InitiatedBy(MakeRoastedCoffeeFlowInitiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {
        private FlowSession otherPartySession;

        public Responder(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(otherPartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {

                }
            });

            return subFlow(new ReceiveFinalityFlow(otherPartySession, signedTransaction.getId()));
        }
    }
}