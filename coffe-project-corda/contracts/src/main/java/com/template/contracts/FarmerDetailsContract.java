package com.template.contracts;

import com.template.states.FarmerDetailsState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.LedgerTransaction;

public class FarmerDetailsContract implements Contract {

    public static final String ID = "com.template.contracts.FarmerDetailsContract";

    @Override
    public void verify(LedgerTransaction tx) {

        if (tx.getOutputStates().size() != 1)
            throw new IllegalArgumentException("One Output Expected");

        if (tx.getCommands().size() != 1)
            throw new IllegalArgumentException("One Command is Expected");

    }

    public interface Commands extends CommandData {
        class RegisterFarmer implements FarmerDetailsContract.Commands {
        }
        class RegisterRoaster implements FarmerDetailsContract.Commands {
        }
        class RegisterCustomer implements FarmerDetailsContract.Commands {
        }
    }
}