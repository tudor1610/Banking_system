package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.Associate;
import org.poo.account.CommerciantSpendings;
import org.poo.bank.Bank;

import java.util.Comparator;
import java.util.List;

public class BusinessReportCommand implements Command {
    private Bank bank;
    private String type;
    private int startTimestamp;
    private int endTimestamp;
    private String iban;
    private int timestamp;

    public BusinessReportCommand(final Bank bank, final String type, final int startTimestamp,
                                 final int endTimestamp, final String iban, final int timestamp) {
        this.bank = bank;
        this.type = type;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.iban = iban;
        this.timestamp = timestamp;
    }

    private void transactionReport() {
        Account account = bank.getAccountHashMap().get(iban);
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "businessReport");
        command.put("timestamp", timestamp);
        ObjectNode status = bank.getObjectMapper().createObjectNode();
        status.put("IBAN", iban);
        status.put("balance", account.getBalance());
        status.put("currency", account.getCurrency());
        status.put("spending limit", account.getSpendingLimit());
        status.put("deposit limit", account.getDepositLimit());
        status.put("total spent", account.getTotalSpent());
        status.put("total deposited", account.getTotalDeposited());
        status.put("statistics type", "transaction");
        ArrayNode managersArray = bank.getObjectMapper().createArrayNode();
        for (Associate associate : account.getManagers()) {
            ObjectNode manager = bank.getObjectMapper().createObjectNode();
                manager.put("username", associate.getLastName() + " " + associate.getFirstName());
                manager.put("spent", associate.calculateTotalSpent(startTimestamp, endTimestamp));
                manager.put("deposited",
                        associate.calculateTotalDeposit(startTimestamp, endTimestamp));
                managersArray.add(manager);

        }
        status.set("managers", managersArray);
        ArrayNode employeesArray = bank.getObjectMapper().createArrayNode();
        for (Associate associate : account.getEmployees()) {
            ObjectNode employee = bank.getObjectMapper().createObjectNode();
                employee.put("username", associate.getLastName() + " " + associate.getFirstName());
                employee.put("spent", associate.calculateTotalSpent(startTimestamp, endTimestamp));
                employee.put("deposited",
                        associate.calculateTotalDeposit(startTimestamp, endTimestamp));
                employeesArray.add(employee);
        }
        status.set("employees", employeesArray);
        command.set("output", status);
        bank.getOutput().add(command);
    }

    /**
     * Create the commerciant report.
     */
    private void commerciantReport() {
        Account account = bank.getAccountHashMap().get(iban);
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "businessReport");
        command.put("timestamp", timestamp);
        ObjectNode status = bank.getObjectMapper().createObjectNode();
        status.put("IBAN", iban);
        status.put("balance", account.getBalance());
        status.put("currency", account.getCurrency());
        status.put("spending limit", account.getSpendingLimit());
        status.put("deposit limit", account.getDepositLimit());
        status.put("statistics type", "commerciant");
        ArrayNode commerciantsArray = bank.getObjectMapper().createArrayNode();
        // get a list of sorted commerciants by name
        List<CommerciantSpendings> sortedCommerciants = account.getCommerciantSpendings().stream()
                .sorted(Comparator.comparing(CommerciantSpendings::getCommerciant))
                .toList();

        for (CommerciantSpendings com : sortedCommerciants) {
            ObjectNode commerciant = bank.getObjectMapper().createObjectNode();
            commerciant.put("commerciant", com.getCommerciant());
            commerciant.put("total received", com.getTotalReceived());
            ArrayNode managersArray = bank.getObjectMapper().createArrayNode();
            for (String manager : com.getManagers().stream()
                    .sorted(Comparator.comparing(String::toString)).toList()) {
                managersArray.add(manager);
            }
            commerciant.set("managers", managersArray);
            ArrayNode employeesArray = bank.getObjectMapper().createArrayNode();
            for (String employee : com.getEmployees().stream()
                    .sorted(Comparator.comparing(String::toString)).toList()) {
                employeesArray.add(employee);
            }
            commerciant.set("employees", employeesArray);
            commerciantsArray.add(commerciant);
        }
        status.set("commerciants", commerciantsArray);
        command.set("output", status);
        bank.getOutput().add(command);
    }



    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        switch (type) {
            case "transaction" :
                transactionReport();
                break;
            case "commerciant":
                commerciantReport();
                break;
            default:
                break;
        }
    }
}
