package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.fileio.CommerciantInput;
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
    private String email;
    private int timestamp;

    public SendMoneyCommand(final Bank bank, final String account1, final String account2,
                            final double amount, final ExchangeInput[] exc,
                            final String description, final String email, final int timestamp) {
        this.bank = bank;
        this.account1 = account1;
        this.account2 = account2;
        this.amount = amount;
        this.exc = exc;
        this.description = description;
        this.email = email;
        this.timestamp = timestamp;
    }

    /**
     * Records a money transfer transaction between two accounts, including
     * details for both the sender and the receiver, and updates their transaction histories.
     *
     * @param sender          the account sending money
     * @param receiver        the account receiving money
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

    private void sendMoneyTransaction2(final Account sender, final String receiver,
                                      final double amount2, final double convertedAmount) {
        Transaction transaction1 = new Transaction.Builder(timestamp, description)
                .senderIban(sender.getIban())
                .receiverIban(receiver)
                .amount(amount2)
                .currency(sender.getCurrency())
                .transferType("sent")
                .build();

        User user1 = bank.getUserHashMap().get(sender.getEmail());
        user1.addTransaction(transaction1);
        sender.accountAddTransaction(transaction1);
    }

    /**
     * Transfers money between two accounts, handling currency conversion if needed.
     * If the sender lacks sufficient funds, logs a failure transaction.
     */
    @Override
    public void execute() {
        if (bank.getUserHashMap().get(email) == null) {
            userNotFound();
            return;
        }
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
            if (sender.isBusiness()) {
                amount = Utils.comision(bank.getUserHashMap().get(sender.getOwner()),
                        amount, bank, sender.getCurrency());
            } else {
                amount = Utils.comision(bank.getUserHashMap().get(sender.getEmail()),
                        amount, bank, sender.getCurrency());
            }
            if (sender.getCurrency().equals(receiver.getCurrency())) {
                if (sender.getBalance() >= amount) {
                    if (sender.isBusiness()) {
                        double originalBalance = sender.getBalance();
                        sender.withdraw(amount, sender.getOwner(), timestamp, oldAmount);
                        double newBalance = sender.getBalance();
                        if (originalBalance != newBalance) {
                            sender.countTransactions(oldAmount, timestamp, email);
                        }
                    } else {
                        double originalBalance = sender.getBalance();
                        sender.withdraw(amount);
                        double newBalance = sender.getBalance();
                        if (originalBalance != newBalance) {
                            sender.countTransactions(oldAmount, timestamp, email);
                        }
                    }
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
                if (convertedAmount != null && sender.getBalance() >= amount) {
                    sender.withdraw(amount);
                    receiver.deposit(convertedAmount);
                    sendMoneyTransaction(sender, receiver, oldAmount, convertedAmount);
                    sender.countTransactions(convertedAmount, timestamp, email);
                } else {
                    Transaction t = new Transaction.Builder(timestamp, "Insufficient funds")
                            .build();
                    User user = bank.getUserHashMap().get(sender.getEmail());
                    user.addTransaction(t);
                    sender.accountAddTransaction(t);
                }
            }
        } else {
            double oldAmount = amount;
            if (sender.isBusiness()) {
                amount = Utils.comision(bank.getUserHashMap().get(sender.getOwner()),
                        amount, bank, sender.getCurrency());
            } else {
                amount = Utils.comision(bank.getUserHashMap().get(sender.getEmail()),
                        amount, bank, sender.getCurrency());
            }
            if (sender.getBalance() >= amount) {
                if (sender.isBusiness()) {
                    String commerciant = null;
                    for (Map.Entry<String, CommerciantInput> entry : bank
                            .getCommerciants().entrySet()) {
                        if (entry.getValue().getAccount().equals(account2)) {
                            commerciant = entry.getValue().getCommerciant();
                        }
                    }
                    if (commerciant == null) {
                        userNotFound();
                        return;
                    }
                    sender.withdraw(amount, sender.getOwner(), timestamp, oldAmount);
                    sender.addCommerciantSpendings(commerciant, oldAmount,
                            bank.getUserHashMap().get(email));
                    sender.countTransactions(oldAmount, timestamp, email);
                } else {
                    sender.withdraw(amount);
                }
                for (Map.Entry<String, CommerciantInput> entry : bank
                        .getCommerciants().entrySet()) {
                    if (entry.getValue().getAccount().equals(account2)) {
                        bank.getUserHashMap().get(sender.getEmail()).addCommercialTransaction(bank,
                                entry.getValue().getCommerciant(), oldAmount, sender.getCurrency(),
                                timestamp, sender);
                        Utils.addCashback(bank, bank.getUserHashMap().get(sender.getEmail()),
                                sender, oldAmount, entry.getValue());
                        sendMoneyTransaction2(sender, account2, oldAmount, oldAmount);
                        sender.countTransactions(oldAmount, timestamp, email);
                        return;
                    }
                }
                userNotFound();
                sender.deposit(amount);
            }
        }
    }

    private void userNotFound() {
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "sendMoney");
        ObjectNode status = bank.getObjectMapper().createObjectNode();
        status.put("description", "User not found");
        status.put("timestamp", timestamp);
        command.set("output", status);
        command.put("timestamp", timestamp);
        bank.getOutput().add(command);
    }
}
