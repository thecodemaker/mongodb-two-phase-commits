package app.service;

import app.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repository;

    public void insert(String accountId, int balance, Object[] pendingTransactions) {
        repository.insert(accountId, balance, pendingTransactions);
    }

    public void updateBalanceAndPushToPendingTransactions(String accountId, int amount, String transactionId) {
        repository.updateBalanceAndPushToPendingTransactions(accountId, amount, transactionId);
    }

    public void updateBalanceAndPullFromPendingTransactions(String accountId, int amount, String transactionId) {
        repository.updateBalanceAndPullFromPendingTransactions(accountId, amount, transactionId);
    }

    public void updatePullFromPendingTransactions(String accountId, String transactionId) {
        repository.updatePullFromPendingTransactions(accountId, transactionId);
    }
}
