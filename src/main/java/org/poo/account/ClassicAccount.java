package org.poo.account;

import org.poo.bank.Bank;

public class ClassicAccount extends Account {
    public ClassicAccount(final Bank bank, final String iban, final String email,
                          final String currency) {
        super(bank, iban, email, currency, "classic");
    }
}
