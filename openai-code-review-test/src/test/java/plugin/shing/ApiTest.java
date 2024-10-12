package plugin.shing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author shing
 */
@SpringBootTest(classes = {Application.class})
class ApiTest {

    @Test
    void test() {
        System.out.println("api test");
    }

}
