package app.config;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import org.jongo.Jongo;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig implements InitializingBean, DisposableBean {

    @Value("${mongo.host:localhost}")
    private String mongoHost;

    @Value("${mongo.port:17017}")
    private Integer mongoPort;

    @Value("${mongo.db:database}")
    private String mongoDatabase;

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;
    private DB db;

    @Bean
    public Jongo jongo() throws Exception {
        return new Jongo(db);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.V2_2_0_RC0, mongoPort, Network.localhostIsIPv6()));
        mongod = mongodExe.start();

        mongo = new MongoClient(mongoHost, mongoPort);
        mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);

        db = mongo.getDB(mongoDatabase);
    }

    @Override
    public void destroy() throws Exception {
        db.dropDatabase();
        mongo.close();
        mongod.stop();
        mongodExe.cleanup();
    }
}
