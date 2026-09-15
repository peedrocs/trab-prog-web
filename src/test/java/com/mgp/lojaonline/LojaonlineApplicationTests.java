package com.mgp.lojaonline;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// sobe o MySQL do compose.yaml tambem durante o teste
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
class LojaonlineApplicationTests {

    @Test
    void contextLoads() {
    }
}
