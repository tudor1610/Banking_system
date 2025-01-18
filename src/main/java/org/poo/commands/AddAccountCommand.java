package org.poo.commands;

import org.poo.account.Account;
import org.poo.account.AccountFactory;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.transactions.Transaction;

public class AddAccountCommand implements Command {
    private Bank bank;
    private String email;
    private String currency;
    private String accountType;
    private double interestRate;
    private int timestamp;

    public AddAccountCommand(final Bank bank, final String email, final String currency,
                             final String accountType, final double interestRate,
                             final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.interestRate = interestRate;
        this.timestamp = timestamp;

    }

    /**
     * Adds a new account to a user, creates an associated transaction,
     * and updates the internal account mapping.
     */
    @Override
    public void execute() {
        User user = bank.getUserHashMap().get(email);
        AccountFactory accountFactory = AccountFactory.getAccountFactory();
        Account account = AccountFactory.createAccount(email, currency, accountType,
                timestamp, interestRate);
        user.addAccount(account);
        Transaction transaction = new Transaction.Builder(timestamp, "New account created")
                .build();
        user.addTransaction(transaction);
        account.accountAddTransaction(transaction);
        bank.getAccountHashMap().put(account.getIban(), account);
    }
}
