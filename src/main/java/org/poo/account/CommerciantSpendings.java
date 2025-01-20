package org.poo.account;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommerciantSpendings {
    private Double totalReceived;
    private String commerciant;
    private List<String> managers;
    private List<String> employees;

    public CommerciantSpendings(final String commerciant) {
        this.commerciant = commerciant;
        totalReceived = 0.0;
        managers = new ArrayList<>();
        employees = new ArrayList<>();
    }

    /**
     * Adds an entry to the commerciant spendings
     * @param username the username of the user
     * @param amount the amount of the entry
     * @param isManager true if the user is a manager, false otherwise
     */
    public void addEntry(final String  username, final double amount, final boolean isManager) {
        totalReceived += amount;
        if (isManager) {
            managers.add(username);
        } else {
            employees.add(username);
        }
    }

}
