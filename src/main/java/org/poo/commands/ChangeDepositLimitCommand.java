package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;

public class ChangeDepositLimitCommand implements Command {
    private Bank bank;
    private String IBAN;
    private String email;
    private double newLimit;
    private int timestamp;

    public ChangeDepositLimitCommand(final Bank bank, final String IBAN, final String email, final double newLimit, final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.newLimit = newLimit;
        this.timestamp = timestamp;
        this.IBAN = IBAN;
    }

    public void execute() {
        Account account = bank.getAccountHashMap().get(IBAN);
        if (account.isBusiness()) {
            if (account.getOwner().equals(email)) {
                System.out.println("timestamp: " + timestamp);
                System.out.println("old limit: " + account.getDepositLimit());
                System.out.println("new limit: " + newLimit);
                account.setDepositLimit(newLimit);
                // add transaction
            } else {
                // error + transaction
                System.out.println("Error: You are not the owner of this account.");
            }
        }
    }
}
