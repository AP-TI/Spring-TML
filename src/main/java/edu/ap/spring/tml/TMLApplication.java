package edu.ap.spring.tml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"edu.ap.spring.redis", 
							   "edu.ap.spring.tml"})
public class TMLApplication {

	public static void main(String[] args) {
		SpringApplication.run(TMLApplication.class, args);
	}
}
