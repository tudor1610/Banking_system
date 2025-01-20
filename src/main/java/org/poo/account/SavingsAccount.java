package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.Bank;

@Getter
@Setter
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final Bank bank, final String iban, final String email,
                          final String currency, final double interestRate) {
        super(bank, iban, email, currency, "savings");
        this.interestRate = interestRate;
    }

    /***
     * Adds the current interest to the account
     */
    @Override
    public void addInterest() {
        double amount = getBalance();
        deposit(amount * interestRate);
    }

    /***
     * Getter for the interest rate
     * @return
     */
    public double getInterestRate() {
        return interestRate;
    }
}
