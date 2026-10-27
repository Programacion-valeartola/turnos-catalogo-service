package com.valeartola.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "catalog.sync.on-startup=false")
class CatalogoServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
