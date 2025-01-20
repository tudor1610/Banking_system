package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.transactions.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;


public class WithdrawSavingsCommand implements Command {
    private Bank bank;
    private String iban;
    private double amount;
    private String currency;
    private int timestamp;

    public WithdrawSavingsCommand(final Bank bank, final String iban,
                                  final double amount, final String currency, final int timestamp) {
        this.bank = bank;
        this.iban = iban;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    /**
     * Check if a user is older than 21 years old.
     * @param birthDateString the birth date of the user
     * @return true if the user is older than 21 years old, false otherwise
     */
    public static boolean isOlderThan21(final String birthDateString) {
        LocalDate birthDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_DATE);
        LocalDate today = LocalDate.now();
        final int legalDrinkingAgeInTheUS = 21;
        return ChronoUnit.YEARS.between(birthDate, today) >= legalDrinkingAgeInTheUS;
    }

    /**
     * Withdraw money from a savings account, if an classic account exists.
     */
    @Override
    public void execute() {
        Account account = bank.getAccountHashMap().get(iban);
        User user = bank.getUserHashMap().get(account.getEmail());

        if (!account.getAccountType().equals("savings")) {
            // error
            return;
        }

        if (isOlderThan21(user.getBirthDate())) {
            boolean hasClassicAccount = false;
            if (!currency.equals(account.getCurrency())) {
                Map<String, Map<String, Double>> exchangeRates = bank.prepareExchangeRates();
                amount = bank.convertCurrency(amount, currency,
                        account.getCurrency(), exchangeRates);
            }

            for (Account a : user.getAccounts()) {
                if (a.getAccountType().equals("classic") && a.getCurrency().equals(currency)) {
                    if (account.getBalance() < amount) {
                        break;
                    }
                    account.withdraw(amount);
                    a.deposit(amount);
                    Transaction t = new Transaction.Builder(timestamp, "Savings withdrawal")
                            .classicAccountIBAN(a.getIban())
                            .savingsAccountIBAN(account.getIban())
                            .amount(amount)
                            .build();
                    user.addTransaction(t);
                    user.addTransaction(t);
                    account.accountAddTransaction(t);
                    a.accountAddTransaction(t);
                    hasClassicAccount = true;
                    break;
                }
            }
            if (!hasClassicAccount) {
                Transaction t = new Transaction
                        .Builder(timestamp, "You do not have a classic account.").build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
            }
        } else {
            Transaction t = new Transaction
                    .Builder(timestamp, "You don't have the minimum age required.").build();
            user.addTransaction(t);
            return;
        }
    }

}
