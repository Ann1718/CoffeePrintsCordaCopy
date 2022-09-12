package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.template.contracts.FarmerDetailsContract;
import com.template.states.CoffeeBatchState;
import com.template.states.FarmerDetailsState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.checkerframework.checker.units.qual.A;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


public class RegisterFarmerFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class RegisterFarmerFlowInitiator extends FlowLogic<SignedTransaction> {

        private String name;
        private String location;
        private int yearsInFarming;
        private int farmSize;
        private int elevation;
        private String email;
        private String password;

        public RegisterFarmerFlowInitiator(String name, String location, int yearsInFarming, int farmSize,
                                           int elevation, String email, String password) {
            this.name = name;
            this.location = location;
            this.yearsInFarming = yearsInFarming;
            this.farmSize = farmSize;
            this.elevation = elevation;
            this.email = email;
            this.password = password;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            final Party roaster = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Roaster,L=Mandaluyong,C=US"));
            final Party customer = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Customer,L=Taguig,C=AU"));

            StateAndRef<AccountInfo> accountInfoStateAndRef = (StateAndRef<AccountInfo>) subFlow(new CreateAccount(this.name));

            subFlow(new ShareAccountInfo(accountInfoStateAndRef, Arrays.asList(roaster, customer)));

            AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);

            AccountInfo myAccount = accountService.accountInfo(this.name).get(0).getState().getData();

            PublicKey myKey = subFlow(new RequestKeyForAccount(myAccount)).getOwningKey();

            List<StateAndRef<FarmerDetailsState>> farmerDetailsStateStateAndRef = getServiceHub().getVaultService()
                    .queryBy(FarmerDetailsState.class).getStates();

//            Stream<StateAndRef<FarmerDetailsState>> farmerStateAndRef = farmerDetailsStateStateAndRef.stream().filter(stateAndRef -> {
//                FarmerDetailsState farmerDetails = stateAndRef.getState().getData();
//                return farmerDetails.getEmail().equals(email);
//            });

            for (StateAndRef<FarmerDetailsState> farmerDetails : farmerDetailsStateStateAndRef) {
                if (farmerDetails.getState().getData().getEmail().equals(this.email))
                    throw new IllegalArgumentException("Email is existing");
            }



            UniqueIdentifier farmerId = new UniqueIdentifier();

            FarmerDetailsState farmerDetailsState = new FarmerDetailsState(this.name, this.location, this.yearsInFarming, this.farmSize,
                    this.elevation, farmerId, this.email, this.password, new AnonymousParty(myKey));



            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(farmerDetailsState)
                    .addCommand(new FarmerDetailsContract.Commands.RegisterFarmer(),
                            Arrays.asList(myKey));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder, Arrays.asList(this.getOurIdentity().getOwningKey(), myKey));

            return subFlow(new FinalityFlow(signedTx, Collections.emptyList()));
        }
    }
}