package org.poo.commands;

import org.poo.bank.Bank;
import org.poo.bank.SplitPayment;

import java.util.ArrayList;
import java.util.List;


public class SplitPaymentCommand implements Command {
    private Bank bank;
    private List<String> accounts;
    private int timestamp;
    private String currency;
    private double amount;
    private String splitPaymentType;
    private List<Double> amountForUser;


    public SplitPaymentCommand(final Bank bank, final List<String> accounts, final int timestamp,
                               final String currency, final double amount,
                               final String splitPaymentType, final List<Double> amountForUser) {

        this.bank = bank;
        this.accounts = accounts;
        this.timestamp = timestamp;
        this.currency = currency;
        this.amount = amount;
        this.splitPaymentType = splitPaymentType;
        this.amountForUser = amountForUser;
    }


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
            payment = new SplitPayment(bank, accounts, amountForUser, amount,
                    currency, splitPaymentType, timestamp);
        } else {
            payment = new SplitPayment(bank, accounts, amountForUser, amount,
                    currency, splitPaymentType, timestamp);
        }
        bank.addWaitingPayment(payment);
    }
}
