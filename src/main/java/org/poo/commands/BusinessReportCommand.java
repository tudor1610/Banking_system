package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.Associate;
import org.poo.bank.Bank;

import java.util.Map;


public class BusinessReportCommand implements Command {
    private Bank bank;
    private String type;
    private int startTimestamp;
    private int endTimestamp;
    private String IBAN;
    private int timestamp;

    public BusinessReportCommand(Bank bank, String type, int startTimestamp, int endTimestamp, String IBAN, int timestamp) {
        this.bank = bank;
        this.type = type;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.IBAN = IBAN;
        this.timestamp = timestamp;
    }

    private void TransactionReport() {
        Account account = bank.getAccountHashMap().get(IBAN);
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "businessReport");
        command.put("timestamp", timestamp);
        ObjectNode status = bank.getObjectMapper().createObjectNode();
        status.put("IBAN", IBAN);
        status.put("balance", account.getBalance());
        status.put("currency", account.getCurrency());
        status.put("spending limit", account.getSpendingLimit());
        status.put("deposit limit", account.getDepositLimit());
        status.put("total spent", account.getTotalSpent());
        status.put("total deposited", account.getTotalDeposited());
        status.put("statistics type", "transaction");
        ArrayNode managersArray = bank.getObjectMapper().createArrayNode();
        for (Map.Entry<String, Associate> entry : account.getManagers().entrySet()) {
            ObjectNode manager = bank.getObjectMapper().createObjectNode();
                manager.put("username", entry.getValue().getLastName() + " " + entry.getValue().getFirstName());
                manager.put("spent", entry.getValue().calculateTotalSpent(startTimestamp, endTimestamp));
                manager.put("deposited", entry.getValue().calculateTotalDeposit(startTimestamp, endTimestamp));
                managersArray.add(manager);

        }
        status.set("managers", managersArray);
        ArrayNode employeesArray = bank.getObjectMapper().createArrayNode();
        for (Map.Entry<String, Associate> entry : account.getEmployees().entrySet()) {
            ObjectNode employee = bank.getObjectMapper().createObjectNode();
                employee.put("username", entry.getValue().getLastName() + " " + entry.getValue().getFirstName());
                employee.put("spent", entry.getValue().calculateTotalSpent(startTimestamp, endTimestamp));
                employee.put("deposited", entry.getValue().calculateTotalDeposit(startTimestamp, endTimestamp));
                employeesArray.add(employee);
        }
        status.set("employees", employeesArray);
        command.set("output", status);
        bank.getOutput().add(command);
    }




    @Override
    public void execute() {
        switch (type) {
            case "transaction" :
                TransactionReport();
                break;
            case "commerciant":
                break;
        }
    }
}
