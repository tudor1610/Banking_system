package org.poo.commands;

import org.poo.account.Account;
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

    public void execute() {
        Account account = bank.getAccountHashMap().get(IBAN);
        if (role.equals("manager")) {
            account.addManager(bank.getUserHashMap().get(email));
        } else {
            account.addEmployee(bank.getUserHashMap().get(email));
        }
    }
}
