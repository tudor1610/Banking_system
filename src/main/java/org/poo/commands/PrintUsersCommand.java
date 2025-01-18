package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.bank.User;

public class PrintUsersCommand implements Command {
    private final Bank context;
    private final int timestamp;
    public PrintUsersCommand(final Bank context, final int timestam) {
        this.context = context;
        this.timestamp = timestam;
    }

    /**
     * Prints the list of users at the specified timestamp and stores the result in the output.
     *
     */
    @Override
    public void execute() {
            ObjectNode command = context.getObjectMapper().createObjectNode();
            command.put("command", "printUsers");
            command.put("timestamp", timestamp);
            ArrayNode userArray = context.getObjectMapper().createArrayNode();
            for (User user : context.getUsers()) {
                //print to output
                user.print(userArray);
            }
            command.set("output", userArray);
            context.getOutput().add(command);
        }
}
