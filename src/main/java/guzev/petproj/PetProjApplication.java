package guzev.petproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class PetProjApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetProjApplication.class, args);
	}

}
