package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class RoasterDetailsContract implements Contract {

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

    }

    public interface Commands extends CommandData {
        class RegisterRoaster implements Commands {
        }
    }
}
