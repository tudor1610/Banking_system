package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents a transaction with various types (transfer, split bill, payment, or card transaction).
 * Supports mandatory fields and optional fields depending on the type of transaction.
 * Uses the Builder pattern to construct a transaction object.
 */
@Getter
public final class Transaction {
    // Mandatory fields
    private final int timestamp;
    private final String description;

    // Optional fields
    private String senderIban;
    private String receiverIban;
    private Double amount;
    private String currency;
    private String transferType;
    private List<String> accountsInvolved;
    private List<Double> amountForUsers;
    private Double totalBill;
    private String error;
    private String commerciant;
    private String accountNumber;
    private String cardNumber;
    private String cardHolder;
    private String newPlanType;
    private Double withdrawalAmount;
    private String splitPaymentType;
    private String classicAccountIBAN;
    private String savingsAccountIBAN;


    /**
     * Constructs a Transaction object using the Builder.
     *
     * @param builder the Builder used to construct this transaction
     */
    private Transaction(final Builder builder) {
        this.timestamp = builder.timestamp;
        this.description = builder.description;
        this.senderIban = builder.senderIban;
        this.receiverIban = builder.receiverIban;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.transferType = builder.transferType;
        this.accountsInvolved = builder.accountsInvolved;
        this.totalBill = builder.totalBill;
        this.error = builder.error;
        this.commerciant = builder.commerciant;
        this.accountNumber = builder.accountNumber;
        this.cardNumber = builder.cardNumber;
        this.cardHolder = builder.cardHolder;
        this.newPlanType = builder.newPlanType;
        this.withdrawalAmount = builder.withdrawalAmount;
        this.amountForUsers = builder.amountForUsers;
        this.splitPaymentType = builder.splitPaymentType;
        this.classicAccountIBAN = builder.classicAccountIBAN;
        this.savingsAccountIBAN = builder.savingsAccountIBAN;
    }

    /**
     * Prints the transaction details into a JSON object based on the type of transaction.
     *
     * @param output the ObjectNode where transaction details will be added
     */
    public void print(final ObjectNode output) {
        if (accountsInvolved != null) {
            printSplitBill(output);
        } else if (senderIban != null) {
            printTransfer(output);
        } else if (commerciant != null) {
            printPayment(output);
        } else if (newPlanType != null) {
            printUpgradePlan(output);
        } else if (accountNumber != null) {
            printCardTransaction(output);
        } else if (withdrawalAmount != null) {
            printWithdrawal(output);
        } else if (currency != null) {
            printInterest(output);
        } else if (classicAccountIBAN != null) {
            printSavingsWithdrawal(output);
        } else {
            output.put("timestamp", timestamp);
            output.put("description", description);
        }
    }

    /**
     * Prints the details of a split bill transaction into a JSON object.
     *
     * @param output the ObjectNode where details will be added
     */
    private void printSplitBill(final ObjectNode output) {
        output.put("timestamp", timestamp);
        if (totalBill * 100 % 10 == 0) {
            output.put("description", description + totalBill + "0 " + currency);
        } else {
            output.put("description", description + totalBill + " " + currency);
        }

        output.put("currency", currency);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode accountsArray = objectMapper.createArrayNode();
        for (String account : accountsInvolved) {
            accountsArray.add(account);
        }
        if (error != null) {
            output.put("error", error);
        }
        if (splitPaymentType.equals("equal")) {
            output.put("amount", amountForUsers.get(0));
        } else {
            ArrayNode amountsArray = objectMapper.createArrayNode();
            if (amountForUsers != null) {
                for (Double amountForUser : amountForUsers) {
                    amountsArray.add(amountForUser);
                }
                output.set("amountForUsers", amountsArray);
            }
        }
        output.set("involvedAccounts", accountsArray);
        output.put("splitPaymentType", splitPaymentType);
    }

    private void printSavingsWithdrawal(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("amount", amount);
        output.put("classicAccountIBAN", classicAccountIBAN);
        output.put("savingsAccountIBAN", savingsAccountIBAN);
    }

    private void printInterest(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("amount", amount);
        output.put("currency", currency);
    }

    private void printWithdrawal(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("amount", withdrawalAmount);
    }

    private void printUpgradePlan(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("newPlanType", newPlanType);
        output.put("accountIBAN", accountNumber);
    }

    /**
     * Prints the details of a transfer transaction into a JSON object.
     *
     * @param output the ObjectNode where details will be added
     */
    private void printTransfer(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("senderIBAN", senderIban);
        output.put("receiverIBAN", receiverIban);
        output.put("amount", amount + " " + currency);
        output.put("transferType", transferType);
    }

    /**
     * Prints the details of a payment transaction into a JSON object.
     *
     * @param output the ObjectNode where details will be added
     */
    private void printPayment(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("amount", amount);
        output.put("commerciant", commerciant);
    }

    /**
     * Prints the details of a card transaction into a JSON object.
     *
     * @param output the ObjectNode where details will be added
     */
    private void printCardTransaction(final ObjectNode output) {
        output.put("timestamp", timestamp);
        output.put("description", description);
        output.put("account", accountNumber);
        output.put("card", cardNumber);
        output.put("cardHolder", cardHolder);
    }

    /**
     * Builder class for constructing {@link Transaction} objects.
     */
    public static class Builder {
        // Mandatory fields
        private final int timestamp;
        private final String description;

        // Optional fields
        private String senderIban;
        private String receiverIban;
        private Double amount;
        private String currency;
        private String transferType;
        private List<String> accountsInvolved;
        public List<Double> amountForUsers;
        private Double totalBill;
        private String error;
        private String commerciant;
        private String accountNumber;
        private String cardNumber;
        private String cardHolder;
        private String newPlanType;
        private Double withdrawalAmount;
        private String splitPaymentType;
        private String classicAccountIBAN;
        private String savingsAccountIBAN;

        /**
         * Constructs a Builder with the mandatory fields.
         *
         * @param timestamp   the timestamp of the transaction
         * @param description the description of the transaction
         */
        public Builder(final int timestamp, final String description) {
            this.timestamp = timestamp;
            this.description = description;
        }

        /**
         * Sets the sender's IBAN for the transaction.
         *
         * @param setSenderIban the sender's IBAN
         * @return the current Builder instance for method chaining
         */
        public Builder senderIban(final String setSenderIban) {
            senderIban = setSenderIban;
            return this;
        }

        /**
         * Sets the receiver's IBAN for the transaction.
         *
         * @param setReceiverIban the receiver's IBAN
         * @return the current Builder instance for method chaining
         */
        public Builder receiverIban(final String setReceiverIban) {
            receiverIban = setReceiverIban;
            return this;
        }

        /**
         * Sets the transaction amount.
         *
         * @param setAmount the amount for the transaction
         * @return the current Builder instance for method chaining
         */
        public Builder amount(final double setAmount) {
            amount = setAmount;
            return this;
        }

        /**
         * Sets the currency for the transaction.
         *
         * @param setCurrency the currency of the transaction
         * @return the current Builder instance for method chaining
         */
        public Builder currency(final String setCurrency) {
            currency = setCurrency;
            return this;
        }

        /**
         * Sets the type of transfer for the transaction.
         *
         * @param setTransferType the type of transfer (e.g., internal, external)
         * @return the current Builder instance for method chaining
         */
        public Builder transferType(final String setTransferType) {
            transferType = setTransferType;
            return this;
        }



        /**
         * Sets the list of accounts involved in a split bill transaction.
         *
         * @param setAccountsInvolved the list of involved accounts
         * @return the current Builder instance for method chaining
         */
        public Builder accountsInvolved(final List<String> setAccountsInvolved) {
            accountsInvolved = setAccountsInvolved;
            return this;
        }

        /**
         * Sets the total bill amount for a split bill transaction.
         *
         * @param setTotalBill the total bill amount
         * @return the current Builder instance for method chaining
         */
        public Builder totalBill(final double setTotalBill) {
            totalBill = setTotalBill;
            return this;
        }

        /**
         * Sets an error message for the transaction, if any.
         *
         * @param setError the error message
         * @return the current Builder instance for method chaining
         */
        public Builder error(final String setError) {
            error = setError;
            return this;
        }

        /**
         * Sets the commerciant associated with the transaction.
         *
         * @param setCommerciant the commerciant's name
         * @return the current Builder instance for method chaining
         */
        public Builder commerciant(final String setCommerciant) {
            commerciant = setCommerciant;
            return this;
        }

        /**
         * Sets the account number for a card transaction.
         *
         * @param setAccountNumber the account number used
         * @return the current Builder instance for method chaining
         */
        public Builder accountNumber(final String setAccountNumber) {
            accountNumber = setAccountNumber;
            return this;
        }

        /**
         * Sets the card number used in the transaction.
         *
         * @param setCardNumber the card number used
         * @return the current Builder instance for method chaining
         */
        public Builder cardNumber(final String setCardNumber) {
            cardNumber = setCardNumber;
            return this;
        }

        /**
         * Sets the cardholder's name for the transaction.
         *
         * @param setCardHolder the cardholder's name
         * @return the current Builder instance for method chaining
         */
        public Builder cardHolder(final String setCardHolder) {
            cardHolder = setCardHolder;
            return this;
        }

        public Builder newPlanType(final String setNewPlanType) {
            newPlanType = setNewPlanType;
            return this;
        }

        public Builder withdrawalAmount(final double setWithdrawalAmount) {
            withdrawalAmount = setWithdrawalAmount;
            return this;
        }

        public Builder amountForUsers(final List<Double> setAmountForUsers) {
            amountForUsers = setAmountForUsers;
            return this;
        }
        public Builder splitPaymentType(final String setSplitPaymentType) {
            splitPaymentType = setSplitPaymentType;
            return this;
        }

        public Builder classicAccountIBAN(final String setClassicAccountIBAN) {
            classicAccountIBAN = setClassicAccountIBAN;
            return this;
        }

        public Builder savingsAccountIBAN(final String setSavingsAccountIBAN) {
            savingsAccountIBAN = setSavingsAccountIBAN;
            return this;
        }

        /**
         * Builds and returns a {@link Transaction} object.
         *
         * @return the constructed {@link Transaction} object
         */
        public Transaction build() {
            return new Transaction(this);
        }
    }
}
