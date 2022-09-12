package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CoffeeBatchContract;
import com.template.states.CoffeeBatchState;
import com.template.states.RoastedCoffeeState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SellCoffeeBatchFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class SellCoffeeBatchFlowInitiator extends FlowLogic<SignedTransaction> {

        private int quantityToSell;
        private Party buyer;
        private int price;
        private UniqueIdentifier batchId;

        public SellCoffeeBatchFlowInitiator(int quantityToSell, Party buyer, int price, UniqueIdentifier batchId) {
            this.quantityToSell = quantityToSell;
            this.buyer = buyer;
            this.price = price;
            this.batchId = batchId;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateStateAndRef = getServiceHub().getVaultService()
                    .queryBy(CoffeeBatchState.class).getStates();


            CoffeeBatchState coffeeBatchState = null;
            int index = 0;
            for (int i=0; i<coffeeBatchStateStateAndRef.size(); i++) {
                if (coffeeBatchStateStateAndRef.get(i).getState().getData().getBatchId().equals(this.batchId)){
                    coffeeBatchState = coffeeBatchStateStateAndRef.get(i).getState().getData();
                    index = i;
                }
            }


            coffeeBatchState.setOwner(buyer);
            coffeeBatchState.getCoffeeDetails().setSoldQuantity(quantityToSell + coffeeBatchState.getCoffeeDetails().getSoldQuantity());
            coffeeBatchState.setPrice(price);
            coffeeBatchState.getCoffeeDetails().setTxId(UUID.randomUUID());

            TransactionBuilder txBuilder = new TransactionBuilder(coffeeBatchStateStateAndRef.get(0).getState().getNotary())
                    .addInputState(coffeeBatchStateStateAndRef.get(index))
                    .addOutputState(coffeeBatchState)
                    .addCommand(new CoffeeBatchContract.Commands.SellCoffeeBatch(),
                            Arrays.asList(this.getOurIdentity().getOwningKey(), this.buyer.getOwningKey()));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession session = initiateFlow(this.buyer);

            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx, Arrays.asList(session)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(session)));
        }
    }

    @InitiatedBy(SellCoffeeBatchFlowInitiator.class)
    public static class Responder extends FlowLogic<Void> {
        private FlowSession otherPartySession;

        public Responder(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(otherPartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {

                }
            });

            subFlow(new ReceiveFinalityFlow(otherPartySession, signedTransaction.getId()));
            return null;
        }
    }
}