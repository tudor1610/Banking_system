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

    public static boolean isOlderThan21(String birthDateString) {
        LocalDate birthDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_DATE);
        LocalDate today = LocalDate.now();
        return ChronoUnit.YEARS.between(birthDate, today) >= 21;
    }

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
                //error: "You don't have a classic account in the same currency."
                Transaction t = new Transaction
                        .Builder(timestamp, "You do not have a classic account.").build();
                user.addTransaction(t);
                account.accountAddTransaction(t);
                return;
            }

        } else {
            //error: "You don't have the minimum age required."
//            System.out.println("NUUUUUUUUUUUUUUUUUU sefule!!!! timestamp: " + timestamp);

            Transaction t = new Transaction
                    .Builder(timestamp, "You don't have the minimum age required.").build();
            user.addTransaction(t);
            return;
        }
    }

}
