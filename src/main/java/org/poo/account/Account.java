package org.poo.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.card.Card;
import org.poo.transactions.Transaction;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public abstract class Account {
    private Bank bank;
    private String iban;
    private String email;
    private String currency;
    private double balance;
    private double minBalance;
    private String accountType;
    private ArrayList<Card> cards;
    private List<Transaction> transactionList;
    private ObjectMapper objectMapper;
    private boolean food;
    private boolean clothes;
    private boolean tech;
    private HashMap<String, Integer> nrOfTransactions;

    public Account(final Bank bank, final String iban, final String email,
                   final String currency, final String accountType) {
        this.iban = iban;
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        balance = 0;
        minBalance = 0;
        cards = new ArrayList<>();
        transactionList = new ArrayList<>();
        objectMapper = new ObjectMapper();
        food = false;
        clothes = false;
        tech = false;
        nrOfTransactions = new HashMap<>();
        this.bank = bank;
    }

    /**
     * Adds a card to the account.
     *
     * @param card the card to add
     */
    public void addCard(final Card card) {
        cards.add(card);
    }

    /**
     * Removes a card from the account.
     *
     * @param card the card to remove
     */
    public void removeCard(final Card card) {
        cards.remove(card);
    }

    /**
     * Adds a transaction to the account's transaction list.
     *
     * @param transaction the transaction to add
     */
    public void accountAddTransaction(final Transaction transaction) {
        transactionList.add(transaction);
    }

    /**
     * Handles a split payment for a user, including currency conversion if necessary,
     * and updates the account's transaction history.
     *
     * @param user             The user making the payment.
     * @param newCurrency      The currency in which the payment is being made.
     * @param amount           The total amount of the bill being split.
     * @param timestamp        The timestamp of the transaction.
     * @param sum              The user's share of the split payment in the current currency.
     * @param accounts         The list of accounts involved in the transaction.
     * @param convertedAmount  The user's share of the split payment in the new currency,
     *                        if conversion is required.
     */
    public void accountPayment(final User user, final String newCurrency, final double amount,
                               final int timestamp, final double sum,
                               final List<Double> amountForUsers, final List<String> accounts,
                               final double convertedAmount, final String splitPaymentType) {
        if (this.currency.equals(newCurrency)) {
                    withdraw(sum);
                    Transaction t = new Transaction.Builder(timestamp, "Split payment of ")
                            .currency(currency)
                            .amountForUsers(amountForUsers)
                            .accountsInvolved(accounts)
                            .splitPaymentType(splitPaymentType)
                            .totalBill(amount)
                            .error(null)
                            .build();
                    user.addTransaction(t);
                    accountAddTransaction(t);
                } else {
                    withdraw(convertedAmount);
                    Transaction t = new Transaction.Builder(timestamp, "Split payment of ")
                            .currency(newCurrency)
                            .amountForUsers(amountForUsers)
                            .accountsInvolved(accounts)
                            .splitPaymentType(splitPaymentType)
                            .totalBill(amount)
                            .error(null)
                            .build();
                    user.addTransaction(t);
                    accountAddTransaction(t);
                }
    }

    /**
     * Prints the account details and associated cards into the provided ArrayNode.
     *
     * @param accountArray the ArrayNode to which the account details will be added
     */
    public void print(final ArrayNode accountArray) {
        ObjectNode account = objectMapper.createObjectNode();
        account.put("IBAN", getIban());
        account.put("balance", getBalance());
        account.put("currency", getCurrency());
        account.put("type", getAccountType());
        ArrayNode cardArray = objectMapper.createArrayNode();
        for (Card card : getCards()) {
            card.print(cardArray);
        }
        account.set("cards", cardArray);
        accountArray.add(account);
    }

    /**
     * Generates a report of transactions within a specified time range.
     *
     * @param timestamp the current timestamp of the report
     * @param startTimestamp the start of the time range
     * @param endTimestamp the end of the time range
     * @return an ObjectNode containing the report details
     */
    public ObjectNode printTransactions(final int timestamp,
                                        final int startTimestamp, final int endTimestamp) {
        ObjectNode command = objectMapper.createObjectNode();
        command.put("command", "report");
        command.put("timestamp", timestamp);
        ObjectNode status = objectMapper.createObjectNode();
        status.put("IBAN", iban);
        status.put("balance", balance);
        status.put("currency", currency);
        ArrayNode transactionArray = objectMapper.createArrayNode();
        for (Transaction t : transactionList) {
            if (t.getTimestamp() >= startTimestamp && t.getTimestamp() <= endTimestamp) {
                ObjectNode node = objectMapper.createObjectNode();
                t.print(node);
                transactionArray.add(node);
            }
        }
        status.set("transactions", transactionArray);
        command.set("output", status);
        return command;
    }

    /**
     * Generates a spendings report of transactions by commerciants within a specified time range.
     *
     * @param timestamp the current timestamp of the report
     * @param startTimestamp the start of the time range
     * @param endTimestamp the end of the time range
     * @return an ObjectNode containing the spendings report details
     */
    public ObjectNode printSpendingsTransaction(final int timestamp,
                                                final int startTimestamp, final int endTimestamp) {
        ObjectNode command = objectMapper.createObjectNode();
        command.put("command", "spendingsReport");
        command.put("timestamp", timestamp);
        ObjectNode status = objectMapper.createObjectNode();
        status.put("IBAN", iban);
        status.put("balance", balance);
        status.put("currency", currency);
        ArrayNode transactionArray = objectMapper.createArrayNode();
        Map<String, Double> commerciants = new TreeMap<>();
        for (Transaction t : transactionList) {
            if (t.getTimestamp() >= startTimestamp && t.getTimestamp() <= endTimestamp) {
                if (t.getCommerciant() != null) {
                    if (commerciants.containsKey(t.getCommerciant())) {
                        double value = commerciants.get(t.getCommerciant());
                        value += t.getAmount();
                        commerciants.replace(t.getCommerciant(), value);
                    } else {
                        commerciants.put(t.getCommerciant(), t.getAmount());
                    }
                    ObjectNode node = objectMapper.createObjectNode();
                    t.print(node);
                    transactionArray.add(node);
                }
            }
        }
        ArrayNode firms = objectMapper.createArrayNode();
        for (Map.Entry<String, Double> entry : commerciants.entrySet()) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("commerciant", entry.getKey());
            node.put("total", entry.getValue());
            firms.add(node);
        }
        status.set("commerciants", firms);
        status.set("transactions", transactionArray);
        command.set("output", status);

        return command;
    }

    /**
     * Adds a transaction to the account's transaction list.
     *
     * @param commerciant the commerciant of the transaction
     */
    public void addNrOfTransaction(final String commerciant) {
        if (nrOfTransactions.containsKey(commerciant)) {
            int value = nrOfTransactions.get(commerciant);
            value++;
            nrOfTransactions.replace(commerciant, value);
        } else {
            nrOfTransactions.put(commerciant, 1);
        }
    }

    /**
     * Deposits a specified amount into the account.
     *
     * @param amount the amount to deposit
     */
    public void deposit(final double amount) {

        if (amount > 0) {
            balance += amount;
        }
    }
    /**
     * Deposits a specified amount into the account.
     *
     * @param amount the amount to deposit
     */
    public void deposit(final double amount, final String email1, final int timestamp) {
       return;
    }

    /**
     * Withdraws a specified amount from the account if sufficient funds are available.
     *
     * @param amount the amount to withdraw
     */
    public void withdraw(final double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        }
    }
    /**
     * Withdraws a specified amount from the account if sufficient funds are available.
     *
     * @param amount the amount to withdraw
     */
    public void withdraw(final double amount, final String email1,
                         final int timestamp, final double amountWithoutComission) {
        return;
    }



    /**
     * Sets a new interest rate for the account.
     *  Overridden by SavingsAccount
     * @param newRate the new interest rate
     */
    public void setInterestRate(final double newRate) {
        return;
    }

    /**
     * Adds interest to the account balance based on the current rate.
     * Overridden by SavingsAccount
     */
    public void addInterest() {
        return;
    }

    /**
     * Returns the interest rate of the account.
     *
     * @return the interest rate
     */
    public double getInterestRate() {
        return 0;
    }

    /**
     * Adds a manager to the account.
     *
     * @param manager the manager to add
     */
    public void addManager(final User manager) {
        return;
    }

    /**
     * Adds an employee to the account.
     *
     * @param employee the employee to add
     */
    public void addEmployee(final User employee) {
        return;
    }

    /**
     * Sets the owner of the account.
     *

     */
    public String getOwner() {
        return null;
    }

    /**
     * Sets the spending limit of the account.
     *
     * @param spendingLimit the spending limit
     */
    public void setSpendingLimit(final double spendingLimit) {
        return;
    }

    /**
     * Sets the deposit limit of the account.
     *
     * @param depositLimit the deposit limit
     */
    public void setDepositLimit(final double depositLimit) {
        return;
    }

    /**
     * Returns the spending limit of the account.
     *
     * @return the spending limit
     */
    public Double getSpendingLimit() {
        return 0.0;
    }

    /**
     * Returns the deposit limit of the account.
     *
     * @return the deposit limit
     */
    public Double getDepositLimit() {
        return 0.0;
    }

    /**
     * Returns the list of managers associated with the account.
     *
     * @return the list of managers
     */
    public List<Associate> getManagers() {
        return null;
    }

    /**
     * Returns the list of employees associated with the account.
     *
     * @return the list of employees
     */
    public List<Associate> getEmployees() {
        return null;
    }

    /**
     * Checks if the account is a business account.
     *
     * @return true if the account is a business account, false otherwise
     */
    public boolean isBusiness() {
        return false;
    }

    /**
     * Returns the total amount spent from the account.
     *
     * @return the total amount spent
     */
    public Double getTotalSpent() {
        return 0.0;
    }

    /**
     * Returns the total amount deposited into the account.
     *
     * @return the total amount deposited
     */
    public Double getTotalDeposited() {
        return 0.0;
    }

    /**
     * Adds a spending to the account's total.
     *
     * @param amount the amount of the spending
     */
    public void addCommerciantSpendings(final String commerciant,
                                        final double amount, final User user) {
        return;
    }

    /**
     * Returns the list of spendings by commerciant.
     *
     * @return the list of spendings by commerciant
     */
    public List<CommerciantSpendings> getCommerciantSpendings() {
        return null;
    }

    /**
     * Checks if the user is the owner of the account.
     *
     * @param user the user to check
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(final User user) {
        return false;
    }

    /**
     * Counts the number of transactions over a certain
     * threshold and upgrades the user's plan if necessary.
     *
     * @param amount the amount of the transaction
     * @param timestamp the timestamp of the transaction
     */
    public void countTransactions(final double amount, final int timestamp, final String mail) {
        Double newAmount = bank.convertCurrency(amount, currency,
                "RON", bank.prepareExchangeRates());
        User user = bank.getUserHashMap().get(mail);
        final int treshold = 300;
        final int nroftrans = 5;
        if (newAmount >= treshold && user.getPlan().equals("silver")) {
            user.setBigTransactions(user.getBigTransactions() + 1);
            if (user.getBigTransactions() == nroftrans && user.getPlan().equals("silver")) {
                user.setPlan("gold");
                Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                        .accountNumber(getIban())
                        .newPlanType("gold")
                        .build();
                user.addTransaction(t);
                accountAddTransaction(t);
            }
        }
    }
}
