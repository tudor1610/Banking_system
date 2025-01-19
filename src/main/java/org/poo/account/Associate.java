package org.poo.account;

import lombok.Getter;
import org.poo.bank.User;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Associate {
    private String firstName;
    private String lastName;
    private Map<Integer, Double> spendings;
    private Map<Integer, Double> deposits;

    public Associate(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.spendings = new HashMap<>();
        this.deposits = new HashMap<>();
    }

    public void addSpendings(int timestamp, double amount) {
        spendings.put(timestamp, amount);
    }

    public void addDeposits(int timestamp, double amount) {
        deposits.put(timestamp, amount);
    }

    public Double calculateTotalSpent(int start, int end) {
        for (Map.Entry<Integer, Double> entry : spendings.entrySet()) {
            if (entry.getKey() >= start && entry.getKey() <= end) {
                return spendings.values().stream().reduce(0.0, Double::sum);
            }
        }
        return 0.0;
    }

    public Double calculateTotalDeposit(int start, int end) {
        for (Map.Entry<Integer, Double> entry : deposits.entrySet()) {
            if (entry.getKey() >= start && entry.getKey() <= end) {
                return deposits.values().stream().reduce(0.0, Double::sum);
            }
        }
        return 0.0;
    }

}
