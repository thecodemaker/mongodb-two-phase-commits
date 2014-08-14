package app;

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

public class MongoDBTwoPhaseCommitsTest {

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;
    private DB db;

    private Jongo jongo;
    private MongoCollection accounts;

    public static final int MONGODB_PORT = 17017;
    public static final String MONGODB_HOST = "localhost";
    public static final String DB_NAME = "database";
    public static final String COL_NAME = "accounts";

    @Before
    public void setUp() throws Exception {

        MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.V2_2_0_RC0, MONGODB_PORT, Network.localhostIsIPv6()));
        mongod = mongodExe.start();

        mongo = new MongoClient(MONGODB_HOST, MONGODB_PORT);

        db = mongo.getDB(DB_NAME);
        jongo = new Jongo(db);
        accounts = jongo.getCollection(COL_NAME);
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

        Account account = new Account("A", 1000, new Object[0]);
        accounts.save(account);

        Account retrievedAccount = accounts.findOne().as(Account.class);

        assertThat(account.getBalance(), is(retrievedAccount.getBalance()));
        assertThat(account.getPendingTransactions(), is(retrievedAccount.getPendingTransactions()));
    }

}
