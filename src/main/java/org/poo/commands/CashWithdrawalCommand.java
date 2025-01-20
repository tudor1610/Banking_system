package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.Associate;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.card.Card;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

public class CashWithdrawalCommand implements Command {
    private Bank bank;
    private String cardNumber;
    private double amount;
    private String email;
    private String location;
    private int timestamp;

    public CashWithdrawalCommand(final Bank bank, final String cardNumber, final double amount,
                                 final String email, final String location, final int timestamp) {
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.email = email;
        this.location = location;
        this.timestamp = timestamp;
    }

    /**
     * Withdraws cash from the specified account.
     */
    @Override
    public void execute() {
        if (bank.getUserHashMap().get(email) == null) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "cashWithdrawal");
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "User not found");
            status.put("timestamp", timestamp);
            command.set("output", status);
            command.put("timestamp", timestamp);
            bank.getOutput().add(command);
            return;
        }
        if (bank.getCardHashMap().get(cardNumber) == null) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "cashWithdrawal");
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "Card not found");
            status.put("timestamp", timestamp);
            command.set("output", status);
            command.put("timestamp", timestamp);
            bank.getOutput().add(command);
            return;
        }
        Account account = bank.getAccountHashMap().get(bank.getCardHashMap()
                .get(cardNumber).getAccountIban());
        User user = null;
        if (account.isBusiness()) {
            if (account.getOwner().equals(email)) {
                user = bank.getUserHashMap().get(email);
            }
            for (Associate a : account.getManagers()) {
                if (a.getEmail().equals(email)) {
                    user = bank.getUserHashMap().get(email);
                    break;
                }
            }
            for (Associate a : account.getEmployees()) {
                if (a.getEmail().equals(email)) {
                    user = bank.getUserHashMap().get(email);
                    break;
                }
            }
            if (user == null) {
                ObjectNode command = bank.getObjectMapper().createObjectNode();
                command.put("command", "cashWithdrawal");
                ObjectNode status = bank.getObjectMapper().createObjectNode();
                status.put("description", "Card not found");
                status.put("timestamp", timestamp);
                command.set("output", status);
                command.put("timestamp", timestamp);
                bank.getOutput().add(command);
                return;
            }
        }
        user = bank.getUserHashMap().get(email);
        Card card = bank.getCardHashMap().get(cardNumber);
        if (!account.getEmail().equals(email)) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "cashWithdrawal");
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "Card not found");
            status.put("timestamp", timestamp);
            command.set("output", status);
            command.put("timestamp", timestamp);
            bank.getOutput().add(command);
            return;
        }
        double convertedAmount = bank.convertCurrency(amount, "RON",
                account.getCurrency(), bank.prepareExchangeRates());
        double oldAmount = convertedAmount;
        convertedAmount = Utils.comision(bank.getUserHashMap().get(email),
                convertedAmount, bank, account.getCurrency());
        if (account.getBalance() >= convertedAmount) {
            if (account.isBusiness()) {
                account.withdraw(convertedAmount, account.getOwner(), timestamp, oldAmount);
            } else {
                account.withdraw(convertedAmount);
                Transaction t = new Transaction.Builder(timestamp, "Cash withdrawal of " + amount)
                        .withdrawalAmount(amount).build();
                bank.getUserHashMap().get(email).addTransaction(t);
            }
        } else {
            Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
            bank.getUserHashMap().get(email).addTransaction(t);
        }

    }

}
