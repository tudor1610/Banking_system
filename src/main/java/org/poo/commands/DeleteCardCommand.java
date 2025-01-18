package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.card.Card;
import org.poo.transactions.Transaction;

public class DeleteCardCommand implements Command {
    private Bank bank;
    private String cardNumber;
    private int timestamp;

    public DeleteCardCommand(final Bank bank, final String cardNumber, final int timestamp) {
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.timestamp = timestamp;
    }

    /**
     * Deletes the specified card and logs a corresponding transaction.
     *
     */
    @Override
    public void execute() {
        Card card = bank.getCardHashMap().get(cardNumber);
        if (card != null) {
            Account account = bank.getAccountHashMap().get(card.getAccountIban());
            account.removeCard(card);
            bank.getCardHashMap().remove(cardNumber);
            Transaction t = new Transaction.Builder(timestamp, "The card has been destroyed")
                    .accountNumber(account.getIban())
                    .cardNumber(cardNumber)
                    .cardHolder(account.getEmail())
                    .build();
            User user = bank.getUserHashMap().get(account.getEmail());
            user.addTransaction(t);
            account.accountAddTransaction(t);
        }
    }
}
