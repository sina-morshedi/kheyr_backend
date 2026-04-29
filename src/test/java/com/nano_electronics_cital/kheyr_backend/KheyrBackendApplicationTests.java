package com.nano_electronics_cital.kheyr_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.data.mongodb.auto-index-creation=false"
})
class KheyrBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
