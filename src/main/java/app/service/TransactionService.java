package app.service;

import app.domain.Transaction;
import app.domain.TransactionState;
import app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository repository;

    private long MINUTES_IN_MILLISECONDS = 30 * 60 * 1000;

    public void insert(String transactionId, String source, String destination, int value, TransactionState state) {
        repository.insert(transactionId, source, destination, value, state);
    }

    public void updateState(String transactionId, TransactionState fromState, TransactionState toState) {
        repository.updateState(transactionId, fromState, toState);
    }

    public Transaction findTransactionByStateAndLastModified(TransactionState state) {
        long dateThreshold = System.currentTimeMillis() - MINUTES_IN_MILLISECONDS;

        return repository.findTransactionByStateAndLastModified(state, dateThreshold);
    }
}
