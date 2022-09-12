
package com.template.states;

import com.template.contracts.CustomerDetailsContract;
import com.template.contracts.FarmerDetailsContract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@BelongsToContract(FarmerDetailsContract.class)
public class CustomerDetailsState implements LinearState {

    private String name;
    private String location;
    private String email;
    private String password;
    private String mobileNumber;
    private int age;
    private Date birthday;
    private UniqueIdentifier customerId;
    private AnonymousParty issuer;

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.issuer);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.customerId;
    }

}
