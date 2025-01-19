package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.jshell.execution.Util;
import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.transactions.Transaction;
import org.poo.utils.Utils;

public class UpgradePlanCommand implements Command{
    private Bank bank;
    private String newPlan;
    private Account account;
    private int timestamp;

    public UpgradePlanCommand(final Bank bank, final String newPlan, final String  account, final int timestamp) {
        this.bank = bank;
        this.newPlan = newPlan;
        this.account = bank.getAccountHashMap().get(account);
        this.timestamp = timestamp;
    }

    private int planType(final String plan) {
        switch (plan) {
            case "standard", "student" -> {
                return 1;
            }
            case "silver" -> {
                return 2;
            }
            case "gold" -> {
                return 3;
            }
            default -> {
                return 0;
            }
        }
    }

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
//        System.out.println("timestamp " + timestamp + " planType " + planType + " newPlanType " + newPlanType);
//        System.out.println("Account :" + account.getIban());
        if (planType == 1 && newPlanType == 2) {
            double amount = bank.convertCurrency(100, "RON", account.getCurrency() , bank.prepareExchangeRates());
            //System.out.println("Account currency " +account.getCurrency()  + " Account balance " + account.getBalance() + " converted amount " + amount);
            if (account.getBalance() >= amount) {
                account.withdraw(amount);
                user.setPlan(newPlan);
                Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                        .accountNumber(account.getIban())
                        .newPlanType(newPlan)
                        .build();
                user.addTransaction(t);
                return;
            }

            if (account.getBalance() < amount) {
                // error
                System.out.println("NO MONEY SARACULE!     timestamp: " + timestamp);
                Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                user.addTransaction(t);
                return;
            }
        }

        if (planType == 2 && newPlanType == 3) {
                double amount = bank.convertCurrency(250, "RON", account.getCurrency(), bank.prepareExchangeRates());
                //System.out.println("Account currency " +account.getCurrency()  + " Account balance " + account.getBalance() + " converted amount " + amount);
                if (account.getBalance() >= amount) {
                    account.withdraw(amount);
                    user.setPlan(newPlan);
                    Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                            .accountNumber(account.getIban())
                            .newPlanType(newPlan)
                            .build();
                    user.addTransaction(t);
                    return;
                }
                if (account.getBalance() < amount) {
                    System.out.println("NO MONEY SARACULE! 2  timestamp: " + timestamp);
                    Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                    user.addTransaction(t);
                    return;
                }
        }

        if (planType == 1 && newPlanType == 3) {
            double amount = bank.convertCurrency(350, "RON",  account.getCurrency(), bank.prepareExchangeRates());
            //System.out.println("Account currency " +account.getCurrency()  + " Account balance " + account.getBalance() + " converted amount " + amount);
            if (account.getBalance() >= amount) {
                account.withdraw(amount);
                user.setPlan(newPlan);
                Transaction t = new Transaction.Builder(timestamp, "Upgrade plan")
                        .accountNumber(account.getIban())
                        .newPlanType(newPlan)
                        .build();
                user.addTransaction(t);
                return;
            }
            if (account.getBalance() < amount) {
                System.out.println("NO MONEY SARACULE! 3    timestamp: " + timestamp);
                Transaction t = new Transaction.Builder(timestamp, "Insufficient funds").build();
                user.addTransaction(t);
                return;
            }
        }

    }
}
