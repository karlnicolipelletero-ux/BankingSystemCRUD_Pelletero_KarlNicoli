package bankingsystem.model;

public class Account {
    private int accountId; 
    private int customerId; 
    private String accountType; 
    private double balance; 

    public Account(int customerId, String accountType, double balance) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
}