package app.repository;

import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * User: Bogdan Apetrei
 * Date: 18.08.2014
 * Time: 16:40
 */
public class AbstractRepository {

    @Autowired
    private Jongo jongo;

    public Jongo getJongo() {
        return jongo;
    }
}
