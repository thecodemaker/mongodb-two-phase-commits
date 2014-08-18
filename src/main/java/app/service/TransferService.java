package app.service;

import app.domain.Transaction;
import app.domain.TransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("CPD-START")
public class TransferService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    public void transfer(Transaction transaction) {

        transactionService.updateState(transaction.getId(), TransactionState.INITIAL, TransactionState.PENDING);

        accountService.updateBalanceAndPushToPendingTransactions(transaction.getSource(), -transaction.getValue(), transaction.getId());
        accountService.updateBalanceAndPushToPendingTransactions(transaction.getDestination(), transaction.getValue(), transaction.getId());

        transactionService.updateState(transaction.getId(), TransactionState.PENDING, TransactionState.APPLIED);

        accountService.updatePullFromPendingTransactions(transaction.getSource(), transaction.getId());
        accountService.updatePullFromPendingTransactions(transaction.getDestination(), transaction.getId());

        transactionService.updateState(transaction.getId(), TransactionState.APPLIED, TransactionState.DONE);
    }

    public void recoverPending(Transaction transaction) {

        accountService.updateBalanceAndPushToPendingTransactions(transaction.getSource(), -transaction.getValue(), transaction.getId());
        accountService.updateBalanceAndPushToPendingTransactions(transaction.getDestination(), transaction.getValue(), transaction.getId());

        transactionService.updateState(transaction.getId(), TransactionState.PENDING, TransactionState.APPLIED);

        accountService.updatePullFromPendingTransactions(transaction.getSource(), transaction.getId());
        accountService.updatePullFromPendingTransactions(transaction.getDestination(), transaction.getId());

        transactionService.updateState(transaction.getId(), TransactionState.APPLIED, TransactionState.DONE);
    }

    public void recoverApplied(Transaction transaction) {

        accountService.updatePullFromPendingTransactions(transaction.getSource(), transaction.getId());
        accountService.updatePullFromPendingTransactions(transaction.getDestination(), transaction.getId());

        transactionService.updateState(transaction.getId(), TransactionState.APPLIED, TransactionState.DONE);
    }

    public void cancelPending(Transaction transaction) {

        transactionService.updateState(transaction.getId(), TransactionState.PENDING, TransactionState.CANCELING);

        accountService.updateBalanceAndPullFromPendingTransactions(transaction.getSource(), transaction.getValue(), transaction.getId());
        accountService.updateBalanceAndPullFromPendingTransactions(transaction.getDestination(), -transaction.getValue(), transaction.getId());

        transactionService.updateState(transaction.getId(), TransactionState.CANCELING, TransactionState.CANCELED);
    }
}
