package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.transactions.Transaction;

public class ChangeInterestCommand implements Command {
    private Bank bank;
    private String accountNumber;
    private double interestRate;
    private int timestamp;

    public ChangeInterestCommand(final Bank bank, final String accountNumber,
                                 final double interestRate, final int timestamp) {
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.interestRate = interestRate;
        this.timestamp = timestamp;
    }

    /**
     * Changes the interest rate for a savings account.
     * If the account is not a savings account, an error is returned.
     *
     */
    public void execute() {
        Account account = bank.getAccountHashMap().get(accountNumber);
        if (account.getAccountType().equals("savings")) {
            account.setInterestRate(interestRate);
            Transaction t = new Transaction.Builder(timestamp,
                    "Interest rate of the account changed to " + interestRate).build();
            bank.getUserHashMap().get(account.getEmail()).addTransaction(t);
            account.accountAddTransaction(t);
        } else {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "changeInterestRate");
            command.put("timestamp", timestamp);
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "This is not a savings account");
            status.put("timestamp", timestamp);
            command.set("output", status);
            bank.getOutput().add(command);
        }
    }
}
