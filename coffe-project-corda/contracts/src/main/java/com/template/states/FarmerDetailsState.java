package com.template.states;

import com.template.contracts.FarmerDetailsContract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@BelongsToContract(FarmerDetailsContract.class)
public class FarmerDetailsState implements LinearState {

    private String name;
    private String location;
    private int yearsInFarming;
    private int farmSize;
    private int elevation;
    private UniqueIdentifier farmerId;
    private String email;
    private String password;
    private AnonymousParty issuer;

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.issuer);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.farmerId;
    }

}
