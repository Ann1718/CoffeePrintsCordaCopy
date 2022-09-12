package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;



public class CustomerFeedbackContract implements Contract {
    public static final String ID = "com.template.contracts.CustomerFeedbackContract";

    @Override
    public void verify(LedgerTransaction tx) {

        final CommandData command = tx.getCommands().get(0).getValue();

        if (command instanceof Commands.CreateCustomerFeedback) {
            if(tx.getOutputStates().size() != 1)
                throw new IllegalArgumentException("One output is required!");

            if(tx.getInputStates().size() != 0)
                throw new IllegalArgumentException("No input must be consume!");

            if(tx.getCommand(0).getSigners().size() != 3)
                throw new IllegalArgumentException("All nodes must sign!");

        }

    }

    public interface Commands extends CommandData {
        class CreateCustomerFeedback implements CustomerFeedbackContract.Commands {
        }
    }
}