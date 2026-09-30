package com.selahfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SelahFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SelahFinanceApplication.class, args);
	}

}
