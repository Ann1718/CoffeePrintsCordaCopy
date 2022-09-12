package com.template.states;

import com.template.contracts.CustomerFeedbackContract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@BelongsToContract(CustomerFeedbackContract.class)
public class CustomerFeedbackState implements ContractState {

    private String remarks;
    private Date dateCreated;
    private Party issuer;
    private Party farmer;
    private Party roaster;

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(farmer, roaster, issuer);
    }

}
