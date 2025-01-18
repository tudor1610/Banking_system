package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;

public class AddFundsCommand implements Command {
    private Bank bank;
    private String iban;
    private double amount;

    public AddFundsCommand(final Bank bank, final String iban, final double amount) {
        this.bank = bank;
        this.iban = iban;
        this.amount = amount;
    }

    /**
     * Adds funds to the specified account.
     *
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(iban);
        if (account != null) {
            account.deposit(amount);
        }
    }
}
