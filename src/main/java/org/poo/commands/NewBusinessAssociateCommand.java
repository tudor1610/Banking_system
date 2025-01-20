package org.poo.commands;

import org.poo.account.Account;
import org.poo.account.Associate;
import org.poo.bank.Bank;

public class NewBusinessAssociateCommand implements Command {
    private Bank bank;
    private String iban;
    private String role;
    private String email;
    private int timestamp;

    public NewBusinessAssociateCommand(final Bank bank, final String iban, final String role,
                                       final String email, final int timestamp) {
        this.bank = bank;
        this.iban = iban;
        this.role = role;
        this.email = email;
        this.timestamp = timestamp;
    }

    /**
     * Check if the user is an employee of the account.
     * @return true if the user is an employee of the account, false otherwise
     */
    public boolean isEmployee() {
        Account account = bank.getAccountHashMap().get(iban);
        for (Associate associate : account.getEmployees()) {
            if (associate.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the user is a manager of the account.
     * @return true if the user is a manager of the account, false otherwise
     */
    public boolean isManager() {
        Account account = bank.getAccountHashMap().get(iban);
        for (Associate associate : account.getManagers()) {
            if (associate.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the user is the owner of the account.
     * @return true if the user is the owner of the account, false otherwise
     */
    public boolean isOwner() {
        Account account = bank.getAccountHashMap().get(iban);
        return account.getOwner().equals(email);
    }

    /**
     * Add a new business associate to the account.
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(iban);
        if (role.equals("manager") && !isEmployee() && !isOwner() && !isManager()) {
            account.addManager(bank.getUserHashMap().get(email));
        } else if (!isEmployee() && !isOwner() && !isManager()) {
            account.addEmployee(bank.getUserHashMap().get(email));
        }
    }
}
