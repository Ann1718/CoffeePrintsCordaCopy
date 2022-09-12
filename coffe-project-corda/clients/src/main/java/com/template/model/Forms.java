
package com.template.model;

import lombok.Data;


import java.util.Date;

public class Forms {

    @Data
    public static class FarmerForm {

        private String name;
        private String location;
        private int yearsInFarming;
        private int farmSize;
        private int elevation;
        private String email;
        private String password;
    }

    @Data
    public static class RoasterForm {

        private String name;
        private String location;
        private String email;
        private String password;
        private String mobileNumber;
    }
    @Data
    public static class CustomerForm {

        private String name;
        private String location;
        private String email;
        private String password;
        private String mobileNumber;
        private int age;
        private Date birthday;
    }

    @Data
    public static class CoffeeBatchForm {
        private String type;
        private String varietal;
        private String temperature;
        private String shadedCover;
        private String process;
        private String tradedGood;
        private boolean isSorted;
        private String unit;
        private int totalQuantity;
        private Date harvestedDate;
    }

    @Data
    public static class SellCoffeeBatchForm {
        private int quantityToSell;
        private String buyer;
        private int price;
        private String batchId;
    }

    @Data
    public static class MakeRoastedCoffeeForm {
        private int roastCoffeeQuantity;
        private String roastType;
        private String grade;
        private String flavor;
        private int price;
        private String customer;
        private String unit;
        private String txId;
    }

    @Data
    public static class CustomerFeedbackForm {
        private String remarks;
    }

    @Data
    public static class BuyRoastedCoffee {
        private String coffeeId;
    }

    @Data
    public static class Login {
        private String email;
        private String password;
    }

    @Data
    public static class CoffeeJourney {
        private String coffeeId;
    }

    @Data
    public static class CustomerInventory {
        private String coffeeId;
    }
}
