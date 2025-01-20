package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.card.Card;
import org.poo.transactions.Transaction;

public class CheckCardStatusCommand implements Command {
    private Bank bank;
    private String cardNumber;
    private int timestamp;

    public CheckCardStatusCommand(final Bank bank, final String cardNumber, final int timestamp) {
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.timestamp = timestamp;
    }

    /**
     * Checks the status of a card. If the card is not found, it returns an error message.
     * If the card is found, it checks the associated account balance and updates the card's status
     * to either "frozen" or "warning" based on the balance.
     *
     */
    @Override
    public void execute() {
        Card card = bank.getCardHashMap().get(cardNumber);
        if (card == null) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "checkCardStatus");
            command.put("timestamp", timestamp);
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "Card not found");
            status.put("timestamp", timestamp);
            command.set("output", status);
            bank.getOutput().add(command);
        } else {
            Account account = bank.getAccountHashMap().get(card.getAccountIban());
            final int warningZone = 30;
            if (account.getBalance() <= account.getMinBalance()) {
                Transaction t = new Transaction.Builder(timestamp,
                        "You have reached the minimum amount of funds, the card will be frozen")
                        .build();
                card.setStatus("frozen");
                User user = bank.getUserHashMap().get(account.getEmail());
                user.addTransaction(t);
            }
        }
    }
}
