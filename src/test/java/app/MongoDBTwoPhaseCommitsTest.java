package app;

import app.domain.Account;
import app.domain.Transaction;
import app.domain.TransactionState;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;

public class MongoDBTwoPhaseCommitsTest {

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;
    private DB db;

    private Jongo jongo;
    private MongoCollection accounts;
    private MongoCollection transactions;

    public static final int MONGODB_PORT = 17017;

    @Before
    public void setUp() throws Exception {

        MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.V2_2_0_RC0, MONGODB_PORT, Network.localhostIsIPv6()));
        mongod = mongodExe.start();

        mongo = new MongoClient("localhost", MONGODB_PORT);

        db = mongo.getDB("database");
        jongo = new Jongo(db);
        accounts = jongo.getCollection("accounts");
        transactions = jongo.getCollection("transactions");
    }

    @After
    public void tearDown() throws Exception {
        db.dropDatabase();
        mongo.close();
        mongod.stop();
        mongodExe.cleanup();
    }

    @Test
    public void testBasicSetup() throws Exception {

        accounts.insert(
                "{_id: \"A\", balance: 1000, pendingTransactions: []}"
        );

        Account retrievedAccount = accounts.findOne().as(Account.class);

        assertThat(retrievedAccount.getBalance(), is(1000));
        assertThat(retrievedAccount.getPendingTransactions(), is(new Object[0]));
    }

    @Test
    public void testTwoPhaseCommit() throws Exception {

        accounts.insert(
            "[" +
            "     { _id: \"A\", balance: 1000, pendingTransactions: [] },\n" +
            "     { _id: \"B\", balance: 1000, pendingTransactions: [] }\n" +
            "]"
        );

        transactions.insert(
            "{ _id: \"1\", source: \"A\", destination: \"B\", value: 100, state: #, lastModified: #}", TransactionState.INITIAL, System.currentTimeMillis()
        );

        //Retrieve the transaction to start.
        Transaction transaction = transactions.findOne().as(Transaction.class);

        //Update transaction state to pending.
        transactions.update(
             "{ _id: #, state: #}", transaction.getId(), TransactionState.INITIAL
        ).with(
             "{$set: { state: #, lastModified: #}}", TransactionState.PENDING, System.currentTimeMillis()
        );

        //Apply the transaction to both accounts.
        accounts.update(
             "{ _id: #, pendingTransactions: { $ne: #}},", transaction.getSource(), transaction.getId()
        ).with(
                "{ $inc: { balance: #}, $push: { pendingTransactions: #}}", -transaction.getValue(), transaction.getId()
        );

        accounts.update(
             "{ _id: #, pendingTransactions: { $ne: #}},", transaction.getDestination(), transaction.getId()
        ).with(
             "{ $inc: { balance: #}, $push: { pendingTransactions: #}}", transaction.getValue(), transaction.getId()
        );

        //Update transaction state to applied.
        transactions.update(
                "{ _id: #, state: #}", transaction.getId(), TransactionState.PENDING
        ).with(
                "{$set: { state: #, lastModified: #}}", TransactionState.APPLIED, System.currentTimeMillis()
        );

        //Update both accounts’ list of pending transactions.
        accounts.update(
                "{ _id: #, pendingTransactions: #},", transaction.getSource(), transaction.getId()
        ).with(
                "{ $pull: { pendingTransactions: #}}",transaction.getId()
        );

        accounts.update(
                "{ _id: #, pendingTransactions: #},", transaction.getDestination(), transaction.getId()
        ).with(
                "{ $pull: { pendingTransactions: #}}",transaction.getId()
        );

        //Update transaction state to done.
        transactions.update(
                "{ _id: #, state: #}", transaction.getId(), TransactionState.APPLIED
        ).with(
                "{$set: { state: #, lastModified: #}}", TransactionState.DONE, System.currentTimeMillis()
        );

        Account accountA = accounts.findOne("{_id: \"A\"}").as(Account.class);
        assertThat(accountA.getBalance(), is(900));
        assertThat(accountA.getPendingTransactions(), is(emptyArray()));

        Account accountB = accounts.findOne("{_id: \"B\"}").as(Account.class);
        assertThat(accountB.getBalance(), is(1100));
        assertThat(accountB.getPendingTransactions(), is(emptyArray()));

        Transaction finalTransaction = transactions.findOne().as(Transaction.class);
        assertThat(finalTransaction.getState(), is(TransactionState.DONE));
    }
}
