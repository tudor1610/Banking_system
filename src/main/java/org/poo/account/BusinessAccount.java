package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;
import org.poo.bank.User;

import java.util.ArrayList;
import java.util.List;

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

    public BusinessAccount(final Bank bank, final String iban,
                           final String email, final String currency) {
        super(bank, iban, email, currency, "business");
        owner = email;
        managers = new ArrayList<>();
        employees = new ArrayList<>();
        commerciantSpendings = new ArrayList<>();
        final int limit = 500;
        spendingLimit = bank.convertCurrency(limit, "RON", currency, bank.prepareExchangeRates());
        depositLimit = bank.convertCurrency(limit, "RON", currency, bank.prepareExchangeRates());
        totalDeposited = 0.0;
        totalSpent = 0.0;
    }

    /**
     * Adds a manager to the business account
     * @param manager the manager to be added
     */
    public void addManager(final User manager) {
        managers.add(new Associate(manager));
    }

    /**
     * Adds an employee to the business account
     * @param employee the employee to be added
     */
    public void addEmployee(final User employee) {
        employees.add(new Associate(employee));
    }

    /**
     * Checks if a user is the owner
     * @param user the user to be checked
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(final User user) {
        return user.getEmail().equals(owner);
    }


    /**
     * Checks if a user is a manager
     * @param user the user to be checked
     * @return true if the user is a manager, false otherwise
     */
    private boolean isManager(final User user) {
        for (Associate associate : managers) {
            if (associate.getFirstName().equals(user.getFirstName())
                    && associate.getLastName().equals(user.getLastName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds spendings for a commerciant
     * @param commerciant the commerciant
     * @param amount the amount to be added
     * @param user the user that adds the spendings
     */
    public void addCommerciantSpendings(final String commerciant,
                                        final double amount, final User user) {
        if (isOwner(user)) {
            return;
        }
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

    /**
     * Deposits money into the account
     * @param amount the amount to be deposited
     * @param email the email of the user
     * @param timestamp the timestamp of the transaction
     */
    public void deposit(final double amount, final String email, final int timestamp) {
        if (email.equals(owner)) {
            super.deposit(amount);
            return;
        }
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
    }

    /**
     * Withdraws money from the account
     * @param amount the amount to be withdrawn
     * @param email the email of the user
     * @param timestamp the timestamp of the transaction
     * @param amountWithoutComission the amount without the comission
     */
    public void withdraw(final double amount, final String email, final int timestamp,
                         final double amountWithoutComission) {
        if (email.equals(owner)) {
            super.withdraw(amount);
            return;
        }
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
    }

    /**
     * Changes spending limit
     * @param newLimit the spending limit
     */
    public void setSpendingLimit(final double newLimit) {
        spendingLimit = newLimit;
    }


    /**
     * Changes deposit limit
     * @param newLimit the deposit limit
     */
    public void setDepositLimit(final double newLimit) {
        depositLimit = newLimit;
    }

    /**
     * Method used for extracting the test number from the file name.
     *
     * @return the extracted numbers
     */
    public boolean isBusiness() {
        return true;
    }

}
