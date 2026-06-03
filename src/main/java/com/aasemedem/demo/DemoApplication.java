package com.aasemedem.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner generateHash() {
		return args -> {
			org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
			String adminHash = encoder.encode("Admin@123");
			String sellerHash = encoder.encode("Seller@123");
			String buyerHash = encoder.encode("Buyer@123");

			System.out.println("=================================================");
			System.out.println("BCRYPT HASH FOR 'Admin@123' : " + adminHash);
			System.out.println("BCRYPT HASH FOR 'Seller@123': " + sellerHash);
			System.out.println("BCRYPT HASH FOR 'Buyer@123' : " + buyerHash);
			System.out.println("Copy these into your neon DB password_hash column!");
			System.out.println("=================================================");
		};
	}

}
