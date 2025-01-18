package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;

public class ReportCommand implements Command {
    private Bank bank;
    private int startTimestamp;
    private int endTimestamp;
    private String accountNumber;
    private int timestamp;

    public ReportCommand(final Bank bank, final int startTimestamp, final int endTimestamp,
                         final String accountNumber, final int timestamp) {
        this.bank = bank;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.accountNumber = accountNumber;
        this.timestamp = timestamp;
    }

    /**
     * Generates a report of transactions for a specified account within a given time range.
     *
     */
    public void execute() {
        Account account = bank.getAccountHashMap().get(accountNumber);
        if (account != null) {
            User user = bank.getUserHashMap().get(account.getEmail());
            bank.getOutput().add(account.
                    printTransactions(timestamp, startTimestamp, endTimestamp));
        } else {
            bank.getOutput().add(bank.accountError(timestamp, "report"));
        }
    }
}
