package org.poo.account;

public class ClassicAccount extends Account {
    public ClassicAccount(final String iban, final String email, final String currency) {
        super(iban, email, currency, "classic");
    }
}
