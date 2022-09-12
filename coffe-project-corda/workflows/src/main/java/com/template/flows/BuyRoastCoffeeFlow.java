package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CoffeeBatchContract;
import com.template.dto.RoastedCoffee;
import com.template.states.CoffeeBatchState;
import com.template.states.FarmerDetailsState;
import com.template.states.RoastedCoffeeState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BuyRoastCoffeeFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class BuyRoastCoffeeFlowInitiator extends FlowLogic<SignedTransaction> {

        private UniqueIdentifier coffeeId;

        public BuyRoastCoffeeFlowInitiator(UniqueIdentifier coffeeId) {
            this.coffeeId = coffeeId;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            final Party customer = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Customer,L=Taguig,C=AU"));
            StateAndRef coffeeBatchStateStateAndRef = getServiceHub().getVaultService().queryBy(CoffeeBatchState.class).getStates().get(0);
//            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateStateAndRef = getServiceHub().getVaultService()
//                    .queryBy(CoffeeBatchState.class).getStates();
            List<StateAndRef<RoastedCoffeeState>> roastedCoffeeStateandRef = getServiceHub().getVaultService().queryBy(RoastedCoffeeState.class).getStates();

            RoastedCoffeeState newRoastedCoffee = null;
            int index = 0;
            for (int i=0; i<roastedCoffeeStateandRef.size(); i++) {
                if (roastedCoffeeStateandRef.get(i).getState().getData().getCoffeeId().equals(this.coffeeId)){
                   roastedCoffeeStateandRef.get(i).getState().getData().setSold(true);
                   roastedCoffeeStateandRef.get(i).getState().getData().setPurchaseDate(new Date());
                   roastedCoffeeStateandRef.get(i).getState().getData().setInvoiceId(new UniqueIdentifier());
                  newRoastedCoffee =  roastedCoffeeStateandRef.get(i).getState().getData();
                    index = i;
                }

            }

//            RoastedCoffeeState newRoastedCoffee = null;
//            for (StateAndRef<RoastedCoffeeState> roastedCoffee : roastedCoffeeStateandRef) {
//                if (roastedCoffee.getState().getData().getCoffeeId().equals(this.coffeeId)){
//                    roastedCoffee.getState().getData().setSold(true);
//                    roastedCoffee.getState().getData().setPurchaseDate(new Date());
//                    roastedCoffee.getState().getData().setInvoiceId(new UniqueIdentifier());
//
//                    newRoastedCoffee = roastedCoffee.getState().getData();
//                }
//            }

            CoffeeBatchState coffeeBatchState = (CoffeeBatchState) coffeeBatchStateStateAndRef.getState().getData();
            if (!this.getOurIdentity().getOwningKey().equals(customer.getOwningKey())){
                throw new IllegalArgumentException("Only customer can buy roast coffee");
            }
            TransactionBuilder txBuilder = new TransactionBuilder(coffeeBatchStateStateAndRef.getState().getNotary())
//                    .addInputState(coffeeBatchStateStateAndRef)
                    .addInputState(roastedCoffeeStateandRef.get(index))
//                    .addOutputState(coffeeBatchState)
                    .addOutputState(newRoastedCoffee)
                    .addCommand(new CoffeeBatchContract.Commands.CreateCoffeeBatch(),
                            Arrays.asList(this.getOurIdentity().getOwningKey(), coffeeBatchState.getSeller().getOwningKey()));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession session = initiateFlow(coffeeBatchState.getSeller());

            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx, Arrays.asList(session)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(session)));
        }
    }

    @InitiatedBy(BuyRoastCoffeeFlowInitiator.class)
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
