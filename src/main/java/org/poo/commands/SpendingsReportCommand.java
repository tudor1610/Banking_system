package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;

public class SpendingsReportCommand implements Command {
    private Bank bank;
    private int startTimestamp;
    private int endTimestamp;
    private String accountNumber;
    private int timestamp;

    public SpendingsReportCommand(final Bank bank, final int startTimestamp,
                                  final int endTimestamp,
                                  final String accountNumber, final int timestamp) {
        this.bank = bank;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.accountNumber = accountNumber;
        this.timestamp = timestamp;
    }

    /**
     * Generates a report of the spending transactions for
     * a specific account within a given time range.
     * This report is only available for classic accounts.
     * For savings accounts, an error is returned.
     *
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(accountNumber);
        if (account != null && account.getAccountType().equals("classic")) {
            User user = bank.getUserHashMap().get(account.getEmail());
            bank.getOutput().add(account
                    .printSpendingsTransaction(timestamp, startTimestamp, endTimestamp));
        } else  if (account != null && account.getAccountType().equals("savings")) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "spendingsReport");
            command.put("timestamp", timestamp);
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("error", "This kind of report is not supported for a saving account");
            command.set("output", status);
            bank.getOutput().add(command);
        } else {
            bank.getOutput().add(bank.accountError(timestamp, "spendingsReport"));
        }
    }
}
