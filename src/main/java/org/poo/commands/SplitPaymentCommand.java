package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.SplitPayment;
import org.poo.bank.User;
import org.poo.fileio.ExchangeInput;
import org.poo.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SplitPaymentCommand implements Command {
    private Bank bank;
    private List<String> accounts;
    private int timestamp;
    private String currency;
    private double amount;
    private String splitPaymentType;
    private List<Double> amountForUser;


    public SplitPaymentCommand(final Bank bank, final List<String> accounts, final int timestamp,
                               final String currency, final double amount, final String splitPaymentType,
                               final List<Double> amountForUser) {

        this.bank = bank;
        this.accounts = accounts;
        this.timestamp = timestamp;
        this.currency = currency;
        this.amount = amount;
        this.splitPaymentType = splitPaymentType;
        this.amountForUser = amountForUser;
    }


    /**
     * Determines which account has insufficient funds for the amount to be split,
     * either in the given currency or converted to the account's currency using
     * the provided exchange rates.
     *
     * @param exchangeRates the map of exchange rates
     * @return the IBAN of the account with insufficient funds,
     *          or null if all accounts have sufficient funds
     */
//    private String lowBalanceAccount(final Map<String, Map<String, Double>> exchangeRates) {
//        int accNumber = accounts.size();
//        double sum = amount / accNumber;
//        String lowBallanceAccount = null;
//        for (String name : accounts) {
//            Account account = bank.getAccountHashMap().get(name);
//            if (account.getCurrency().equals(currency)) {
//                if (account.getBalance() < sum) {
//                    lowBallanceAccount = account.getIban();
//
//                }
//            } else {
//                Double convertedAmount = bank.convertCurrency(sum, currency,
//                        account.getCurrency(), exchangeRates);
//                if (account.getBalance() < convertedAmount) {
//                    lowBallanceAccount = account.getIban();
//                }
//            }
//        }
//        return lowBallanceAccount;
//    }

    /**
     * Splits a bill evenly among a list of accounts.
     * If one account has insufficient funds to cover its share,
     * no split occurs, and an error is logged for all involved accounts.
     * Otherwise, the bill is split and each
     * account is charged the appropriate amount, potentially with currency conversion.
     *
     */
    public void execute() {
        SplitPayment payment;
        if (splitPaymentType.equals("equal")) {
            amountForUser = new ArrayList<>();
            for (int i = 0; i < accounts.size(); i++) {
                amountForUser.add(amount / accounts.size());
            }
            System.out.println("amountForUsers pt equal pay");
            for (int i = 0; i < accounts.size(); i++) {
                System.out.println(amountForUser.get(i));
            }
            payment = new SplitPayment(bank, accounts, amountForUser, amount, currency, splitPaymentType,timestamp);
        } else {
            System.out.println("amountForUsers pt custom pay timestamp " + timestamp);
            System.out.println(amountForUser);
            System.out.println(splitPaymentType);
            payment = new SplitPayment(bank, accounts, amountForUser, amount, currency, splitPaymentType,timestamp);
        }
        bank.addWaitingPayment(payment);
//        int accNumber = accounts.size();
//        double sum = amount / accNumber;
//        Map<String, Map<String, Double>> exchangeRates = bank.prepareExchangeRates();
//        String lowBalanceAccount = lowBalanceAccount(exchangeRates);
//
//        // split bill
//        if (lowBalanceAccount == null) {
//            for (String name : accounts) {
//                Account account = bank.getAccountHashMap().get(name);
//                User user = bank.getUserHashMap().get(account.getEmail());
//                Double convertedAmount = bank.convertCurrency(sum, currency,
//                        account.getCurrency(), exchangeRates);
//                account.accountPayment(user, currency, amount, timestamp,
//                        sum, accounts, convertedAmount);
//            }
//        } else {
//            // don't split bill
//            for (String name : accounts) {
//                if (name != null) {
//                    Account account = bank.getAccountHashMap().get(name);
//                    User user = bank.getUserHashMap().get(account.getEmail());
//                    Transaction t = new Transaction.Builder(timestamp, "Split payment of ")
//                            .currency(currency)
//                            .amount(sum)
//                            .accountsInvolved(accounts)
//                            .totalBill(amount)
//                            .error("Account " + lowBalanceAccount
//                                    + " has insufficient funds for a split payment.")
//                            .build();
//                    user.addTransaction(t);
//                    account.accountAddTransaction(t);
//                }
//            }
//        }
    }

}
