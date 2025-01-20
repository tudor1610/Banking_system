package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.transactions.Transaction;


public class UpgradePlanCommand implements Command {
    private Bank bank;
    private String newPlan;
    private Account account;
    private int timestamp;

    public UpgradePlanCommand(final Bank bank, final String newPlan,
                              final String  account, final int timestamp) {
        this.bank = bank;
        this.newPlan = newPlan;
        this.account = bank.getAccountHashMap().get(account);
        this.timestamp = timestamp;
    }

    private int planType(final String plan) {
        final int myBeautifulMagicNumber = 3;
        switch (plan) {
            case "standard", "student" -> {
                return 1;
            }
            case "silver" -> {
                return 2;
            }
            case "gold" -> {
                return myBeautifulMagicNumber;
            }
            default -> {
                return 0;
            }
        }
    }

    /**
     * Upgrade the plan of a user.
     */
    @Override
    public void execute() {
        if (account == null) {
            ObjectNode command = bank.getObjectMapper().createObjectNode();
            command.put("command", "upgradePlan");
            ObjectNode status = bank.getObjectMapper().createObjectNode();
            status.put("description", "Account not found");
            status.put("timestamp", timestamp);
            command.set("output", status);
            command.put("timestamp", timestamp);
            bank.getOutput().add(command);
            return;
        }
        User user = bank.getUserHashMap().get(account.getEmail());
        int planType = planType(user.getPlan());
        int newPlanType = planType(newPlan);
        final int p1 = 1;
        final int p2 = 2;
        final int p3 = 3;

        if (planType == newPlanType) {
            Transaction t;
            switch (planType) {
                case p1 -> {
                    t = new Transaction.Builder(timestamp,
                            "The user already has the standard plan.").build();
                }
                case p2 -> {
                    t = new Transaction.Builder(timestamp,
                            "The user already has the silver plan.").build();
                }
                case p3 -> {
                    t = new Transaction.Builder(timestamp,
                            "The user already has the gold plan.").build();
                }
                default -> {
                    t = new Transaction.Builder(timestamp, "Plan not found").build();
                }
            }
            account.accountAddTransaction(t);
            user.addTransaction(t);
            return;
        }

        if (planType == p1 && newPlanType == p2) {
            final int magicNumber = 100;
            double amount = bank.convertCurrency(magicNumber, "RON",
                    account.getCurrency(), bank.prepareExchangeRates());
            if (account.getBalance() >= amount) {
                account.withdraw(amount);
                user.setPlan(newPlan);
                Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                        .accountNumber(account.getIban())
                        .newPlanType(newPlan)
                        .build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
                return;
            }

            if (account.getBalance() < amount) {
                Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                user.addTransaction(t);
                return;
            }
        }

        if (planType == p2 && newPlanType == p3) {
            final int magicNumber = 250;
                double amount = bank.convertCurrency(magicNumber, "RON",
                        account.getCurrency(), bank.prepareExchangeRates());
                if (account.getBalance() >= amount) {
                    account.withdraw(amount);
                    user.setPlan(newPlan);
                    Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                            .accountNumber(account.getIban())
                            .newPlanType(newPlan)
                            .build();
                    user.addTransaction(t);
                    account.accountAddTransaction(t);
                    return;
                }
                if (account.getBalance() < amount) {
                    Transaction t = new Transaction.Builder(timestamp,
                            "Insufficient funds").build();
                    user.addTransaction(t);
                    return;
                }
        }

        if (planType == p1 && newPlanType == p3) {
            final int magicNumber = 350;
            double amount = bank.convertCurrency(magicNumber, "RON",
                    account.getCurrency(), bank.prepareExchangeRates());
            if (account.getBalance() >= amount) {
                account.withdraw(amount);
                user.setPlan(newPlan);
                Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                        .accountNumber(account.getIban())
                        .newPlanType(newPlan)
                        .build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
                return;
            }
            if (account.getBalance() < amount) {
                Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                user.addTransaction(t);
            }
        }
    }
}
