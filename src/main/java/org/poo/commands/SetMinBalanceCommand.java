package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;

public class SetMinBalanceCommand implements Command {
    private Bank bank;
    private double minBalance;
    private String accountNumber;

    public SetMinBalanceCommand(final Bank bank, final double minBalance,
                                final String accountNumber) {
        this.bank = bank;
        this.minBalance = minBalance;
        this.accountNumber = accountNumber;
    }

    /***
     * Sets the minimum balance of an account; below that balance, the account freezes
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(accountNumber);
        account.setMinBalance(minBalance);
    }
}
