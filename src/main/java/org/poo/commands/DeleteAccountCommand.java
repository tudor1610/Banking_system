package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.transactions.Transaction;

public class DeleteAccountCommand implements Command {
    private Bank bank;
    private int timestamp;
    private String email;
    private String iban;

    public DeleteAccountCommand(final Bank bank, final int timestamp,
                                final String email, final String iban) {
        this.bank = bank;
        this.timestamp = timestamp;
        this.email = email;
        this.iban = iban;
    }

    /**
     * Handles the case where an account cannot be deleted due to constraints,
     * such as a non-zero balance.
     *
     */
    private void cannotDeleteAccount() {
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "deleteAccount");
        ObjectNode status = bank.getObjectMapper().createObjectNode();
        status.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
        status.put("timestamp", timestamp);
        command.set("output", status);
        command.put("timestamp", timestamp);
        bank.getOutput().add(command);
    }

    /**
     * Deletes the specified account if it has no remaining balance.
     * Otherwise, logs a failure message.
     *
     */
    @Override
    public void execute() {
        User user = bank.getUserHashMap().get(email);
        Account account = bank.getAccountHashMap().get(iban);
        if (account != null) {
            if (account.getBalance() == 0) {
                user.removeAccount(account, bank.getOutput(), timestamp);
                bank.getAccountHashMap().remove(iban);
            } else {
                cannotDeleteAccount();
                Transaction t = new Transaction.Builder(timestamp,
                        "Account couldn't be deleted - there are funds remaining").build();
                user.addTransaction(t);
            }
        }
    }


}
