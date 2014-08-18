package app.repository;

import org.jongo.MongoCollection;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class AccountRepository extends AbstractRepository {

    private MongoCollection accounts;

    @PostConstruct
    private void init() {
        accounts = getJongo().getCollection("accounts");
    }

    public void insert(String id, int balance, Object[] pendingTransactions) {
        accounts.insert(
                "{_id: #, balance: #, pendingTransactions: #}", id, balance, pendingTransactions
        );
    }

    public void updateBalanceAndPushToPendingTransactions(String accountId, int amount, String transactionId) {
        accounts.update(
                "{ _id: #, pendingTransactions: { $ne: #}},", accountId, transactionId
        ).with(
                "{ $inc: { balance: #}, $push: { pendingTransactions: #}}", amount, transactionId
        );
    }

    public void updateBalanceAndPullFromPendingTransactions(String accountId, int amount, String transactionId) {
        accounts.update(
                "{ _id: #, pendingTransactions: #},", accountId, transactionId
        ).with(
                "{ $inc: { balance: #}, $pull: { pendingTransactions: #}}", amount, transactionId
        );
    }

    public void updatePullFromPendingTransactions(String accountId, String transactionId) {
        accounts.update(
                "{ _id: #, pendingTransactions: #},", accountId, transactionId
        ).with(
                "{ $pull: { pendingTransactions: #}}", transactionId
        );
    }
}
