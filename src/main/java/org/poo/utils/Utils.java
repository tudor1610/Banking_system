package org.poo.utils;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.fileio.CommerciantInput;

import java.util.Map;
import java.util.Random;

public final class Utils {
    private Utils() {
        // Checkstyle error free constructor
    }

    private static final int IBAN_SEED = 1;
    private static final int CARD_SEED = 2;
    private static final int DIGIT_BOUND = 10;
    private static final int DIGIT_GENERATION = 16;
    private static final String RO_STR = "RO";
    private static final String POO_STR = "POOB";

    private static Random ibanRandom = new Random(IBAN_SEED);
    private static Random cardRandom = new Random(CARD_SEED);

    /**
     * Utility method for generating an IBAN code.
     *
     * @return the IBAN as String
     */
    public static String generateIBAN() {
        StringBuilder sb = new StringBuilder(RO_STR);
        for (int i = 0; i < RO_STR.length(); i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        sb.append(POO_STR);
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Utility method for generating a card number.
     *
     * @return the card number as String
     */
    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(cardRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Resets the seeds between runs.
     */
    public static void resetRandom() {
        ibanRandom = new Random(IBAN_SEED);
        cardRandom = new Random(CARD_SEED);
    }

    /**
     * Method used for calculating the commission.
     *
     * @param user the user
     * @param amount the amount
     * @param bank the bank
     * @param currency the currency
     * @return the commission
     */
    public static Double comision(final User user, final double amount,
                                  final Bank bank, final String currency) {
        Double convertedAmount = amount;
        if (!currency.equals("RON")) {
            convertedAmount = bank.convertCurrency(amount, currency,
                    "RON", bank.prepareExchangeRates());
        }
        if (user == null) {
            return amount;
        }

        final double commission1 = 0.002;
        final double commission2 = 0.001;
        final int limit = 500;
        if ("student".equals(user.getPlan())) {
            return amount;
        } else if ("standard".equals(user.getPlan())) {
            return amount + commission1 * amount;
        } else if ("silver".equals(user.getPlan())) {
            if (convertedAmount < limit) {
                return amount;
            }
            return amount + commission2 * amount;
        }
        return amount;
    }

    /**
     * Method used for adding cashback to an account.
     *
     * @param bank the bank
     * @param user the user
     * @param account the account
     * @param amount the amount
     * @param commerciant the commerciant
     */
    public static void addCashback(final Bank bank, final User user, final Account account,
                                   final double amount, final CommerciantInput commerciant) {

        final double cb1 = 0.02;
        final double cb2 = 0.05;
        final double cb3 = 0.1;
        switch (commerciant.getType()) {
            case "Food" -> {
                if (account.isFood()) {
                    account.deposit(cb1 * amount);
                    account.setFood(false);
                }
            }
            case "Clothes" -> {
                if (account.isClothes()) {
                    account.deposit(cb2 * amount);
                    account.setClothes(false);
                }
            }
            case "Tech" -> {
                if (account.isTech()) {
                    account.deposit(cb3 * amount);
                    account.setTech(false);
                }
            }
            default -> {
            }
        }

        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            account.addNrOfTransaction(commerciant.getCommerciant());
            final int limit1 = 2;
            final int limit2 = 5;
            final int limit3 = 10;
            if (account.getNrOfTransactions().get(commerciant.getCommerciant()) == limit1) {
                account.setFood(true);
            }
            if (account.getNrOfTransactions().get(commerciant.getCommerciant()) == limit2) {
                account.setClothes(true);
            }
            if (account.getNrOfTransactions().get(commerciant.getCommerciant()) == limit3) {
                account.setTech(true);
            }
        }


       if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
           Double spent = user.getCommercialTransactions().entrySet().stream()
                   .filter(entry -> "spendingThreshold"
                           .equals(bank.getCommerciants()
                                   .get(entry.getKey()).getCashbackStrategy()))
                   .mapToDouble(Map.Entry::getValue)
                   .sum();
            final int limit1 = 100;
            final int limit2 = 300;
            final int limit3 = 500;
           if (spent >= limit3) {
                final double cashback1 = 0.0025;
                final double cashback2 = 0.005;
                final double cashback3 = 0.007;
               switch (user.getPlan()) {
                   case "student" -> account.deposit(cashback1 * amount);
                   case "standard" -> account.deposit(cashback1 * amount);
                   case "silver" -> account.deposit(cashback2 * amount);
                   case "gold" -> account.deposit(cashback3 * amount);
                   default -> {
                   }
               }
           } else if (spent >= limit2) {
                final double cashback1 = 0.002;
                final double cashback2 = 0.004;
                final double cashback3 = 0.0055;
               if (user.getPlan().equals("student")) {
                   account.deposit(cashback1 * amount);
               } else if ("standard".equals(user.getPlan())) {
                   account.deposit(cashback1 * amount);
               } else if ("silver".equals(user.getPlan())) {
                   account.deposit(cashback2 * amount);
               } else if ("gold".equals(user.getPlan())) {
                   account.deposit(cashback3 * amount);
               }
           } else if (spent >= limit1) {
                final double cashback1 = 0.001;
                final double cashback2 = 0.003;
                final double cashback3 = 0.005;
               if (user.getPlan().equals("student")) {
                   account.deposit(cashback1 * amount);
               } else if ("standard".equals(user.getPlan())) {
                   account.deposit(cashback1 * amount);
               } else if ("silver".equals(user.getPlan())) {
                   account.deposit(cashback2 * amount);
               } else if ("gold".equals(user.getPlan())) {
                   account.deposit(cashback3 * amount);
               }
           }
       }
    }
}
