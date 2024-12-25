package com.wikicoding.psqlredisbenchmark;

import com.wikicoding.psqlredisbenchmark.entities.Person;
import com.wikicoding.psqlredisbenchmark.entities.PersonMongo;
import com.wikicoding.psqlredisbenchmark.repository.PersonMongoRepository;
import com.wikicoding.psqlredisbenchmark.repository.PersonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PsqlredisbenchmarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsqlredisbenchmarkApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(PersonRepository personRepository, PersonMongoRepository personMongoRepository) {
		return args -> {
			for (int i = 0; i < 100_000; i++) {
				personRepository.save(new Person(0, "John Doe" + i));
				personMongoRepository.save(new PersonMongo("John Doe" + i));
			}
			// personRepository.save(new Person(0, "John Doe"));
			// personRepository.save(new Person(0, "Jane Doe"));
			// personRepository.save(new Person(0, "Tom Doe"));

			// personMongoRepository.save(new PersonMongo("John Doe"));
			// personMongoRepository.save(new PersonMongo("Jane Doe"));
			// personMongoRepository.save(new PersonMongo("Tom Doe"));
		};
	}

}
