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
    private List<Associate> managers;
    private List<Associate> employees;
    private Double spendingLimit;
    private Double depositLimit;
    private Double totalSpent;
    private Double totalDeposited;
    private List<CommerciantSpendings> commerciantSpendings;



    public BusinessAccount(Bank bank, final String iban, final String email, final String currency) {
        super(bank, iban, email, currency, "business");
        owner = email;
        managers = new ArrayList<>();
        employees = new ArrayList<>();
        commerciantSpendings = new ArrayList<>();
        spendingLimit = bank.convertCurrency(500, "RON", currency, bank.prepareExchangeRates());
        depositLimit = bank.convertCurrency(500, "RON", currency, bank.prepareExchangeRates());
        totalDeposited = 0.0;
        totalSpent = 0.0;
    }
    public void addManager(final User manager) {
        managers.add(new Associate(manager));
    }
    public void addEmployee(final User employee) {
        employees.add(new Associate(employee));
    }

    public boolean isOwner(final User user) {
        return user.getEmail().equals(owner);
    }


    private boolean isManager(final User user) {
        for (Associate associate : managers) {
            if (associate.getFirstName().equals(user.getFirstName()) && associate.getLastName().equals(user.getLastName())) {
                return true;
            }
        }
        return false;
    }

    public void addCommerciantSpendings(final String commerciant, final double amount, final User user) {
        if (isOwner(user))
            return;
        for (CommerciantSpendings comm : commerciantSpendings) {
            if (comm.getCommerciant().equals(commerciant)) {
                if (isManager(user)) {
                    String name = user.getLastName() + " " + user.getFirstName();
                    comm.addEntry(name, amount, true);
                    return;
                }
                String name = user.getLastName() + " " + user.getFirstName();
                comm.addEntry(name, amount, false);
                return;
            }
        }
        CommerciantSpendings newCommerciant = new CommerciantSpendings(commerciant);
        commerciantSpendings.add(newCommerciant);
        for (CommerciantSpendings comm : commerciantSpendings) {
            if (comm.getCommerciant().equals(commerciant)) {
                if (isManager(user)) {
                    String name = user.getLastName() + " " + user.getFirstName();
                    comm.addEntry(name, amount, true);
                    return;
                }
                String name = user.getLastName() + " " + user.getFirstName();
                comm.addEntry(name, amount, false);
                return;
            }
        }
    }

    public void deposit(final double amount, final String email, final int timestamp) {

        for (Associate associate : managers) {
            if (associate.getEmail().equals(email)) {
                associate.addDeposits(timestamp, amount);
                totalDeposited += amount;
                super.deposit(amount);
                return;
            }
        }

        for (Associate associate : employees) {
            if (associate.getEmail().equals(email)) {
                if (amount > depositLimit) {

                    return;
                }
                associate.addDeposits(timestamp, amount);
                totalDeposited += amount;
                super.deposit(amount);
                return;
            }
        }
        if (email.equals(owner)) {
            super.deposit(amount);
        } else {
            System.out.println("Error: You are not allowed to deposit money into this account.");
        }

    }

    public void withdraw(final double amount, final String email, final int timestamp, final double amountWithoutComission) {
        for (Associate associate : managers) {
            if (associate.getEmail().equals(email)) {
                super.withdraw(amount);
                totalSpent += amountWithoutComission;
                associate.addSpendings(timestamp, amountWithoutComission);
                return;
            }
        }

        for (Associate associate : employees) {
            if (associate.getEmail().equals(email)) {
                if (amount > spendingLimit) {
                    return;
                }
                super.withdraw(amount);
                totalSpent += amountWithoutComission;
                associate.addSpendings(timestamp, amountWithoutComission);
                return;
            }
        }
        if (email.equals(owner)) {
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
