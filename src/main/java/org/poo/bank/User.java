package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.fileio.UserInput;
import org.poo.transactions.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a user in the banking system.
 *
 * <p>The {@code User} class contains personal details of the user, a list of
 * accounts and transactions associated with the user, and provides methods to
 * manage accounts, transactions, and generate user-specific data outputs.</p>
 */
@Setter
@Getter
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String birthDate;
    private String occupation;
    private List<Account> accounts;
    private List<Transaction> transactions;
    private ObjectMapper objectMapper;
    private String plan;
    private Map<String, Double> commercialTransactions;
    private int bigTransactions;

    /**
     * Constructs a new {@code User} object based on the given {@link UserInput}.
     *
     * @param userInput the input object containing user details
     */
    public User(final UserInput userInput) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
        birthDate = userInput.getBirthDate();
        occupation = userInput.getOccupation();
        accounts = new ArrayList<>();
        objectMapper = new ObjectMapper();
        transactions = new ArrayList<>();
        if (occupation.equals("student")) {
            plan = "student";
        } else {
            plan = "standard";
        }
        commercialTransactions = new HashMap<>();
        bigTransactions = 0;
    }

    /**
     * Adds a commercial transaction to the user's list of commercial transactions.
     *
     * @param bank the {@link Bank} object
     * @param commerciant the name of the commerciant
     * @param amount the amount of the transaction
     * @param currency the currency of the transaction
     * @param timestamp the timestamp of the transaction
     * @param account the {@link Account} object
     */
    public void addCommercialTransaction(final Bank bank, final String commerciant,
                                         final double amount, final String currency,
                                         final int timestamp, final Account account) {
        double newAmount = bank.convertCurrency(amount, currency, "RON",
                bank.prepareExchangeRates());
        if (commercialTransactions.containsKey(commerciant)) {
            commercialTransactions.put(commerciant,
                    commercialTransactions.get(commerciant) + newAmount);
        } else {
            commercialTransactions.put(commerciant, newAmount);
        }
    }

    /**
     * Adds an account to the user's list of accounts.
     *
     * @param account the {@link Account} to add
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }

    /**
     * Adds a transaction to the user's transaction history.
     *
     * @param t the {@link Transaction} to add
     */
    public void addTransaction(final Transaction t) {
        transactions.add(t);
    }

    /**
     * Removes an account from the user's list of accounts.
     *
     * <p>The method also adds a command to the output {@link ArrayNode} to log
     * the account deletion operation.</p>
     *
     * @param account the {@link Account} to remove
     * @param output the {@link ArrayNode} to which the operation result will be logged
     * @param timestamp the timestamp of the operation
     */
    public void removeAccount(final Account account, final ArrayNode output, final int timestamp) {
        ObjectNode command = objectMapper.createObjectNode();
        command.put("command", "deleteAccount");
        ObjectNode status = objectMapper.createObjectNode();
        status.put("success", "Account deleted");
        status.put("timestamp", timestamp);
        command.set("output", status);
        command.put("timestamp", timestamp);
        output.add(command);
        accounts.remove(account);
    }

    /**
     * Prints the user's details, including accounts, into the provided {@link ArrayNode}.
     *
     * <p>This method serializes the user's information, including their accounts,
     * into a JSON format using Jackson and appends it to the output array.</p>
     *
     * @param output the {@link ArrayNode} to which the user details will be added
     */
    public void print(final ArrayNode output) {
        ObjectNode userNode = objectMapper.createObjectNode();
        userNode.put("firstName", firstName);
        userNode.put("lastName", lastName);
        userNode.put("email", email);
        ArrayNode accountNode = objectMapper.createArrayNode();
        for (Account account : accounts) {
            account.print(accountNode);
        }
        userNode.set("accounts", accountNode);
        output.add(userNode);
    }


}
