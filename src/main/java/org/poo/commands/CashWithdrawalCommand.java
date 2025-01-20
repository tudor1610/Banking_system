package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.card.Card;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

public class CashWithdrawalCommand implements Command{
    private Bank bank;
    private String cardNumber;
    private double amount;
    private String email;
    private String location;
    private int timestamp;

    public CashWithdrawalCommand(final Bank bank, final String cardNumber, final double amount, final String email, final String location, final int timestamp) {
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.email = email;
        this.location = location;
        this.timestamp = timestamp;
    }

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
        //System.out.println("Executing cash withdrawal command timestamp: " + timestamp);
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
        Account account = bank.getAccountHashMap().get(bank.getCardHashMap().get(cardNumber).getAccountIban());
        double convertedAmount = bank.convertCurrency(amount, "RON", account.getCurrency(), bank.prepareExchangeRates());
        convertedAmount = Utils.comision(bank.getUserHashMap().get(email), convertedAmount, bank, account.getCurrency());
        if (account.getBalance() >= convertedAmount) {
            account.withdraw(convertedAmount);
            System.out.println("amount: " + amount + " convertedAmount: " + convertedAmount);
            Transaction t = new Transaction.Builder(timestamp, "Cash withdrawal of " + amount)
                    .withdrawalAmount(amount).build();
            bank.getUserHashMap().get(email).addTransaction(t);
        } else {
            Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
            bank.getUserHashMap().get(email).addTransaction(t);
        }

    }

}
