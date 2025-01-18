package org.poo.commands;

import org.poo.account.Account;
import org.poo.bank.Bank;

public class SetAliasCommand implements Command {
    private Bank bank;
    private String alias;
    private String account;

    public SetAliasCommand(final Bank bank, final String alias, final String account) {
        this.bank = bank;
        this.alias = alias;
        this.account = account;

    }

    /**
     * Associates an alias with a specific account, allowing the account to be referenced
     * by the alias in subsequent operations.
     *
     */
    @Override
    public void execute() {
        Account account1 = bank.getAccountHashMap().get(this.account);
        bank.getAliasHashMap().put(alias, account1);
    }
}
