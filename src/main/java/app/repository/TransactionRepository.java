package app.repository;

import app.domain.TransactionState;
import com.mongodb.WriteResult;
import org.jongo.MongoCollection;

import javax.annotation.PostConstruct;

public class TransactionRepository extends AbstractRepository{

    private MongoCollection transactions;

    @PostConstruct
    private void init() {
        transactions = getJongo().getCollection("transactions");
    }

    public int insert(String transactionId, String source, String destination, int value, TransactionState state) {
        WriteResult result = transactions.insert(
                "{ _id: #, source: #, destination: #, value: #, state: #, lastModified: #}", transactionId, source, destination, value, state, System.currentTimeMillis()
        );
        return result.getN();
    }

    public int updateState(int transactionId, TransactionState fromState, TransactionState toState) {
        WriteResult result = transactions.update(
                "{ _id: #, state: #}", transactionId, fromState
        ).with(
                "{$set: { state: #, lastModified: #}}", toState, System.currentTimeMillis()
        );
        return result.getN();
    }

}
