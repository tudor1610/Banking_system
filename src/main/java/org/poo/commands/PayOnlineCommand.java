package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.card.Card;
import org.poo.fileio.ExchangeInput;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

import java.util.Map;

public class PayOnlineCommand implements Command {
    private Bank bank;
    private String cardNumber;
    private double amount;
    private String currency;
    private String description;
    private String commerciant;
    private String email;
    private ExchangeInput[] exc;
    private int timestamp;

    public PayOnlineCommand(final Bank bank, final String cardNumber, final double amount,
                            final String currency, final String description,
                            final String commerciant, final String email,
                            final ExchangeInput[] exc,  final int timestamp) {
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.commerciant = commerciant;
        this.email = email;
        this.exc = exc;
        this.timestamp = timestamp;
    }

    /**
     * Handles the case where a specified card cannot be found.
     *
     */
    private void cannotFindCard() {
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "payOnline");
        ObjectNode status = bank.getObjectMapper().createObjectNode();
        status.put("description", "Card not found");
        status.put("timestamp", timestamp);
        command.set("output", status);
        command.put("timestamp", timestamp);
        bank.getOutput().add(command);
    }

    /**
     * Processes an online payment using the specified card, verifying
     * sufficient funds and updating the account and transaction history.
     *
     */
    @Override
    public void execute() {
        Card card = bank.getCardHashMap().get(cardNumber);
        if (card == null) {
            cannotFindCard();
            return;
        }
        Account account;
        account = bank.getAccountHashMap().get(card.getAccountIban());
        User user = bank.getUserHashMap().get(account.getEmail());
        if (card.getStatus().equals("frozen")) {
            Transaction t = new Transaction.Builder(timestamp, "The card is frozen").build();
            user.addTransaction(t);
            account.accountAddTransaction(t);
            return;
        }
        if (account.getCurrency().equals(currency) && Utils.comision(user, amount, bank, account.getCurrency()) <= account.getBalance()) {
            double oldAmount = amount;
            amount = Utils.comision(user, amount, bank, account.getCurrency());
            account.withdraw(amount);
            Utils.addCashback(bank, user, account, oldAmount, bank.getCommerciants().get(commerciant));
            Transaction t = new Transaction.Builder(timestamp, "Card payment")
                    .commerciant(commerciant)
                    .amount(oldAmount)
                    .build();
            user.addTransaction(t);
            account.accountAddTransaction(t);
            if (card.isOneTime()) {
                bank.getCardHashMap().remove(cardNumber);
                Transaction t1 = new Transaction.Builder(timestamp, "The card has been destroyed")
                        .accountNumber(account.getIban())
                        .cardNumber(cardNumber)
                        .cardHolder(account.getEmail())
                        .build();
                user.addTransaction(t1);
                for (Card c : account.getCards()) {
                    if (c.getCardNumber().equals(cardNumber)) {
                        c.setCardNumber(c.regenerateCardNumber());
                        bank.getCardHashMap().put(c.getCardNumber(), c);
                        Transaction t2 = new Transaction.Builder(timestamp, "New card created")
                                .accountNumber(account.getIban())
                                .cardNumber(c.getCardNumber())
                                .cardHolder(account.getEmail())
                                .build();
                        user.addTransaction(t2);
                        break;
                    }
                }
            }
        } else {

            Map<String, Map<String, Double>> exchangeRates = bank.prepareExchangeRates();
            Double convertedAmount = bank.convertCurrency(amount, currency,
                    account.getCurrency(), exchangeRates);
//            System.out.println("cand fac conversia " + convertedAmount);
            double oldAmount = convertedAmount;
            convertedAmount = Utils.comision(user, convertedAmount, bank, account.getCurrency());
//            System.out.println("cand fac comisionul " + convertedAmount);
            if (convertedAmount != null && account.getBalance() >= convertedAmount) {
//                System.out.println("in ocnt sunt " + account.getBalance());
                account.withdraw(convertedAmount);
//                System.out.println("timestamp: " + timestamp);
//                System.out.println("contul are plan :" + user.getPlan());
//                System.out.println("Contul " + account.getIban() + " are " + account.getBalance() + " dupa ce a cumparat de la " + commerciant);
//                System.out.println("A cheltuit " + amount + " " + currency + " iar din cont i am luat " + convertedAmount + " " + account.getCurrency());
                Utils.addCashback(bank, user, account, oldAmount, bank.getCommerciants().get(commerciant));
//                System.out.println("Contul " + account.getIban() + " are " + account.getBalance() + "dupa cea primit cashbank de 0.1%");
                Transaction t = new Transaction.Builder(timestamp, "Card payment")
                        .commerciant(commerciant)
                        .amount(oldAmount)
                        .build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
                if (card.isOneTime()) {
                    bank.getCardHashMap().remove(cardNumber);
                    Transaction t1 = new Transaction.Builder(timestamp,
                            "The card has been destroyed")
                            .accountNumber(account.getIban())
                            .cardNumber(cardNumber)
                            .cardHolder(account.getEmail())
                            .build();
                    user.addTransaction(t1);
                    for (Card c : account.getCards()) {
                        if (c.getCardNumber().equals(cardNumber)) {
                            c.setCardNumber(c.regenerateCardNumber());
                            bank.getCardHashMap().put(c.getCardNumber(), c);
                            Transaction t2 = new Transaction.Builder(timestamp, "New card created")
                                    .accountNumber(account.getIban())
                                    .cardNumber(c.getCardNumber())
                                    .cardHolder(account.getEmail())
                                    .build();
                            user.addTransaction(t2);
                            break;
                        }
                    }
                }
            } else {
                Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
            }
        }
    }
}
