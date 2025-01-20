package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.Associate;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.card.Card;
import org.poo.fileio.ExchangeInput;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

import java.sql.SQLOutput;
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
        Account account = bank.getAccountHashMap().get(card.getAccountIban());
        User user = null;
        if (account.isBusiness()) {
            if (account.getOwner().equals(email)) {
                user = bank.getUserHashMap().get(email);
            }
            for (Associate associate : account.getManagers()) {
                if (associate.getEmail().equals(email)) {
                    user = bank.getUserHashMap().get(email);
                    break;
                }
            }

            for (Associate associate : account.getEmployees()) {
                if (associate.getEmail().equals(email)) {
                    user = bank.getUserHashMap().get(email);
                    break;
                }
            }
            if (user == null) {
                cannotFindCard();
                return;
            }
        }

        user = bank.getUserHashMap().get(email);

        if (card.getStatus().equals("frozen")) {
            Transaction t = new Transaction.Builder(timestamp, "The card is frozen").build();
            user.addTransaction(t);
            account.accountAddTransaction(t);
            return;
        }
        if (account.getCurrency().equals(currency) && Utils.comision(user, amount, bank, account.getCurrency()) <= account.getBalance() && amount !=0) {
            if (account.isBusiness()) {
                double oldAmount = amount;
                amount = Utils.comision(bank.getUserHashMap().get(account.getOwner()), amount, bank, account.getCurrency());
                bank.getUserHashMap().get(account.getOwner()).addCommercialTransaction(bank, commerciant, oldAmount, account.getCurrency(), timestamp, account);
                account.withdraw(amount, email, timestamp, oldAmount);
                account.addCommerciantSpendings(commerciant, oldAmount,user);
                account.countTransactions(oldAmount, timestamp);
                Utils.addCashback(bank, bank.getUserHashMap().get(account.getOwner()), account, oldAmount, bank.getCommerciants().get(commerciant));
            } else  {
                double oldAmount = amount;
                amount = Utils.comision(user, amount, bank, account.getCurrency());
                account.withdraw(amount);
                user.addCommercialTransaction(bank, commerciant, oldAmount, account.getCurrency(), timestamp, account);
                Utils.addCashback(bank, user, account, oldAmount, bank.getCommerciants().get(commerciant));
                Transaction t = new Transaction.Builder(timestamp, "Card payment")
                        .commerciant(commerciant)
                        .amount(oldAmount)
                        .build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
                account.countTransactions(oldAmount, timestamp);
            }
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
        } else  if (amount != 0){
            Map<String, Map<String, Double>> exchangeRates = bank.prepareExchangeRates();
            Double convertedAmount = bank.convertCurrency(amount, currency,
                    account.getCurrency(), exchangeRates);
            double oldAmount = convertedAmount;
            if (account.isBusiness()) {
                convertedAmount = Utils.comision(bank.getUserHashMap().get(account.getOwner()), convertedAmount, bank, account.getCurrency());
            } else {
                convertedAmount = Utils.comision(user, convertedAmount, bank, account.getCurrency());
            }
            if (convertedAmount != null && account.getBalance() >= convertedAmount) {
                if (account.isBusiness()) {
                    bank.getUserHashMap().get(account.getOwner()).addCommercialTransaction(bank, commerciant, oldAmount, account.getCurrency(), timestamp, account);
                    account.withdraw(convertedAmount, email, timestamp, oldAmount);
                    account.addCommerciantSpendings(commerciant, oldAmount, user);
                    account.countTransactions(convertedAmount, timestamp);
                    Utils.addCashback(bank, bank.getUserHashMap().get(account.getOwner()), account, oldAmount, bank.getCommerciants().get(commerciant));
                } else {

                    account.withdraw(convertedAmount);
                    user.addCommercialTransaction(bank, commerciant, oldAmount, account.getCurrency(), timestamp, account);
                    Utils.addCashback(bank, user, account, oldAmount, bank.getCommerciants().get(commerciant));
                    Transaction t = new Transaction.Builder(timestamp, "Card payment")
                            .commerciant(commerciant)
                            .amount(oldAmount)
                            .build();
                    user.addTransaction(t);
                    account.accountAddTransaction(t);
                    account.countTransactions(convertedAmount, timestamp);
                }
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
                System.out.println("Insufficient funds timestamp: " + timestamp);
                Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
            }
        }
    }
}
