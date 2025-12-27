package nomadia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NomadiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(NomadiaApplication.class, args);
	}
}
