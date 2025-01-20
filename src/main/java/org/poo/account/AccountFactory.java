package org.poo.account;

import org.poo.bank.Bank;
import org.poo.utils.Utils;

public final class AccountFactory {

    private static AccountFactory accountFactory = null;

    private AccountFactory() {

    }

    /**
     * Returns a singleton instance of the {@code AccountFactory}.
     * This method ensures that only one instance of {@code AccountFactory} is created,
     * using lazy instantiation.
     *
     * @return the singleton instance of {@code AccountFactory}
     */
    public static AccountFactory getAccountFactory() {
        if (accountFactory == null) {
            accountFactory = new AccountFactory();
        }
        return accountFactory;
    }

    /**
     * Factory method to create an account based on the specified account type.
     *
     * @param email        the email address associated with the account
     * @param currency     the currency for the account (e.g., USD, EUR)
     * @param accountType  the type of the account ("savings" or "classic")
     * @param timestamp    the creation timestamp of the account
     * @param interestRate the interest rate for savings accounts, null for other account types
     * @return an instance of {@code SavingsAccount} if the account type is "savings",
     *         otherwise an instance of {@code ClassicAccount}
     */
    public static Account createAccount(final Bank bank, final String email, final String currency,
                                        final String accountType, final int timestamp,
                                        final Double interestRate) {
        if (accountType.equalsIgnoreCase("savings")) {
            return new SavingsAccount(bank, Utils.generateIBAN(), email, currency, interestRate);
        } else if (accountType.equalsIgnoreCase("business")) {
            return new BusinessAccount(bank, Utils.generateIBAN(), email, currency);
        } else {
            return new ClassicAccount(bank, Utils.generateIBAN(), email, currency);
        }
    }
}
