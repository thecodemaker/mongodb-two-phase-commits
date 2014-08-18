package app.repository;

import app.config.ApplicationConfig;
import org.jongo.Jongo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationConfig.class})
public class AccountRepositoryTest {

    @Autowired
    private Jongo jongo;

    @Before
    public void setUp() throws Exception {
        System.out.println(jongo.getDatabase());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testName() throws Exception {
        System.out.println(jongo.getDatabase());
    }
}