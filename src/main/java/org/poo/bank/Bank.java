package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.commands.Command;
import org.poo.commands.AcceptSplitPaymentCommand;
import org.poo.commands.AddAccountCommand;
import org.poo.commands.AddFundsCommand;
import org.poo.commands.AddInterestCommand;
import org.poo.commands.NewBusinessAssociateCommand;
import org.poo.commands.BusinessReportCommand;
import org.poo.commands.CashWithdrawalCommand;
import org.poo.commands.ChangeDepositLimitCommand;
import org.poo.commands.ChangeInterestCommand;
import org.poo.commands.ChangeSpendingLimitCommand;
import org.poo.commands.CheckCardStatusCommand;
import org.poo.commands.CreateCardCommand;
import org.poo.commands.CreateSingleCardCommand;
import org.poo.commands.DeleteAccountCommand;
import org.poo.commands.DeleteCardCommand;
import org.poo.commands.PayOnlineCommand;
import org.poo.commands.PrintTransactionsCommand;
import org.poo.commands.PrintUsersCommand;
import org.poo.commands.ReportCommand;
import org.poo.commands.RejectSplitPaymentCommand;
import org.poo.commands.SendMoneyCommand;
import org.poo.commands.SetAliasCommand;
import org.poo.commands.SetMinBalanceCommand;
import org.poo.commands.SpendingsReportCommand;
import org.poo.commands.SplitPaymentCommand;
import org.poo.commands.UpgradePlanCommand;
import org.poo.commands.WithdrawSavingsCommand;


import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.CommandInput;
import org.poo.fileio.UserInput;
import org.poo.fileio.CommerciantInput;
import org.poo.transactions.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class Bank {
    private static Bank bank = null;
    private ArrayList<User> users;
    private ExchangeInput[] exchangeRates;
    private CommandInput[] commands;
    private Map<String, CommerciantInput> commerciants;
    private ObjectMapper objectMapper;
    private Map<String, User> userHashMap;
    private Map<String, Account> accountHashMap;
    private Map<String, Card> cardHashMap;
    private Map<String, Account> aliasHashMap;
    private List<SplitPayment> waitingPayments;
    private ArrayNode output;

    private enum Commands {
        PRINTUSERS("printUsers"),
        ADDACCOUNT("addAccount"),
        CREATECARD("createCard"),
        CREATEONETIMECARD("createOneTimeCard"),
        ADDFUNDS("addFunds"),
        DELETEACCOUNT("deleteAccount"),
        DELETECARD("deleteCard"),
        SETMINBALANCE("setMinimumBalance"),
        PAYONLINE("payOnline"),
        SENDMONEY("sendMoney"),
        SETALIAS("setAlias"),
        PRINTTRANSACTIONS("printTransactions"),
        CHECKCARDSTATUS("checkCardStatus"),
        SPLITPAYMENT("splitPayment"),
        REPORT("report"),
        SPENDINGSREPORT("spendingsReport"),
        CHANGEINTERESTRATE("changeInterestRate"),
        ADDINTEREST("addInterest"),
        WITHDRAWSAVINGS("withdrawSavings"),
        UPGRADEPLAN("upgradePlan"),
        CASHWITHDRAWAL("cashWithdrawal"),
        ACCEPTSPLITPAYMENT("acceptSplitPayment"),
        REJECTSPLITPAYMENT("rejectSplitPayment"),
        ADDNEWBUSINESSASSOCIATE("addNewBusinessAssociate"),
        CHANGESPENDLIMIT("changeSpendingLimit"),
        CHANGEDOPOSITLIMIT("changeDepositLimit"),
        BUSINESSREPORT("businessReport");

        private final String commands;

        Commands(final String command) {
            this.commands = command;
        }

        public static Commands fromString(final String command) {
            for (Commands cmd : Commands.values()) {
                if (cmd.commands.equalsIgnoreCase(command)) {
                    return cmd;
                }
            }
            throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    public Bank(final ObjectInput objectInput, final ArrayNode output) {
        users = new ArrayList<>();
        for (UserInput userInput : objectInput.getUsers()) {
            User user = new User(userInput);
            users.add(user);
        }
        exchangeRates = objectInput.getExchangeRates();
        commands = objectInput.getCommands();
        commerciants = new HashMap<>();
        for (CommerciantInput commerciant : objectInput.getCommerciants()) {
            commerciants.put(commerciant.getCommerciant(), commerciant);
        }
        objectMapper = new ObjectMapper();
        userHashMap = new HashMap<>();
        cardHashMap = new HashMap<>();
        accountHashMap = new HashMap<>();
        aliasHashMap = new HashMap<>();
        waitingPayments = new ArrayList<>();
        for (User user : this.users) {
            userHashMap.put(user.getEmail(), user);
        }
        this.output = output;
    }

    /**
     * Processes a list of commands and performs appropriate actions
     *
     * <p>The method iterates through a list of commands and executes corresponding operations
     * such as adding accounts, creating cards, making payments, generating reports, and more.
     * It handles unrecognized commands gracefully by logging a message and catches invalid commands
     * using an {@link IllegalArgumentException}.</p>
     *
     */
    public void commandProcessor() {
        for (CommandInput command : commands) {
            try {
                Commands c = Commands.fromString(command.getCommand());
                switch (c) {
                    case PRINTUSERS -> executeCommand(new PrintUsersCommand(this,
                            command.getTimestamp()));
                    case ADDACCOUNT -> executeCommand(new AddAccountCommand(this,
                            command.getEmail(), command.getCurrency(), command.getAccountType(),
                            command.getInterestRate(), command.getTimestamp()));
                    case CREATECARD -> executeCommand(new CreateCardCommand(this,
                            command.getAccount(), command.getEmail(), command.getTimestamp()));
                    case CREATEONETIMECARD -> executeCommand(new CreateSingleCardCommand(this,
                            command.getAccount(), command.getEmail(), command.getTimestamp()));
                    case ADDFUNDS -> executeCommand(new AddFundsCommand(this, command.getAccount(),
                            command.getAmount(), command.getTimestamp(), command.getEmail()));
                    case DELETEACCOUNT -> executeCommand(new DeleteAccountCommand(this,
                            command.getTimestamp(), command.getEmail(), command.getAccount()));
                    case DELETECARD -> executeCommand(new DeleteCardCommand(this,
                            command.getCardNumber(), command.getTimestamp(), command.getEmail()));
                    case SETMINBALANCE -> executeCommand(new SetMinBalanceCommand(this,
                            command.getMinBalance(), command.getAccount()));
                    case PAYONLINE -> executeCommand(new PayOnlineCommand(this,
                            command.getCardNumber(), command.getAmount(), command.getCurrency(),
                            command.getDescription(), command.getCommerciant(), command.getEmail(),
                            exchangeRates, command.getTimestamp()));
                    case SENDMONEY -> executeCommand(new SendMoneyCommand(this,
                            command.getAccount(), command.getReceiver(), command.getAmount(),
                            exchangeRates, command.getDescription(), command.getEmail(),
                            command.getTimestamp()));
                    case SETALIAS -> executeCommand(new SetAliasCommand(this,
                            command.getAlias(), command.getAccount()));
                    case PRINTTRANSACTIONS -> executeCommand(new PrintTransactionsCommand(this,
                            command.getEmail(), command.getTimestamp()));
                    case CHECKCARDSTATUS -> executeCommand(new CheckCardStatusCommand(this,
                            command.getCardNumber(), command.getTimestamp()));
                    case SPLITPAYMENT -> executeCommand(new SplitPaymentCommand(this,
                            command.getAccounts(), command.getTimestamp(), command.getCurrency(),
                            command.getAmount(), command.getSplitPaymentType(),
                            command.getAmountForUsers()));
                    case REPORT -> executeCommand(new ReportCommand(this,
                            command.getStartTimestamp(), command.getEndTimestamp(),
                            command.getAccount(), command.getTimestamp()));
                    case SPENDINGSREPORT -> executeCommand(new SpendingsReportCommand(this,
                            command.getStartTimestamp(), command.getEndTimestamp(),
                            command.getAccount(), command.getTimestamp()));
                    case CHANGEINTERESTRATE -> executeCommand(new ChangeInterestCommand(this,
                            command.getAccount(), command.getInterestRate(),
                            command.getTimestamp()));
                    case ADDINTEREST -> executeCommand(new AddInterestCommand(this,
                            command.getAccount(), command.getTimestamp()));
                    case WITHDRAWSAVINGS -> executeCommand(new WithdrawSavingsCommand(this,
                            command.getAccount(), command.getAmount(), command.getCurrency(),
                            command.getTimestamp()));
                    case UPGRADEPLAN -> executeCommand(new UpgradePlanCommand(this,
                            command.getNewPlanType(), command.getAccount(),
                            command.getTimestamp()));
                    case CASHWITHDRAWAL -> executeCommand(new CashWithdrawalCommand(this,
                            command.getCardNumber(), command.getAmount(), command.getEmail(),
                            command.getLocation(), command.getTimestamp()));
                    case ACCEPTSPLITPAYMENT -> executeCommand(new AcceptSplitPaymentCommand(this,
                            command.getEmail(), command.getTimestamp(),
                            command.getSplitPaymentType()));
                    case REJECTSPLITPAYMENT -> executeCommand(new RejectSplitPaymentCommand(this,
                            command.getEmail(), command.getTimestamp(),
                            command.getSplitPaymentType()));
                    case ADDNEWBUSINESSASSOCIATE -> executeCommand(
                            new NewBusinessAssociateCommand(this, command.getAccount(),
                                    command.getRole(), command.getEmail(), command.getTimestamp()));
                    case CHANGESPENDLIMIT -> executeCommand(new ChangeSpendingLimitCommand(this,
                            command.getEmail(), command.getAccount(), command.getAmount(),
                            command.getTimestamp()));
                    case CHANGEDOPOSITLIMIT -> executeCommand(new ChangeDepositLimitCommand(this,
                            command.getAccount(),
                            command.getEmail(), command.getAmount(), command.getTimestamp()));
                    case BUSINESSREPORT -> executeCommand(new BusinessReportCommand(this,
                            command.getType(), command.getStartTimestamp(),
                            command.getEndTimestamp(), command.getAccount(),
                            command.getTimestamp()));
                    default -> System.out.println("Unrecognized command: " + command);
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Adds a transaction associated with a newly created card.
     *
     * @param account    the account associated with the card
     * @param timestamp  the timestamp of the transaction
     * @param cardNumber the card number of the newly created card
     */
    public void addCardTransaction(final Account account, final int timestamp,
                                    final String cardNumber) {
        Transaction t = new Transaction.Builder(timestamp, "New card created")
                .accountNumber(account.getIban())
                .cardNumber(cardNumber)
                .cardHolder(account.getEmail())
                .build();
        userHashMap.get(account.getEmail()).addTransaction(t);
        account.accountAddTransaction(t);
    }

    /**
     * Prepares a mapping of currency exchange rates for conversion between currencies.
     *
     * @return a map where each key is a currency, and the value is a map of exchange
     *          rates to other currencies
     */
    public Map<String, Map<String, Double>> prepareExchangeRates() {
        Map<String, Map<String, Double>> exchangeRate = new HashMap<>();
        for (ExchangeInput rate : exchangeRates) {
            exchangeRate.putIfAbsent(rate.getFrom(), new HashMap<>());
            exchangeRate.putIfAbsent(rate.getTo(), new HashMap<>());
            exchangeRate.get(rate.getFrom()).put(rate.getTo(), rate.getRate());
            exchangeRate.get(rate.getTo()).put(rate.getFrom(), 1 / rate.getRate());
        }
        return exchangeRate;
    }

    /**
     * Converts an amount from one currency to another using a provided map of exchange rates.
     * Supports direct conversion or indirect conversion through an intermediate currency.
     *
     * @param amount        the amount to convert
     * @param fromCurrency  the currency to convert from
     * @param toCurrency    the currency to convert to
     * @param exc a map of exchange rates between currencies
     * @return the converted amount, or null if no conversion path exists
     */
    public Double convertCurrency(final double amount, final String fromCurrency,
                                   final String toCurrency,
                                   final Map<String, Map<String, Double>> exc) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        Map<String, Double> ratesFrom = exc.get(fromCurrency);
        if (ratesFrom != null && ratesFrom.containsKey(toCurrency)) {
            return amount * ratesFrom.get(toCurrency);
        }
        assert ratesFrom != null;
        for (Map.Entry<String, Double> entry : ratesFrom.entrySet()) {
            String intermediateCurrency = entry.getKey();
            if (exc.containsKey(intermediateCurrency) && exc
                    .get(intermediateCurrency).containsKey(toCurrency)) {
                double intermediateRate = entry.getValue();
                double finalRate = exc.get(intermediateCurrency).get(toCurrency);
                return amount * intermediateRate * finalRate;
            }
        }
        return null;
    }

    /**
     * Creates an error response for when an account is not found,
     * typically used for reporting commands.
     *
     * @param timestamp the timestamp of the command execution
     * @param input     the type of command (e.g., "report") that triggered the error
     * @return an ObjectNode containing the error message
     */
    public ObjectNode accountError(final int timestamp, final String input) {
        ObjectNode command = objectMapper.createObjectNode();
        command.put("command", input);
        command.put("timestamp", timestamp);
        ObjectNode status = objectMapper.createObjectNode();
        status.put("description", "Account not found");
        status.put("timestamp", timestamp);
        command.set("output", status);
        return command;
    }

    /**
     * Adds a payment to the list of waiting payments.
     */
    public void addWaitingPayment(final SplitPayment payment) {
        waitingPayments.add(payment);
    }

    /**
     * Removes a payment from the list of waiting payments.
     *
     * @param payment the payment to remove
     */
    public void removeWaitingPayment(final SplitPayment payment) {
        waitingPayments.remove(payment);
    }

    /***
     * Executes a specific command
     * @param command the type of command that has to execute
     */
    public void executeCommand(final Command command) {
        command.execute();
    }
}
