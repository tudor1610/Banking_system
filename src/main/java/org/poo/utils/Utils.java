package org.poo.utils;

import org.poo.account.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.fileio.CommerciantInput;

import javax.sound.midi.Soundbank;
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

    public static Double comision(User user, double amount, Bank bank, String currency) {
//        System.out.println("User plan " + user.getPlan());
//        System.out.println("user ocupation " + user.getOccupation());
        Double converted_amount = amount;
        if (!currency.equals("RON")) {
            converted_amount = bank.convertCurrency(amount,currency,
                    "RON", bank.prepareExchangeRates());
        }
        if (user == null)
            return amount;
        if ("student".equals(user.getPlan())) {
           // System.out.println("ii iau comision student");
            return amount;
        } else if ("standard".equals(user.getPlan())) {
            //System.out.println("ii iau comision standard");
            return amount + 0.002 * amount;
        } else if ("silver".equals(user.getPlan())) {
           // System.out.println("ii iau comision silver");
            if (converted_amount < 500) {
                return amount;
            }
            return amount + 0.001 * amount;
        }
        //System.out.println("ii iau comision gold");
        return amount;

    }

    public static void addCashback(Bank bank, User user, Account account, double amount, CommerciantInput commerciant) {

        switch (commerciant.getType()) {
            case "Food" -> {
                if (account.getFood() > 0) {
                    account.deposit(0.02 * amount);
                    account.setFood(account.getFood() - 1);
                }
            }
            case "Clothes" -> {
                if (account.getClothes() > 0) {
                    account.deposit(0.05 * amount);
                    account.setClothes(account.getClothes() - 1);
                }
            }
            case "Tech" -> {
                if (account.getTech() > 0) {
                    account.deposit(0.1 * amount);
                    account.setTech(account.getTech() - 1);
                }
            }
        }

        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            account.addNrOfTransaction(commerciant.getCommerciant());
            if (account.getNrOfTransactions().get(commerciant.getCommerciant()) == 2) {
                account.setFood(account.getFood() + 1);
            }
            if (account.getNrOfTransactions().get(commerciant.getCommerciant()) == 5) {
                account.setClothes(account.getClothes() + 1);
            }
            if (account.getNrOfTransactions().get(commerciant.getCommerciant()) == 10) {
                account.setTech(account.getTech() + 1);
            }
        }


       if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
           System.out.println("Usserul " + user.getEmail() + "ia cashback si are plan " +  user.getPlan() + " balance " + account.getBalance());
           Double spent = user.getCommercialTransactions().entrySet().stream()
                   .filter(entry -> "spendingThreshold".equals(bank.getCommerciants().get(entry.getKey()).getCashbackStrategy()))
                   .mapToDouble(Map.Entry::getValue)
                   .sum();
           System.out.println("spent " + spent);
           if (spent >= 500) {
               if (user.getPlan().equals("student")) {
                   account.deposit(0.0025 * amount);
               } else if ( user.getPlan().equals("standard")) {
                   account.deposit(0.0025 * amount);
               } else if (user.getPlan().equals("silver")) {
                   account.deposit(0.005 * amount);
               } else if (user.getPlan().equals("gold")) {
                   account.deposit(0.007 * amount);
               }
           } else if (spent >= 300) {
               if (user.getPlan().equals("student")){
                   account.deposit(0.002 * amount);
               } else if ("standard".equals(user.getPlan())) {
                   account.deposit(0.002 * amount);
               } else if ("silver".equals(user.getPlan())) {
                   account.deposit(0.004 * amount);
               } else if ("gold".equals(user.getPlan())) {
                   account.deposit(0.0055 * amount);
               }
           } else if (spent >= 100) {
               if (user.getPlan().equals("student")){
                   account.deposit(0.001 * amount);
               } else if ("standard".equals(user.getPlan())) {
                   account.deposit(0.001 * amount);
               } else if ("silver".equals(user.getPlan())) {
                   account.deposit(0.003 * amount);
               } else if ("gold".equals(user.getPlan())) {
                   account.deposit(0.005 * amount);
               }
           }
           System.out.println("Usserul " + user.getEmail() + "ia cashback si are plan " +  user.getPlan() + " balance " + account.getBalance());
       }
    }
}
