package org.poo.bank;

import lombok.Getter;
import org.poo.account.Account;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

import java.util.Arrays;
import java.util.List;

@Getter
public class SplitPayment {
    private Bank bank;
    private Account[] accounts;
    private Double[] amounts;
    private Boolean[] accepted;
    private String currency;
    private double totalAmount;
    private String paymentType;
    private int timestamp;

    public SplitPayment(final Bank bank, final List<String> accounts, final List<Double> amounts,
                        final double totalAmount, final String currency, final String paymentType,
                        final int timestamp) {
        this.bank = bank;
        this.accounts = new Account[accounts.size()];
        for (String a : accounts) {
            this.accounts[accounts.indexOf(a)] = bank.getAccountHashMap().get(a);
        }
        this.amounts = new Double[amounts.size()];
        for (int i = 0; i < amounts.size(); i++) {
            this.amounts[i] = amounts.get(i);
        }
        this.currency = currency;
        this.paymentType = paymentType;
        this.timestamp = timestamp;
        this.accepted = new Boolean[accounts.size()];
        this.totalAmount = totalAmount;
        for (int i = 0; i < accounts.size(); i++) {
            this.accepted[i] = false;
        }
    }

    /**
     * Accept the payment.
     * @param email the email of the user that accepted the payment
     */
    public void paymentCheck() {
        for (int i = 0; i < accounts.length; i++) {
            if (!accepted[i]) {
                return;
            }
        }
        processPayment();
    }

    /**
     * Cancel the payment.
     * @param lowBalanceAccount the account with insufficient funds
     */
    private void cancelPayment(final String lowBalanceAccount) {
        for (int i = 0; i < accounts.length; i++) {
            User user = bank.getUserHashMap().get(accounts[i].getEmail());
            Transaction t = new Transaction.Builder(timestamp, "Split payment of ")
                    .currency(currency)
                    .amountForUsers(Arrays.stream(amounts).toList())
                    .accountsInvolved(Arrays.stream(accounts).map(Account::getIban).toList())
                    .totalBill(totalAmount)
                    .splitPaymentType(paymentType)
                    .error("Account " + lowBalanceAccount
                            + " has insufficient funds for a split payment.")
                    .build();
            user.addTransaction(t);
            accounts[i].accountAddTransaction(t);
        }
        bank.removeWaitingPayment(this);
    }

    /**
     * Reject the payment.
     */
    public void rejectedPayment() {
        for (int i = 0; i < accounts.length; i++) {
            User user = bank.getUserHashMap().get(accounts[i].getEmail());
            Transaction t = new Transaction.Builder(timestamp, "Split payment of ")
                    .currency(currency)
                    .amountForUsers(Arrays.stream(amounts).toList())
                    .splitPaymentType(paymentType)
                    .accountsInvolved(Arrays.stream(accounts).map(Account::getIban).toList())
                    .totalBill(totalAmount)
                    .error("One user rejected the payment.")
                    .build();
            user.addTransaction(t);
            accounts[i].accountAddTransaction(t);
        }
        bank.removeWaitingPayment(this);
    }

    /**
     * Process the payment.
     */
    private void processPayment() {

        for (int i = 0; i < accounts.length; i++) {
            double amount = amounts[i];
            double convertedAmount = bank.convertCurrency(amount, currency,
                    accounts[i].getCurrency(), bank.prepareExchangeRates());
            double amountAfterCommission = Utils.comision(bank.getUserHashMap()
                    .get(accounts[i].getEmail()), convertedAmount, bank,
                    accounts[i].getCurrency());
            if (accounts[i].getBalance() < amountAfterCommission) {
                // cannot make payment; cancel all payments
                cancelPayment(accounts[i].getIban());
                return;
            }
        }

        for (int i = 0; i < accounts.length; i++) {
            double amount = amounts[i];
            double convertedAmount = bank.convertCurrency(amount, currency,
                    accounts[i].getCurrency(), bank.prepareExchangeRates());
            accounts[i].accountPayment(bank.getUserHashMap().get(accounts[i].getEmail()),
                    currency, totalAmount, timestamp, amount, Arrays.stream(amounts).toList(),
                    Arrays.stream(accounts).map(Account::getIban).toList(),
                    convertedAmount, paymentType);
        }
        bank.removeWaitingPayment(this);
    }
}
