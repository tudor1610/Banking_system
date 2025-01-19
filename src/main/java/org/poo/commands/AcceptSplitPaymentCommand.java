package org.poo.commands;

import org.poo.bank.Bank;
import org.poo.bank.SplitPayment;

import java.util.List;

public class AcceptSplitPaymentCommand implements Command{
    private Bank bank;
    private String email;
    private int timestamp;

    public AcceptSplitPaymentCommand(final Bank bank, final String email, final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.timestamp = timestamp;
    }

    public void execute() {
        System.out.println("AcceptSplitPaymentCommand  timestamp = " + timestamp);
        List<SplitPayment> paymentList = bank.getWaitingPayments();
        for (SplitPayment payment : paymentList) {
            for (int i = 0; i < payment.getAccounts().length; i++) {
                if (payment.getAccounts()[i].getEmail().equals(email) && payment.getAccepted()[i].equals(false)) {
                    payment.getAccepted()[i] = true;
                    System.out.println("payment : " + payment.getTotalAmount());
                    payment.paymentCheck();
                    return;
                }
            }
        }
    }
}
