package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;

public class ChangeSpendingLimitCommand implements Command{
    private Bank bank;
    private String email;
    private String IBAN;
    private double newLimit;
    private int timestamp;

    public ChangeSpendingLimitCommand(final Bank bank, final String email, final String IBAN, final double newLimit, final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.IBAN = IBAN;
        this.newLimit = newLimit;
        this.timestamp = timestamp;
    }

    public void execute() {
        Account account = bank.getAccountHashMap().get(IBAN);
        if (account.isBusiness()) {
            if (account.getOwner().equals(email)) {
                account.setSpendingLimit(newLimit);
                // add transaction
            } else {
                // error + transaction
                ObjectNode command = bank.getObjectMapper().createObjectNode();
                command.put("command", "changeSpendingLimit");
                ObjectNode status = bank.getObjectMapper().createObjectNode();
                status.put("description", "You must be owner in order to change spending limit.");
                status.put("timestamp", timestamp);
                command.set("output", status);
                command.put("timestamp", timestamp);
                bank.getOutput().add(command);
            }
        }
    }
}
