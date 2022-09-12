
package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.template.contracts.FarmerDetailsContract;
import com.template.contracts.RoasterDetailsContract;
import com.template.states.CustomerDetailsState;
import com.template.states.RoasterDetailsState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RegisterRoasterFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class RegisterRoasterFlowInitiator extends FlowLogic<SignedTransaction> {

        private String name;
        private String location;
        private String email;
        private String password;
        private String mobileNumber;

        public RegisterRoasterFlowInitiator(String name, String location, String email, String password, String mobileNumber) {
            this.name = name;
            this.location = location;
            this.email = email;
            this.password = password;
            this.mobileNumber = mobileNumber;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            final Party farmer = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Farmer,L=Rizal,C=PH"));
            final Party customer = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Customer,L=Taguig,C=AU"));

            StateAndRef<AccountInfo> accountInfoStateAndRef = (StateAndRef<AccountInfo>) subFlow(new CreateAccount(this.name));

            subFlow(new ShareAccountInfo(accountInfoStateAndRef, Arrays.asList(farmer, customer)));

            AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);

            AccountInfo myAccount = accountService.accountInfo(this.name).get(0).getState().getData();

            PublicKey myKey = subFlow(new RequestKeyForAccount(myAccount)).getOwningKey();

            List<StateAndRef<RoasterDetailsState>> roasterDetailsStateStateAndRef = getServiceHub().getVaultService().queryBy(RoasterDetailsState.class).getStates();

            for (StateAndRef<RoasterDetailsState> roasterDetails : roasterDetailsStateStateAndRef) {
                if (roasterDetails.getState().getData().getEmail().equals(this.email))
                    throw new IllegalArgumentException("Email is existing");
            }

            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            UniqueIdentifier roasterId = new UniqueIdentifier();

            RoasterDetailsState roasterDetailsState = new RoasterDetailsState(this.name, this.location, this.email, this.password, this.mobileNumber,
                    roasterId, new AnonymousParty(myKey));

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(roasterDetailsState)
                    .addCommand(new RoasterDetailsContract.Commands.RegisterRoaster(),
                            Arrays.asList(myKey));

            txBuilder.verify(getServiceHub());

            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder, Arrays.asList(this.getOurIdentity().getOwningKey(), myKey));

            return subFlow(new FinalityFlow(signedTx, Collections.emptyList()));
        }
    }
}
