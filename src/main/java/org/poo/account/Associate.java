package org.poo.account;

import lombok.Getter;
import org.poo.bank.User;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Associate {
    private String firstName;
    private String lastName;
    private String email;
    private Map<Integer, Double> spendings;
    private Map<Integer, Double> deposits;

    public Associate(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.spendings = new HashMap<>();
        this.deposits = new HashMap<>();
        this.email =   user.getEmail();
    }

    /**
     * Adds spendings for the associate
     * @param timestamp the timestamp of the spending
     * @param amount the amount of the spending
     */
    public void addSpendings(final int timestamp, final double amount) {
        spendings.put(timestamp, amount);
    }

    /**
     * Adds deposits for the associate
     * @param timestamp the timestamp of the deposit
     * @param amount the amount of the deposit
     */
    public void addDeposits(final int timestamp, final double amount) {
        deposits.put(timestamp, amount);
    }

    /**
     * Calculates the total spent of the associate
     * @param start the start timestamp
     * @param end the end timestamp
     * @return the total spent
     */
    public Double calculateTotalSpent(final int start, final int end) {
        for (Map.Entry<Integer, Double> entry : spendings.entrySet()) {
            if (entry.getKey() >= start && entry.getKey() <= end) {
                return spendings.values().stream().reduce(0.0, Double::sum);
            }
        }
        return 0.0;
    }

    /**
     * Calculates the total deposit of the associate
     * @param start the start timestamp
     * @param end the end timestamp
     * @return the total deposit
     */
    public Double calculateTotalDeposit(final int start, final int end) {
        for (Map.Entry<Integer, Double> entry : deposits.entrySet()) {
            if (entry.getKey() >= start && entry.getKey() <= end) {
                return deposits.values().stream().reduce(0.0, Double::sum);
            }
        }
        return 0.0;
    }

}
