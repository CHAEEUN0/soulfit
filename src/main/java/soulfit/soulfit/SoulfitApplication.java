package soulfit.soulfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SoulfitApplication {
	public static void main(String[] args) {
		SpringApplication.run(SoulfitApplication.class, args);

	}

}
