package com.template.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.template.flows.*;
import com.template.model.APIResponse;
import com.template.model.Forms;
import com.template.states.*;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;


@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class Controller {
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    public String toDisplayString(X500Name name) {
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo) {
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo) {
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

    @GetMapping(value = "/notaries", produces = APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> notaries() {
        try {
            HashMap<String, String> myMap = new HashMap<>();
            myMap.put("notary", proxy.notaryIdentities().toString());
            return new ResponseEntity<>(APIResponse.success(myMap), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/myIdentity", produces = APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> myIdentity() {

        try {
            HashMap<String, String> myMap = new HashMap<>();
            myMap.put("myIdentity", me.toString());
            return new ResponseEntity<>(APIResponse.success(myMap), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register-farmer")
    public ResponseEntity<Object> registerFarmer(@RequestBody Forms.FarmerForm farmerForm) {
        try {
            SignedTransaction result = proxy.startTrackedFlowDynamic(RegisterFarmerFlow.RegisterFarmerFlowInitiator.class, farmerForm.getName(),
                    farmerForm.getLocation(), farmerForm.getYearsInFarming(), farmerForm.getFarmSize(), farmerForm.getElevation(),
                    farmerForm.getEmail(), farmerForm.getPassword()).getReturnValue().get();

            logger.info("end: calling /register-farmer");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/register-roaster")
    public ResponseEntity<Object> registerRoaster(@RequestBody Forms.RoasterForm roasterForm) {
        try {
            SignedTransaction result = proxy.startTrackedFlowDynamic(RegisterRoasterFlow.RegisterRoasterFlowInitiator.class, roasterForm.getName(),
                    roasterForm.getLocation(),roasterForm.getEmail(),roasterForm.getPassword(),roasterForm.getMobileNumber()).getReturnValue().get();

            logger.info("end: calling /register-roaster");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()),HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/register-customer")
    public ResponseEntity<Object> registerCustomer(@RequestBody Forms.CustomerForm customerForm) {
        try {

            SignedTransaction result = proxy.startTrackedFlowDynamic(RegisterCustomerFlow.RegisterCustomerFlowInitiator.class, customerForm.getName(),
                    customerForm.getLocation(),customerForm.getEmail(),customerForm.getPassword(),customerForm.getMobileNumber(),
                    customerForm.getAge(),customerForm.getBirthday()).getReturnValue().get();

            logger.info("end: calling /register-customer");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()),HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/create-coffeebatch")
    public ResponseEntity<Object> addCoffeeBatch(@RequestBody Forms.CoffeeBatchForm coffeeBatchForm) {
        try {
            SignedTransaction result = proxy.startTrackedFlowDynamic(AddCoffeeBatchFlow.AddCoffeeBatchFlowInitiator.class, coffeeBatchForm.getType(),
                    coffeeBatchForm.getVarietal(), coffeeBatchForm.getTemperature(), coffeeBatchForm.getShadedCover(), coffeeBatchForm.getProcess(),
                    coffeeBatchForm.getTradedGood(), coffeeBatchForm.isSorted(), coffeeBatchForm.getUnit(), coffeeBatchForm.getTotalQuantity(),
                    coffeeBatchForm.getHarvestedDate()).getReturnValue().get();

            logger.info("end: calling /create-coffeebatch");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/sell-coffeebatch")
    public ResponseEntity<Object> sellCoffeeBatch(@RequestBody Forms.SellCoffeeBatchForm sellCoffeeBatchForm) {
        try {

            if (sellCoffeeBatchForm.getBuyer().toLowerCase().contains("roaster")) {
                sellCoffeeBatchForm.setBuyer("O=Roaster,L=Mandaluyong,C=US");
            }
            String party = sellCoffeeBatchForm.getBuyer();
            CordaX500Name partyX500Name = CordaX500Name.parse(party);
            Party buyer = proxy.wellKnownPartyFromX500Name(partyX500Name);

            UniqueIdentifier batchId = new UniqueIdentifier(null, UUID.fromString(sellCoffeeBatchForm.getBatchId()));

            SignedTransaction result = proxy.startTrackedFlowDynamic(SellCoffeeBatchFlow.SellCoffeeBatchFlowInitiator.class, sellCoffeeBatchForm.getQuantityToSell(),
                    buyer, sellCoffeeBatchForm.getPrice(), batchId).getReturnValue().get();

            logger.info("end: calling /sell-coffeebatch");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/make-roastedcoffee")
    public ResponseEntity<Object> makeRoastedCoffee(@RequestBody Forms.MakeRoastedCoffeeForm makeRoastedCoffeeForm) {
        try {

            if (makeRoastedCoffeeForm.getCustomer().toLowerCase().contains("customer")) {
                makeRoastedCoffeeForm.setCustomer("O=Customer,L=Taguig,C=AU");
            }

            String party = makeRoastedCoffeeForm.getCustomer();
            CordaX500Name partyX500Name = CordaX500Name.parse(party);
            Party customer = proxy.wellKnownPartyFromX500Name(partyX500Name);

            UUID transId = UUID.fromString(makeRoastedCoffeeForm.getTxId());

            SignedTransaction result = proxy.startTrackedFlowDynamic(MakeRoastedCoffeeFlow.MakeRoastedCoffeeFlowInitiator.class, makeRoastedCoffeeForm.getRoastCoffeeQuantity(),
                    makeRoastedCoffeeForm.getRoastType(), makeRoastedCoffeeForm.getGrade(),
                    makeRoastedCoffeeForm.getFlavor(),makeRoastedCoffeeForm.getPrice(), customer, makeRoastedCoffeeForm.getUnit(),transId).getReturnValue().get();

            logger.info("end: calling /make-roastedcoffee");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/buy-roastedcoffee")
    public ResponseEntity<Object> buyRoastedCoffee(@RequestBody Forms.BuyRoastedCoffee buyRoastedCoffee) {
        try {
            UniqueIdentifier coffeeId = new UniqueIdentifier(null, UUID.fromString(buyRoastedCoffee.getCoffeeId()));

            SignedTransaction result = proxy.startTrackedFlowDynamic(BuyRoastCoffeeFlow.BuyRoastCoffeeFlowInitiator.class, coffeeId).getReturnValue().get();

            logger.info("end: calling /buy-roastedcoffee");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/customer-feedback")
    public ResponseEntity<Object> createCustomerFeedback(@RequestBody Forms.CustomerFeedbackForm customerFeedbackForm) {
        try {
            SignedTransaction result = proxy.startTrackedFlowDynamic(CreateCustomerFeedbackFlow.CreateCustomerFeedbackFlowInitiator.class,
                    customerFeedbackForm.getRemarks()).getReturnValue().get();

            logger.info("end: calling /customer-feedback");
            return new ResponseEntity<>(APIResponse.success("Transaction id: " + result.getId()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Forms.Login login) {
        try {
            FarmerDetailsState farmerDetailsState = proxy.vaultQuery(FarmerDetailsState.class).getStates().get(0).getState().getData();

            if (farmerDetailsState.getEmail().equals(login.getEmail()) && farmerDetailsState.getPassword().equals(login.getPassword())) {
                return new ResponseEntity<>(APIResponse.success(), HttpStatus.OK);
            }

            return new ResponseEntity<>(APIResponse.error(HttpStatus.BAD_REQUEST.toString()), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-farmerdetails")
    public ResponseEntity<Object> getFarmerDetails() {
        try {
            logger.info("start: calling /get-farmerdetails");
            List<StateAndRef<FarmerDetailsState>> farmerDetailsStateandRef = proxy.vaultQuery(FarmerDetailsState.class).getStates();

            List<FarmerDetailsState> farmerDetailsStates = new ArrayList<>();
            for (StateAndRef<FarmerDetailsState> farmerDetailsState : farmerDetailsStateandRef) {
                farmerDetailsStates.add(farmerDetailsState.getState().getData());
            }

            logger.info("end: calling /get-farmerdetails");
            return new ResponseEntity<>(APIResponse.success(farmerDetailsStates), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/my-purchase")
    public ResponseEntity<Object> myPurchase() {
        try {
//            HashMap<String, Object> response = new HashMap<>();
//            UUID generateUUID = UUID.fromString(Forms.SellCoffeeBatchForm.class );
            logger.info("start: calling /my-purchase");

//            QueryCriteria.VaultQueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria()
//                    .withStatus(Vault.StateStatus.ALL);

            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateandRef = proxy.vaultQuery( CoffeeBatchState.class).getStates();

            List<CoffeeBatchState> coffeeBatchStates = new ArrayList<>();
            for (StateAndRef<CoffeeBatchState> coffeeBatchState : coffeeBatchStateandRef) {
               coffeeBatchStates.add(coffeeBatchState.getState().getData());
            }

//            response.put("state", coffeeBatchStates);
//            response.put("transactionId", new UniqueIdentifier());

            logger.info("end: calling /my-purchase");
            return new ResponseEntity<>(APIResponse.success(coffeeBatchStates), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/get-roasterdetails")
    public ResponseEntity<Object> getRoasterDetails() {
        try {
            logger.info("start: calling /get-roasterdetails");
            List<StateAndRef<RoasterDetailsState>> roasterDetailsStateandRef = proxy.vaultQuery(RoasterDetailsState.class).getStates();
            List<RoasterDetailsState> roasterDetailsStates = new ArrayList<>();
            for (StateAndRef<RoasterDetailsState> roasterDetailsState : roasterDetailsStateandRef) {
                roasterDetailsStates.add(roasterDetailsState.getState().getData());
            }

            logger.info("end: calling /get-roasterdetails");
            return new ResponseEntity<>(APIResponse.success(roasterDetailsStates),HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-customerdetails")
    public ResponseEntity<Object> getCustomerDetails() {
        try {
            logger.info("start: calling /get-customerdetails");
            List<StateAndRef<CustomerDetailsState>> customerDetailsStateandRef = proxy.vaultQuery(CustomerDetailsState.class).getStates();
            List<CustomerDetailsState> customerDetailsStates = new ArrayList<>();
            for (StateAndRef<CustomerDetailsState> roasterDetailsState : customerDetailsStateandRef) {
                customerDetailsStates.add(roasterDetailsState.getState().getData());
            }

            logger.info("end: calling /get-customerdetails");
            return new ResponseEntity<>(APIResponse.success(customerDetailsStates),HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-coffeebatch")
    public ResponseEntity<Object> getCoffeeBatch() {
        try {
            logger.info("start: calling /get-coffeebatch");
            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateandRef = proxy.vaultQuery(CoffeeBatchState.class).getStates();

            List<CoffeeBatchState> coffeeBatchStates = new ArrayList<>();
            for (StateAndRef<CoffeeBatchState> coffeeBatchState : coffeeBatchStateandRef) {
                coffeeBatchStates.add(coffeeBatchState.getState().getData());
            }

            logger.info("end: calling /get-coffeebatch");
            return new ResponseEntity<>(APIResponse.success(coffeeBatchStates), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping("/get-coffeebatch")
//    public ResponseEntity<Object> getCoffeeBatch() {
//        try {
//            logger.info("start: calling /get-coffeebatch");
//            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateandRef = proxy.vaultQuery(CoffeeBatchState.class).getStates();
//
//            List<CoffeeBatchState> coffeeBatchStates = new ArrayList<>();
////            for (StateAndRef<CoffeeBatchState> coffeeBatchStateStateAndRefs : coffeeBatchStateandRef) {
//////            coffeeBatchStates.add(coffeeBatchStateStateAndRefs.getState().getData());
//            coffeeBatchStates.add(coffeeBatchStateandRef.get(coffeeBatchStateandRef.size() - 1).getState().getData());
//
//
//
//            logger.info("end: calling /get-coffeebatch");
//            return new ResponseEntity<>(APIResponse.success(coffeeBatchStates), HttpStatus.OK);
//
//        } catch (Exception e) {
//            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//    }

    @GetMapping("/get-customerfeedback")
    public ResponseEntity<Object> getCustomerFeedback() {
        try {
            logger.info("start: calling /get-customerfeedback");
            List<StateAndRef<CustomerFeedbackState>> customerFeedbackStateandRef = proxy.vaultQuery(CustomerFeedbackState.class).getStates();

            List<CustomerFeedbackState> customerFeedbackStates = new ArrayList<>();
            for (StateAndRef<CustomerFeedbackState> customerFeedbackState : customerFeedbackStateandRef) {
                customerFeedbackStates.add(customerFeedbackState.getState().getData());
            }

            logger.info("end: calling /get-coffeebatch");
            return new ResponseEntity<>(APIResponse.success(customerFeedbackStates), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-roastedCoffee")
    public ResponseEntity<Object> getRoastedCoffee() {
        try {
            logger.info("start: calling /get-roastedCoffee");
//            QueryCriteria.VaultQueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria()
//                    .withStatus(Vault.StateStatus.UNCONSUMED);
            List<StateAndRef<RoastedCoffeeState>> roastedCoffeeStateAndRef = proxy.vaultQuery( RoastedCoffeeState.class).getStates();

            List<RoastedCoffeeState> roastedCoffeeStates = new ArrayList<>();
            for (StateAndRef<RoastedCoffeeState> roastedCoffeeState : roastedCoffeeStateAndRef) {
                if(roastedCoffeeState.getState().getData().isSold()){
                    roastedCoffeeState.getState().getData().setStatus("SOLD");
                }
                roastedCoffeeStates.add(roastedCoffeeState.getState().getData());
            }

            logger.info("end: calling /get-roastedCoffee");
            return new ResponseEntity<>(APIResponse.success(roastedCoffeeStates), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-accounts")
    public ResponseEntity<Object> getAccounts() {
        try {
            logger.info("start: calling /get-accounts");
            List<StateAndRef<AccountInfo>> accountInfoStateAndRef = proxy.vaultQuery(AccountInfo.class).getStates();

//            String party = "O=Farmer,L=Rizal,C=PH";
//            CordaX500Name partyX500Name = CordaX500Name.parse(party);
//            Party farmer = proxy.wellKnownPartyFromX500Name(partyX500Name);

            List<AccountInfo> accounts = new ArrayList<>();
            for (StateAndRef<AccountInfo> account : accountInfoStateAndRef) {
//                if (account.getState().getData().getHost().equals(farmer))
                accounts.add(account.getState().getData());
            }

            logger.info("end: calling /get-accounts");
            return new ResponseEntity<>(APIResponse.success(accounts), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/coffee-journey")
    public ResponseEntity<Object> coffeeJourney(@RequestParam(value = "coffeeId") String coffeeId) {
        try {
            HashMap<String, Object> response = new HashMap<>();

            UniqueIdentifier batchId = new UniqueIdentifier(null,UUID.fromString(coffeeId));

            List<StateAndRef<CoffeeBatchState>> coffeeBatchStateandRef = proxy.vaultQuery(CoffeeBatchState.class).getStates();

            List<StateAndRef<RoastedCoffeeState>> roastedCoffeeStateandRef = proxy.vaultQuery(RoastedCoffeeState.class).getStates();

            List<RoastedCoffeeState> roastedCoffeeStates = new ArrayList<>();
            for (StateAndRef<RoastedCoffeeState> roastedCoffee : roastedCoffeeStateandRef) {
                if (roastedCoffee.getState().getData().getCoffeeId().equals(batchId))
                    roastedCoffeeStates.add(roastedCoffee.getState().getData());
            }
                UniqueIdentifier coffeeBatch = roastedCoffeeStates.get(0).getBatchId();

                List<CoffeeBatchState> coffeeBatchStates = new ArrayList<>();
                for (StateAndRef<CoffeeBatchState> coffeeBatchState : coffeeBatchStateandRef) {
                    if (coffeeBatchState.getState().getData().getBatchId().equals(coffeeBatch))
                        coffeeBatchStates.add(coffeeBatchState.getState().getData());
                }

            response.put("state", coffeeBatchStates);
            response.put("roastedCoffee", roastedCoffeeStates);
            logger.info("end: calling /coffee-journey");
            return new ResponseEntity<>(APIResponse.success(response), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/customer-inventory")
    public ResponseEntity<Object> customerInventor() {
        try {

           // UniqueIdentifier batchId = new UniqueIdentifier(null,UUID.fromString(coffeeId));


            List<StateAndRef<RoastedCoffeeState>> roastedCoffeeStateandRef = proxy.vaultQuery(RoastedCoffeeState.class).getStates();

            List<RoastedCoffeeState> roastedCoffeeStates = new ArrayList<>();
            for (StateAndRef<RoastedCoffeeState> roastedCoffee : roastedCoffeeStateandRef) {
                if(roastedCoffee.getState().getData().isSold())
                roastedCoffeeStates.add(roastedCoffee.getState().getData());
            }

            logger.info("end: calling /coffee-journey");
            return new ResponseEntity<>(APIResponse.success(roastedCoffeeStates), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/farmer-accounts")
    public ResponseEntity<Object> getFarmerAccounts() {
        try {
            logger.info("start: calling /farmer-accounts");
            List<StateAndRef<AccountInfo>> accountInfoStateAndRef = proxy.vaultQuery(AccountInfo.class).getStates();

            String party = "O=Farmer,L=Rizal,C=PH";
            CordaX500Name partyX500Name = CordaX500Name.parse(party);
            Party farmer = proxy.wellKnownPartyFromX500Name(partyX500Name);

            List<AccountInfo> accounts = new ArrayList<>();
            for (StateAndRef<AccountInfo> account : accountInfoStateAndRef) {
                if (account.getState().getData().getHost().equals(farmer))
                    accounts.add(account.getState().getData());
            }

            logger.info("end: calling /farmer-accounts");
            return new ResponseEntity<>(APIResponse.success(accounts), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/roaster-accounts")
    public ResponseEntity<Object> getRoasterAccounts() {
        try {
            logger.info("start: calling /roaster-accounts");
            List<StateAndRef<AccountInfo>> accountInfoStateAndRef = proxy.vaultQuery(AccountInfo.class).getStates();

            String party = "O=Roaster, L=Mandaluyong, C=US";
            CordaX500Name partyX500Name = CordaX500Name.parse(party);
            Party roaster = proxy.wellKnownPartyFromX500Name(partyX500Name);

            List<AccountInfo> accounts = new ArrayList<>();

            for (StateAndRef<AccountInfo> account : accountInfoStateAndRef) {
                if (account.getState().getData().getHost().equals(roaster))
                    accounts.add(account.getState().getData());
            }

            logger.info("end: calling /roaster-accounts");
            return new ResponseEntity<>(APIResponse.success(accounts), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/customer-accounts")
    public ResponseEntity<Object> getCustomerAccounts() {
        try {
            logger.info("start: calling /customer-accounts");
            List<StateAndRef<AccountInfo>> accountInfoStateAndRef = proxy.vaultQuery(AccountInfo.class).getStates();
            String party = "O=Customer, L=Taguig, C=AU";
            CordaX500Name partyX500Name = CordaX500Name.parse(party);
            Party customer = proxy.wellKnownPartyFromX500Name(partyX500Name);

            List<AccountInfo> accounts = new ArrayList<>();

            for (StateAndRef<AccountInfo> account : accountInfoStateAndRef) {
                if (account.getState().getData().getHost().equals(customer))
                    accounts.add(account.getState().getData());
            }

            logger.info("end: calling /customer-accounts");
            return new ResponseEntity<>(APIResponse.success(accounts), HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(APIResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}