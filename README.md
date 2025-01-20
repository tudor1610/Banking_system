<span style="color:gray"><small>Copyright Brandibur Tudor 2024-2025</small></span>

# <span style="color:DarkSlateGray">**Bank Management System**</span>

## <span style="color:DarkSlateGray">**Project Overview**</span>

The **Bank Management System** is a comprehensive **Java** application designed to provide robust functionalities for managing bank accounts, transactions, cards, and user interactions. The system supports diverse account types, seamless currency conversions, online payments, bill splitting, and detailed transaction reporting, ensuring a complete suite of banking services.
## <span style="color:DarkSlateGray">**Key Features**</span>

- Support for multiple account types (classic, savings, business, etc.)
- User and account management capabilities
- Currency exchange and conversion
- Card management (including regular and single-use cards)
- Online payment processing
- Money transfers between accounts
- Bill splitting functionality (equal and custom splits)
- Transaction tracking and spending reports
- Interest rate management for savings accounts
- Business-specific features (e.g., associate accounts, associated cards)
- Commision fees and cashback rewards

## <span style="color:DarkSlateGray">**Project Structure**</span>
The project is organized into well-defined packages and classes for clarity and modularity.
### 1.Main Components

- <span style="color:Teal">**Bank**</span> : Serves as the primary orchestrator, responsible for processing commands and managing system-wide interactions.
- <span style="color:Teal">**User**</span>: Represents individual bank users, tracking their accounts and transactions.

### 2.Supporting Classes

- <span style="color:Teal">**Account**</span>: The base class for bank accounts, extended by `ClassicAccount`, `SavingsAccount` and `BusinessAccount`. It maintains a list of cards associated with the account and a record of all related transactions.
- <span style="color:Teal">**AccountFactory**</span>: A factory class for creating different types of accounts, designed to facilitate the addition of new account types in the future.
- <span style="color:Teal">**Card**</span>: Represents bank cards, with functionality extended by `SingleUseCard`. The `SingleUseCard` class overrides the `regenerateCardNumber()` method to regenerate the card details after each transaction.
- <span style="color:Teal">**Transaction**</span>: Implements the **Builder pattern** to efficiently manage various types of transactions. It formats and outputs transaction details using the `print()` method, tailoTeal to the transaction type.
- <span style="color:Teal">**Associate**</span>: A class that stores the transactions of an employee or manager associated with a business account.
- <span style="color:Teal">**SplitPayment**</span>:Stores all information about a split payment, so that it can be processed after the user confirms the split.

## <span style="color:DarkSlateGray">**Supported Operations**</span>
1. User Management

    - Create and manage users
    - Display user details
    - Upgrade user plan type

2. Account Management

    - Add or delete accounts
    - Configure minimum balance requirements
    - Deposit funds
    - Adjust interest rates for savings accounts
    - Add associate accounts (for business accounts)
    - Set deposit and withdrawal limits (for business accounts)

3. Card Management

    - Create regular and single-use cards
    - Delete cards
    - Check card statuses
    - Process online payments

4. Transactions
   
    - Get commision fees and cashback rewards
    - Transfer funds between accounts
    - Withdraw cash
    - Split bill payments (equal or custom splits)
    - Generate transaction summaries
    - Generate business reports
    - Create detailed spending reports

5. Currency Support

    - Support for multi-currency accounts
    - Currency conversion with real-time exchange rates, including indirect conversions (e.g., USD → RON → EUR)

## <span style="color:DarkSlateGray">**Design Patterns**</span>

The system employs the following design patterns to ensure scalability, maintainability, and efficiency:

- <span style="color:Teal">**Factory Pattern**</span> : Used for account creation through the `AccountFactory` class.
- <span style="color:Teal">**Singleton Pattern**</span>: Ensures a single instance of `AccountFactory` throughout the system.
- <span style="color:Teal">**Builder Pattern**</span>: Simplifies the construction and management of `Transaction` objects.
- <span style="color:Teal">**Command Pattern**</span>: Facilitates the execution of user commands through the `Bank` class. Every command
is encapsulated in a separate class, allowing for easy extension and modification. They are processed by the `executeCommand()` method.

## <span style="color:DarkSlateGray">**Error handling**</span>

The system incorporates robust error-handling mechanisms to address scenarios such as:

- Insufficient account funds
- Invalid account or card operations
- Limitations in currency conversions
- Account-specific restrictions
- Users trying to perform unauthorized actions


Errors are logged systematically and, in certain cases, may trigger the creation of specific transactions to inform users of issues.

## <span style="color:DarkSlateGray">**Extensibility**</span>

The system's modular architecture enables seamless integration of new features, including:

- Additional account types
- Enhanced transaction types
- New card types with custom functionality
- New user roles and permissions
- Other commands and operations