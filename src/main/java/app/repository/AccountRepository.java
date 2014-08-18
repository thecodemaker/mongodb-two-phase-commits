package app.repository;

import com.mongodb.WriteResult;
import org.jongo.MongoCollection;

import javax.annotation.PostConstruct;

public class AccountRepository extends AbstractRepository {

    private MongoCollection accounts;

    @PostConstruct
    private void init() {
        accounts = getJongo().getCollection("accounts");
    }

    public int insert(String id, int balance, Object[] pendingTransactions) {
        WriteResult result = accounts.insert(
                "{_id: #, balance: #, pendingTransactions: #}", id, balance, pendingTransactions
        );
        return result.getN();
    }

    public int updateBalanceAndPendingTransactions(String accountId, int amount, String transactionId) {
        WriteResult result = accounts.update(
                "{ _id: #, pendingTransactions: { $ne: #}},", accountId, transactionId
        ).with(
                "{ $inc: { balance: #}, $push: { pendingTransactions: #}}", amount, transactionId
        );
        return  result.getN();
    }

    public int updatePendingTransactions(String accountId, String transactionId) {
        WriteResult result = accounts.update(
                "{ _id: #, pendingTransactions: #},", accountId, transactionId
        ).with(
                "{ $pull: { pendingTransactions: #}}", transactionId
        );
        return result.getN();
    }
}
