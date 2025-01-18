package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.card.Card;
import org.poo.card.SingleUseCard;

public class CreateSingleCardCommand implements Command {
    private Bank bank;
    private String iban;
    private String email;
    private int timestamp;

    public CreateSingleCardCommand(final Bank bank, final String iban,
                                   final String email, final int timestamp) {
        this.bank = bank;
        this.iban = iban;
        this.email = email;
        this.timestamp = timestamp;
    }

    /**
     * Creates a single-use card linked to the specified account and user.
     *
     */
    public void execute() {
        Account account = bank.getAccountHashMap().get(iban);
        if (account != null && account.getEmail().equals(email)) {
            Card card = new SingleUseCard(iban, email);
            account.addCard(card);
            bank.getCardHashMap().put(card.getCardNumber(), card);
            bank.addCardTransaction(account, timestamp, card.getCardNumber());
        }
    }
}
