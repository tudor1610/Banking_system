package org.poo.commands;

import org.poo.account.Account;
import org.poo.account.Associate;
import org.poo.bank.Bank;

public class NewBusinessAssociateCommand implements Command{
    private Bank bank;
    private String IBAN;
    private String role;
    private String email;
    private int timestamp;

    public NewBusinessAssociateCommand(final Bank bank, final String IBAN, final String role, final String email, final int timestamp) {
        this.bank = bank;
        this.IBAN = IBAN;
        this.role = role;
        this.email = email;
        this.timestamp = timestamp;
    }

    public boolean isEmployee() {
        Account account = bank.getAccountHashMap().get(IBAN);
        for (Associate associate : account.getEmployees()) {
            if (associate.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public boolean isManager() {
        Account account = bank.getAccountHashMap().get(IBAN);
        for (Associate associate : account.getManagers()) {
            if (associate.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner() {
        Account account = bank.getAccountHashMap().get(IBAN);
        return account.getOwner().equals(email);
    }

    public void execute() {
        Account account = bank.getAccountHashMap().get(IBAN);
        if (role.equals("manager") && !isEmployee() && !isOwner() && !isManager()) {
            account.addManager(bank.getUserHashMap().get(email));
        } else if (!isEmployee() && !isOwner() && !isManager()) {
            account.addEmployee(bank.getUserHashMap().get(email));
        }
    }
}
