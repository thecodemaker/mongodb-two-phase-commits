package app.repository;

import app.domain.Transaction;
import app.domain.TransactionState;
import org.jongo.MongoCollection;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class TransactionRepository extends AbstractRepository{

    private MongoCollection transactions;

    @PostConstruct
    private void init() {
        transactions = getJongo().getCollection("transactions");
    }

    public void insert(String transactionId, String source, String destination, int value, TransactionState state) {
        transactions.insert(
                "{ _id: #, source: #, destination: #, value: #, state: #, lastModified: #}", transactionId, source, destination, value, state, System.currentTimeMillis()
        );
    }

    public void updateState(String transactionId, TransactionState fromState, TransactionState toState) {
        transactions.update(
                "{ _id: #, state: #}", transactionId, fromState
        ).with(
                "{$set: { state: #, lastModified: #}}", toState, System.currentTimeMillis()
        );
    }

    public Transaction findTransactionByStateAndLastModified(TransactionState state, long dateThreshold) {
        return transactions.findOne(
               "{state: #, lastModified: {$lt: #}}", state, dateThreshold
        ).as(Transaction.class);
    }
}
