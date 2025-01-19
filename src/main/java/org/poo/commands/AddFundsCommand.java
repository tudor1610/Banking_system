package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;

public class AddFundsCommand implements Command {
    private Bank bank;
    private String iban;
    private String email;
    private double amount;
    private int timestamp;

    public AddFundsCommand(final Bank bank, final String iban, final double amount, final int timestamp, final String email) {
        this.bank = bank;
        this.iban = iban;
        this.amount = amount;
        this.timestamp = timestamp;
        this.email = email;
    }

    /**
     * Adds funds to the specified account.
     *
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(iban);
        if (account != null) {
            if (account.isBusiness()) {
                account.deposit(amount, email, timestamp);
                return;
            }
            account.deposit(amount);
        }
    }
}
