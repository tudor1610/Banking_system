package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.bank.SplitPayment;
import org.poo.bank.User;

import java.util.List;

public class RejectSplitPaymentCommand implements Command {
    private Bank bank;
    private String email;
    private int timestamp;
    private String splitType;

    public RejectSplitPaymentCommand(final Bank bank, final String email, final int timestamp,
                                     final String splitType) {
        this.bank = bank;
        this.email = email;
        this.timestamp = timestamp;
        this.splitType = splitType;
    }

    /**
     * Reject a split payment.
     */
    @Override
    public void execute() {
        User user = bank.getUserHashMap().get(email);
        if (user == null) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "rejectSplitPayment");
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "User not found");
            status.put("timestamp", timestamp);
            command.set("output", status);
            command.put("timestamp", timestamp);
            bank.getOutput().add(command);
            return;
        }
        List<SplitPayment> paymentList = bank.getWaitingPayments();
        for (SplitPayment payment : paymentList) {
            for (int i = 0; i < payment.getAccounts().length; i++) {
                if (payment.getPaymentType().equals(splitType)) {
                    if (payment.getAccounts()[i].getEmail().equals(email)
                            && payment.getAccepted()[i].equals(false)) {
                        payment.getAccepted()[i] = false;
                        payment.rejectedPayment();
                        return;
                    }
                }
            }
        }
    }
}
