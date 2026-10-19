package com.sumith.collabpad;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class CollabpadApplication {

	public static void main(String[] args) {
		// Load root .env if present
		if (new File("../.env").exists()) {
			Dotenv rootDotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
			rootDotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		}
		// Load local .env if present (takes priority)
		if (new File(".env").exists()) {
			Dotenv localDotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
			localDotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		}

		// Ensure DB_URL has jdbc: prefix required by Spring Boot JDBC driver
		String dbUrl = System.getProperty("DB_URL");
		if (dbUrl == null || dbUrl.isBlank()) {
			dbUrl = System.getenv("DB_URL");
		}
		if (dbUrl != null && dbUrl.startsWith("postgresql://")) {
			System.setProperty("DB_URL", "jdbc:" + dbUrl);
		}

		SpringApplication.run(CollabpadApplication.class, args);
	}

}
