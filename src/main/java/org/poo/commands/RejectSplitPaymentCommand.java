package org.poo.commands;

import org.poo.bank.Bank;
import org.poo.bank.SplitPayment;

import java.util.List;

public class RejectSplitPaymentCommand implements Command{
    private Bank bank;
    private String email;
    private int timestamp;

    public RejectSplitPaymentCommand(final Bank bank, final String email, final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.timestamp = timestamp;
    }

    public void execute() {
        List<SplitPayment> paymentList = bank.getWaitingPayments();
        for (SplitPayment payment : paymentList) {
            for (int i = 0; i < payment.getAccounts().length; i++) {
                if (payment.getAccounts()[i].getEmail().equals(email) && payment.getAccepted()[i].equals(false)) {
                    payment.getAccepted()[i] = false;
                    payment.rejectedPayment();
                    return;
                }
            }
        }
    }
}
