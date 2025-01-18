package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.fileio.ExchangeInput;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

import java.util.Map;

public class SendMoneyCommand implements Command {
    private Bank bank;
    private String account1;
    private String account2;
    private double amount;
    private ExchangeInput[] exc;
    private String description;
    private int timestamp;

    public SendMoneyCommand(final Bank bank, final String account1, final String account2,
                            final double amount, final ExchangeInput[] exc,
                            final String description, final int timestamp) {
        this.bank = bank;
        this.account1 = account1;
        this.account2 = account2;
        this.amount = amount;
        this.exc = exc;
        this.description = description;
        this.timestamp = timestamp;
    }

    /**
     * Records a money transfer transaction between two accounts, including
     * details for both the sender and the receiver, and updates their transaction histories.
     *
     * @param sender         the account sending money
     * @param receiver       the account receiving money
     * @param amount2         the amount of money sent in the sender's currency
     * @param convertedAmount the equivalent amount received in the receiver's currency
     */
    private void sendMoneyTransaction(final Account sender, final Account receiver,
                                      final double amount2, final double convertedAmount) {
        Transaction transaction1 = new Transaction.Builder(timestamp, description)
                .senderIban(sender.getIban())
                .receiverIban(receiver.getIban())
                .amount(amount2)
                .currency(sender.getCurrency())
                .transferType("sent")
                .build();
        Transaction transaction2 = new Transaction.Builder(timestamp, description)
                .senderIban(sender.getIban())
                .receiverIban(receiver.getIban())
                .amount(convertedAmount)
                .currency(receiver.getCurrency())
                .transferType("received")
                .build();
        User user1 = bank.getUserHashMap().get(sender.getEmail());
        User user2 = bank.getUserHashMap().get(receiver.getEmail());
        user1.addTransaction(transaction1);
        sender.accountAddTransaction(transaction1);
        user2.addTransaction(transaction2);
        receiver.accountAddTransaction(transaction2);
    }

    /**
     * Transfers money between two accounts, handling currency conversion if needed.
     * If the sender lacks sufficient funds, logs a failure transaction.
     *
     */
    @Override
    public void execute() {
        Account sender = bank.getAccountHashMap().get(account1);
        if (sender == null) {
            return;
        }
        Account receiver = bank.getAccountHashMap().get(account2);
        if (receiver == null) {
            receiver = bank.getAliasHashMap().get(account2);
        }
        if (receiver != null) {
            double oldAmount = amount;
            amount = Utils.comision(bank.getUserHashMap().get(sender.getEmail()), amount, bank, sender.getCurrency());
            if (sender.getCurrency().equals(receiver.getCurrency())) {
//                System.out.println("timestamp " + timestamp + " amount " + amount + " oldAmount " + oldAmount);
//                System.out.println("Account sending " + sender.getIban() + " " + sender.getBalance() + " " + amount  +" currency " + sender.getCurrency());
//                System.out.println("Account receiving " + receiver.getIban() + " " + receiver.getBalance() + " " + oldAmount + " currency " + receiver.getCurrency());
                if (sender.getBalance() >= amount) {
                    sender.withdraw(amount);
                    receiver.deposit(oldAmount);
                    sendMoneyTransaction(sender, receiver, oldAmount, oldAmount);
                } else {
                    Transaction t = new Transaction.Builder(timestamp, "Insufficient funds")
                            .build();
                    User user = bank.getUserHashMap().get(sender.getEmail());
                    user.addTransaction(t);
                    sender.accountAddTransaction(t);
                }
            } else {
                Map<String, Map<String, Double>> exchangeRates = bank.prepareExchangeRates();
                Double convertedAmount = bank.convertCurrency(oldAmount, sender.getCurrency(),
                        receiver.getCurrency(), exchangeRates);
//                System.out.println("timestamp " + timestamp + " amount " + amount + " oldAmount " + oldAmount + " convertedAmount " + convertedAmount);
                if (convertedAmount != null && sender.getBalance() >= amount) {
                    sender.withdraw(amount);
                    receiver.deposit(convertedAmount);
                    sendMoneyTransaction(sender, receiver, oldAmount, convertedAmount);
                } else {
                    Transaction t = new Transaction.Builder(timestamp, "Insufficient funds")
                            .build();
                    User user = bank.getUserHashMap().get(sender.getEmail());
                    user.addTransaction(t);
                    sender.accountAddTransaction(t);
                }
            }
        }
    }
}
