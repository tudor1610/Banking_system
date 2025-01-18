package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;

public class AddInterestCommand implements Command {
    private Bank bank;
    private String accountNumber;
    private int timestamp;
    public AddInterestCommand(final Bank bank, final String accountNumber, final int timestamp) {
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.timestamp = timestamp;
    }

    /**
     * Adds the interest to a savings account. If the account is not a savings account,
     * an error is returned.
     *
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(accountNumber);
        if (account.getAccountType().equals("savings")) {
            account.addInterest();
        } else {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "addInterest");
            command.put("timestamp", timestamp);
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "This is not a savings account");
            status.put("timestamp", timestamp);
            command.set("output", status);
            bank.getOutput().add(command);
        }
    }
}
