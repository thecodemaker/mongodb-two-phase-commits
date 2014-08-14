package app;

public class Account extends Entity {

    private Integer balance;
    private Object[] pendingTransactions;

    public Account() {

    }

    public Account(String id, Integer balance, Object[] pendingTransactions) {
        super(id);
        this.balance = balance;
        this.pendingTransactions = pendingTransactions;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Object[] getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(Object[] pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }
}
