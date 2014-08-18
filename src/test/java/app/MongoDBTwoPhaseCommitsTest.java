package app;

import app.config.ApplicationConfig;
import app.domain.Account;
import app.domain.Transaction;
import app.domain.TransactionState;
import app.service.AccountService;
import app.service.TransactionService;
import app.service.TransferService;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationConfig.class})
public class MongoDBTwoPhaseCommitsTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private Jongo jongo;

    private MongoCollection accounts;
    private MongoCollection transactions;

    private long HOUR_IN_MILLISECONDS = 60 * 60 * 1000;

    @Before
    public void setUp() throws Exception {
        accounts = jongo.getCollection("accounts");
        transactions = jongo.getCollection("transactions");
    }

    @After
    public void tearDown() throws Exception {
        accounts.drop();
        transactions.drop();
    }

    @Test
    public void testBasicSetup() throws Exception {

        accountService.insert("A", 1000, new Object[0]);

        Account retrievedAccount = accounts.findOne().as(Account.class);

        assertThat(retrievedAccount.getBalance(), is(1000));
        assertThat(retrievedAccount.getPendingTransactions(), is(new Object[0]));
    }

    @Test
    public void testInitialVersion() throws Exception {

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

    @Test
    public void testRefactoredVersion() throws Exception {

        accounts.insert(
            "[" +
            "     { _id: \"A\", balance: 1000, pendingTransactions: [] },\n" +
            "     { _id: \"B\", balance: 1000, pendingTransactions: [] }\n" +
            "]"
        );

        transactions.insert(
            "{ _id: \"1\", source: \"A\", destination: \"B\", value: 100, state: #, lastModified: #}", TransactionState.INITIAL, System.currentTimeMillis()
        );

        Transaction transaction = transactions.findOne().as(Transaction.class);

        transferService.transfer(transaction);

        Account accountA = accounts.findOne("{_id: \"A\"}").as(Account.class);
        assertThat(accountA.getBalance(), is(900));
        assertThat(accountA.getPendingTransactions(), is(emptyArray()));

        Account accountB = accounts.findOne("{_id: \"B\"}").as(Account.class);
        assertThat(accountB.getBalance(), is(1100));
        assertThat(accountB.getPendingTransactions(), is(emptyArray()));

        Transaction finalTransaction = transactions.findOne().as(Transaction.class);
        assertThat(finalTransaction.getState(), is(TransactionState.DONE));
    }

    @Test
    public void testRecoverPendingState() throws Exception {
        accounts.insert(
           "[" +
           "     { _id: \"A\", balance: 900, pendingTransactions: [\"1\"] },\n" +
           "     { _id: \"B\", balance: 1000, pendingTransactions: [] }\n" +
           "]"
        );

        transactions.insert(
            "{ _id: \"1\", source: \"A\", destination: \"B\", value: 100, state: #, lastModified: #}", TransactionState.PENDING, System.currentTimeMillis() - HOUR_IN_MILLISECONDS
        );

        Transaction transaction = transactionService.findTransactionByStateAndLastModified(TransactionState.PENDING);

        transferService.recoverPending(transaction);

        Account accountA = accounts.findOne("{_id: \"A\"}").as(Account.class);
        assertThat(accountA.getBalance(), is(900));
        assertThat(accountA.getPendingTransactions(), is(emptyArray()));

        Account accountB = accounts.findOne("{_id: \"B\"}").as(Account.class);
        assertThat(accountB.getBalance(), is(1100));
        assertThat(accountB.getPendingTransactions(), is(emptyArray()));

        Transaction finalTransaction = transactions.findOne().as(Transaction.class);
        assertThat(finalTransaction.getState(), is(TransactionState.DONE));
    }

    @Test
    public void testRecoverAppliedState() throws Exception {
        accounts.insert(
                "[" +
                "     { _id: \"A\", balance: 900, pendingTransactions: [\"1\"] },\n" +
                "     { _id: \"B\", balance: 1100, pendingTransactions: [\"1\"] }\n" +
                "]"
        );

        transactions.insert(
                "{ _id: \"1\", source: \"A\", destination: \"B\", value: 100, state: #, lastModified: #}", TransactionState.APPLIED, System.currentTimeMillis() - HOUR_IN_MILLISECONDS
        );

        Transaction transaction = transactionService.findTransactionByStateAndLastModified(TransactionState.APPLIED);

        transferService.recoverApplied(transaction);

        Account accountA = accounts.findOne("{_id: \"A\"}").as(Account.class);
        assertThat(accountA.getBalance(), is(900));
        assertThat(accountA.getPendingTransactions(), is(emptyArray()));

        Account accountB = accounts.findOne("{_id: \"B\"}").as(Account.class);
        assertThat(accountB.getBalance(), is(1100));
        assertThat(accountB.getPendingTransactions(), is(emptyArray()));

        Transaction finalTransaction = transactions.findOne().as(Transaction.class);
        assertThat(finalTransaction.getState(), is(TransactionState.DONE));
    }


    @Test
    public void testCancelPending() throws Exception {

        accounts.insert(
            "[" +
            "     { _id: \"A\", balance: 900, pendingTransactions: [\"1\"] },\n" +
            "     { _id: \"B\", balance: 1100, pendingTransactions: [\"1\"] }\n" +
            "]"
        );

        transactions.insert(
                "{ _id: \"1\", source: \"A\", destination: \"B\", value: 100, state: #, lastModified: #}", TransactionState.PENDING, System.currentTimeMillis() - HOUR_IN_MILLISECONDS
        );

        Transaction transaction = transactionService.findTransactionByStateAndLastModified(TransactionState.PENDING);

        transferService.cancelPending(transaction);

        Account accountA = accounts.findOne("{_id: \"A\"}").as(Account.class);
        assertThat(accountA.getBalance(), is(1000));
        assertThat(accountA.getPendingTransactions(), is(emptyArray()));

        Account accountB = accounts.findOne("{_id: \"B\"}").as(Account.class);
        assertThat(accountB.getBalance(), is(1000));
        assertThat(accountB.getPendingTransactions(), is(emptyArray()));

        Transaction finalTransaction = transactions.findOne().as(Transaction.class);
        assertThat(finalTransaction.getState(), is(TransactionState.CANCELED));
    }

}
