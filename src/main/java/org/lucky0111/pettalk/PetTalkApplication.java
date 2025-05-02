package org.lucky0111.pettalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetTalkApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetTalkApplication.class, args);
	}

}