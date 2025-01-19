package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.transactions.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintTransactionsCommand implements Command {
    private Bank bank;
    private String email;
    private int timestamp;

    public PrintTransactionsCommand(final Bank bank, final String email, final int timestamp) {
        this.bank = bank;
        this.email = email;
        this.timestamp = timestamp;
    }

    /**
     * Prints the list of all transactions for a specific user at the given timestamp
     * and adds the result to the output.
     *
     */
    @Override
    public void execute() {
        ObjectNode command = bank.getObjectMapper().createObjectNode();
        command.put("command", "printTransactions");
        ArrayNode transactionArray = bank.getObjectMapper().createArrayNode();
        User user = bank.getUserHashMap().get(email);

        List<Transaction> sortedTransactions = user.getTransactions().stream()
                .sorted(Comparator.comparingInt(Transaction::getTimestamp))
                .toList();
        for (Transaction t : sortedTransactions) {
            ObjectNode transaction = bank.getObjectMapper().createObjectNode();
            t.print(transaction);
            transactionArray.add(transaction);
        }
        command.set("output", transactionArray);
        command.put("timestamp", timestamp);
        bank.getOutput().add(command);
    }
}
