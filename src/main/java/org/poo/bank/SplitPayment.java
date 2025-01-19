package org.poo.bank;

import lombok.Getter;
import org.poo.account.Account;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

import javax.sound.midi.Soundbank;
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

    public SplitPayment(Bank bank, List<String> accounts, List<Double> amounts, double totalAmount, String currency, String paymentType, int timestamp) {
        this.bank = bank;
        this.accounts = new Account[accounts.size()];
        for (String a : accounts) {
            this.accounts[accounts.indexOf(a)] = bank.getAccountHashMap().get(a);
        }
        this.amounts = new Double[amounts.size()];
        for (int i = 0; i < amounts.size(); i++) {
            //System.out.println("in Split payment la timestamp " + timestamp + " indexOf(a) = " + i);
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

    public void paymentCheck() {
        for (int i = 0; i < accounts.length; i++) {
            if (!accepted[i]) {
                System.out.println("Payment not accepted by all users yet.");
                return;
            }
        }
        System.out.println("Payment accepted by all users.");
        processPayment();
    }

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

    public void rejectedPayment() {
        for (int i = 0; i < accounts.length; i++) {
            User user = bank.getUserHashMap().get(accounts[i].getEmail());
            Transaction t = new Transaction.Builder(timestamp, "Split payment of ")
                    .currency(currency)
                    .amount(amounts[i])
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

    private void processPayment() {

//        System.out.println("Processing payment timestamp " + timestamp);
//        System.out.println("accounts.length = " + accounts.length);
        for (int i = 0; i < accounts.length; i++) {
            double amount = amounts[i];
            double convertedAmount = bank.convertCurrency(amount, currency,
                    accounts[i].getCurrency(), bank.prepareExchangeRates());
            double amountAfterCommission = Utils.comision(bank.getUserHashMap()
                    .get(accounts[i].getEmail()), convertedAmount, bank, accounts[i].getCurrency());
            if (accounts[i].getBalance() < amountAfterCommission) {
                // cannot make payment; cancel all payments
                cancelPayment(accounts[i].getIban());
                return;
            }
        }

        System.out.println("timestamp " + timestamp);
        System.out.println("AmountforUsers in split payment " + Arrays.stream(amounts).toList());
        System.out.println("splitPaymentType " + paymentType);
        // Pot aparea erori; fii atent aici
        for (int i = 0; i < accounts.length; i++) {
            double amount = amounts[i];
            double convertedAmount = bank.convertCurrency(amount, currency,
                    accounts[i].getCurrency(), bank.prepareExchangeRates());
            double amountAfterCommission = Utils.comision(bank.getUserHashMap()
                    .get(accounts[i].getEmail()), convertedAmount, bank, accounts[i].getCurrency());
            accounts[i].accountPayment(bank.getUserHashMap().get(accounts[i].getEmail()),
                    currency, totalAmount, timestamp, amount, Arrays.stream(amounts).toList(),
                    Arrays.stream(accounts).map(Account::getIban).toList(), amountAfterCommission, paymentType);
        }
        bank.removeWaitingPayment(this);
    }
}
