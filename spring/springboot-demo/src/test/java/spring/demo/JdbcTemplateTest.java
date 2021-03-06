package spring.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import spring.demo.service.IUserService;

import javax.annotation.Resource;

/**
 * Created by facheng on 16.03.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:0")
public class JdbcTemplateTest {

    @Resource
    private IUserService userService;

    @Before
    public void setUp() {
        userService.deleteAllUsers();
    }

    @Test

    public void test() throws Exception {

    }
}
