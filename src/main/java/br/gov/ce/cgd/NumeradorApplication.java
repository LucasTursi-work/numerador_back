package br.gov.ce.cgd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication()
public class NumeradorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NumeradorApplication.class, args);
	}

}
