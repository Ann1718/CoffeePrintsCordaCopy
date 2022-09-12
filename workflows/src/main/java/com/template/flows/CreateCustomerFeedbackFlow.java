package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CustomerFeedbackContract;
import com.template.states.CustomerFeedbackState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;

public class CreateCustomerFeedbackFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class CreateCustomerFeedbackFlowInitiator extends FlowLogic<SignedTransaction> {

        private String remarks;

        public CreateCustomerFeedbackFlowInitiator(String remarks) {
            this.remarks = remarks;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            final Party farmer = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Farmer,L=Rizal,C=PH"));
            final Party roaster = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Roaster,L=Mandaluyong,C=US"));
            final Party customer = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Customer,L=Taguig,C=AU"));
            CustomerFeedbackState customerFeedbackState = new CustomerFeedbackState(remarks, new Date(),
                    this.getOurIdentity(), farmer, roaster);

            if (!this.getOurIdentity().getOwningKey().equals(customer.getOwningKey())) {
                throw new IllegalArgumentException("Only customer can give Feedback");
            }

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(customerFeedbackState)
                    .addCommand(new CustomerFeedbackContract.Commands.CreateCustomerFeedback(),
                            Arrays.asList(this.getOurIdentity().getOwningKey(), farmer.getOwningKey(), roaster.getOwningKey()));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession sessionFarmer = initiateFlow(farmer);
            FlowSession sessionRoaster = initiateFlow(roaster);

            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx, Arrays.asList(sessionFarmer, sessionRoaster)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(sessionFarmer, sessionRoaster)));
        }
    }

    @InitiatedBy(CreateCustomerFeedbackFlowInitiator.class)
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
