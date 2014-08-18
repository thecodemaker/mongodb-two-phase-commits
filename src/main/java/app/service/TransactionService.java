package app.service;

import app.domain.TransactionState;
import app.exception.ApplicationException;
import app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionService {

    @Autowired
    private TransactionRepository repository;

    public void insert(String transactionId, String source, String destination, int value, TransactionState state) {
        int result = repository.insert(transactionId, source, destination, value, state);
        if (result != 1) {
            throw new ApplicationException("Expected result is 1, returned " + result);
        }
    }

    public void updateState(int transactionId, TransactionState fromState, TransactionState toState) {
        int result = repository.updateState(transactionId, fromState, toState);
        if (result != 1) {
            throw new ApplicationException("Expected result is 1, returned " + result);
        }
    }
}
