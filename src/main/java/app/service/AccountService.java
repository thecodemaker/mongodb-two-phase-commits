package app.service;

import app.exception.ApplicationException;
import app.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountService {

    @Autowired
    private AccountRepository repository;

    public void insert(String accountId, int balance, Object[] pendingTransactions) {
        int result = repository.insert(accountId, balance, pendingTransactions);
        if (result != 1) {
            throw new ApplicationException("Expected result is 1, returned " + result);
        }
    }

    public void updateBalanceAndPendingTransactions(String accountId, int amount, String transactionId) {
        int result = repository.updateBalanceAndPendingTransactions(accountId, amount, transactionId);
        if (result != 1) {
            throw new ApplicationException("Expected result is 1, returned " + result);
        }
    }

    public void updatePendingTransactions(String accountId, String transactionId) {
        int result = repository.updatePendingTransactions(accountId, transactionId);
        if (result != 1) {
            throw new ApplicationException("Expected result is 1, returned " + result);
        }
    }
}
