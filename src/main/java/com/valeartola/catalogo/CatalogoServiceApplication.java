package com.valeartola.catalogo;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatalogoServiceApplication {

	public static void main(String[] args) {
		// Toda la aplicación trabaja en UTC, como exige el contrato de la cátedra
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(CatalogoServiceApplication.class, args);
	}
}