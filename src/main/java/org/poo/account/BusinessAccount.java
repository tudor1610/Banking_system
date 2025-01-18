package org.poo.account;

public class BusinessAccount extends Account {
    private String owner;

    public BusinessAccount(final String iban, final String email, final String currency) {
        super(iban, email, currency, "classic");
        owner = email;
    }
}
