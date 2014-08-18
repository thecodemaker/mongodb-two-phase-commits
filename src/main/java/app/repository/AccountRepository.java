package app.repository;

import com.mongodb.WriteResult;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class AccountRepository {

    @Autowired
    private Jongo jongo;

    private MongoCollection accounts;

    @PostConstruct
    private void init() {
        accounts = jongo.getCollection("accounts");
    }

}
