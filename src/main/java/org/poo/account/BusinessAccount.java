package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;
import org.poo.bank.User;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BusinessAccount extends Account {
    private String owner;
    private Map<String, Associate> managers;
    private Map<String, Associate> employees;
    private Double spendingLimit;
    private Double depositLimit;
    private Double totalSpent;
    private Double totalDeposited;



    public BusinessAccount(Bank bank, final String iban, final String email, final String currency) {
        super(iban, email, currency, "business");
        owner = email;
        managers = new HashMap<>();
        employees = new HashMap<>();
        spendingLimit = bank.convertCurrency(500, "RON", currency, bank.prepareExchangeRates());
        depositLimit = bank.convertCurrency(500, "RON", currency, bank.prepareExchangeRates());
        totalDeposited = 0.0;
        totalSpent = 0.0;
    }
    public void addManager(final User manager) {
        managers.put(manager.getEmail(), new Associate(manager));
    }
    public void addEmployee(final User employee) {
        employees.put(employee.getEmail(), new Associate(employee));
    }

    public void deposit(final double amount, final String email, final int timestamp) {
        if (managers.containsKey(email)) {
            managers.get(email).addDeposits(timestamp, amount);
            totalDeposited += amount;
            super.deposit(amount);
        } else if (employees.containsKey(email)) {
            if (amount > depositLimit) {
                System.out.println("Amount is " + amount + " and deposit limit is " + depositLimit);
                System.out.println("Error: Deposit limit exceeded.");
                return;
            }
            totalDeposited += amount;
            super.deposit(amount);
            employees.get(email).addDeposits(timestamp, amount);
        } else if (email.equals(owner)) {
//            totalDeposited += amount;
            super.deposit(amount);
        } else {
            System.out.println("Error: You are not allowed to deposit money into this account.");
        }

    }

    public void withdraw(final double amount, final String email, final int timestamp, final double amountWithoutComission) {
        if (managers.containsKey(email)) {
            super.withdraw(amount);
            totalSpent += amountWithoutComission;
            managers.get(email).addSpendings(timestamp, amountWithoutComission);
        } else if (employees.containsKey(email)) {
            if (amount > spendingLimit) {
                System.out.println("Amount is " + amount + " and spending limit is " + spendingLimit);
                System.out.println("Error: Spending limit exceeded.");
                return;
            }
            super.withdraw(amount);
            totalSpent += amountWithoutComission;
            employees.get(email).addSpendings(timestamp, amountWithoutComission);
        } else if (email.equals(owner)) {
            super.withdraw(amount);
        } else {
            System.out.println("Error: You are not allowed to withdraw money from this account.");
        }
    }
    public void setSpendingLimit(final double newLimit) {
        spendingLimit = newLimit;
    }

    public void setDepositLimit(final double newLimit) {
        depositLimit = newLimit;
    }
    public boolean isBusiness() {
        return true;
    }

}
