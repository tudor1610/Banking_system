package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;

public class ChangeDepositLimitCommand implements Command {
    private Bank bank;
    private String iban;
    private String email;
    private double newLimit;
    private int timestamp;

    public ChangeDepositLimitCommand(final Bank bank, final String iban, final String email,
                                     final double newLimit, final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.newLimit = newLimit;
        this.timestamp = timestamp;
        this.iban = iban;
    }

    /**
     * Change the deposit limit of a business account.
     */
    public void execute() {
        Account account = bank.getAccountHashMap().get(iban);
        if (account.isBusiness()) {
            if (account.getOwner().equals(email)) {
                account.setDepositLimit(newLimit);
                // add transaction
            } else {
                // error + transaction
                ObjectNode command = bank.getObjectMapper().createObjectNode();
                command.put("command", "changeDepositLimit");
                ObjectNode status = bank.getObjectMapper().createObjectNode();
                status.put("description", "You must be owner in order to change deposit limit.");
                status.put("timestamp", timestamp);
                command.set("output", status);
                command.put("timestamp", timestamp);
                bank.getOutput().add(command);
            }
        }
    }
}
