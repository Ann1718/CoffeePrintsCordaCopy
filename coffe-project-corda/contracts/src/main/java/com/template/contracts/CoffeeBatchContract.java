package com.template.contracts;

import com.template.states.CoffeeBatchState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;


public class CoffeeBatchContract implements Contract {

    public static final String ID = "com.template.contracts.CoffeeBatchContract";

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        /*CreateCoffeeBatch
        TODO:
         participants must be farmer node
        */

        /*SellCoffeeBatch
        TODO:
          participants must be farmer, roaster node
          sold quantity < total quantity
          price != zero or null
        */
        if (commandData instanceof Commands.SellCoffeeBatch) {

            CoffeeBatchState outputState = tx.outputsOfType(CoffeeBatchState.class).get(0);
            if (outputState.getCoffeeDetails().getSoldQuantity() > outputState.getCoffeeDetails().getTotalQuantity())
                throw new IllegalArgumentException("Total Quantity must be greater to Sold Quantity");

            if (outputState.getPrice() == 0) {
                throw new IllegalArgumentException("Price must not be less than or equal to zero");
            }


        /*MakeRoastedCoffee
        TODO:
         participants must be roaster, customer node
         if soldquantity = 1 then roastCoffeeQuantity must be
         equal or less than 10
        */
        }

        if (commandData instanceof Commands.MakeRoastedCoffee){

            CoffeeBatchState output = tx.outputsOfType(CoffeeBatchState.class).get(0);

            if (output.getRoastedCoffee().getRoastedCoffeeQuantity() > output.getCoffeeDetails().getSoldQuantity() * 5)
                throw new IllegalArgumentException("Conversion ratio is 1 : 5");
        }
    }

    public interface Commands extends CommandData {
        class CreateCoffeeBatch implements Commands {}
        class SellCoffeeBatch implements Commands {}
        class MakeRoastedCoffee implements Commands {}
    }
}