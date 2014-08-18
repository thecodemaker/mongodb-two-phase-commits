package app.repository;

import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractRepository {

    @Autowired
    private Jongo jongo;

    public Jongo getJongo() {
        return jongo;
    }
}
