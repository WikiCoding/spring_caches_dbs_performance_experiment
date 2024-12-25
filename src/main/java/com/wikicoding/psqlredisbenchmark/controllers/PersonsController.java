package com.wikicoding.psqlredisbenchmark.controllers;

import com.wikicoding.psqlredisbenchmark.entities.CachedPerson;
import com.wikicoding.psqlredisbenchmark.entities.Person;
import com.wikicoding.psqlredisbenchmark.entities.PersonMongo;
import com.wikicoding.psqlredisbenchmark.repository.PersonMongoRepository;
import com.wikicoding.psqlredisbenchmark.repository.PersonRepository;
import com.wikicoding.psqlredisbenchmark.repository.RedisCache;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class PersonsController {
    private final PersonRepository personRepository;
    private final PersonMongoRepository personMongoRepository;
    private final RedisCache redisCache;
    private final Logger logger = LoggerFactory.getLogger(PersonsController.class);

    @GetMapping
    public ResponseEntity<List<Person>> findAll() {
        long startTime = System.currentTimeMillis();
        Iterable<CachedPerson> cachedPeople = redisCache.findAll();
        long endTime = System.currentTimeMillis();

        logger.info("Cache query took {} ms", (endTime - startTime));

        List<Person> people = new ArrayList<>();

        if (cachedPeople.iterator().hasNext()) {
            for (CachedPerson cached : cachedPeople) {
                Person person = new Person(cached.getId(), cached.getName());
                people.add(person);
            }
        }

        long endCache = System.currentTimeMillis();
        logger.info("Full Cache trip took {} ms", (endCache - startTime));

        if(!cachedPeople.iterator().hasNext()) {
            long start = System.currentTimeMillis();
            people = personRepository.findAll();
            long end = System.currentTimeMillis();
            logger.info("Db query took {} ms", (end - start));

            long saveTime = System.currentTimeMillis();
            people.forEach(person -> redisCache.save(new CachedPerson(person.getId(), person.getName())));
            long endSave = System.currentTimeMillis();
            logger.info("Save in cache took {} ms", (endSave - saveTime));
        }

        return ResponseEntity.ok(people);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable(name = "id") int id) {
        long startTime = System.currentTimeMillis();
        Optional<CachedPerson> cachedPerson = redisCache.findById(id);

        if (cachedPerson.isPresent()) {
            CachedPerson cached = cachedPerson.get();
            logger.info("Person with name {} found in cache", cached.getName());

            Person person = new Person(cached.getId(), cached.getName());
            long endTime = System.currentTimeMillis();
            logger.info("Cache query took {} ms", (endTime - startTime));

            return ResponseEntity.ok(person);
        }

        logger.info("Person with id {} not found in cache", id);
        long start = System.currentTimeMillis();
        Optional<Person> person = personRepository.findById(id);

        if (person.isPresent()) {
            long end = System.currentTimeMillis();
            logger.info("Db query took {} ms", (end - start));
            return ResponseEntity.ok(person.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/mongodb")
    public ResponseEntity<Iterable<PersonMongo>> findAllMongoDb() {
        long start = System.currentTimeMillis();
        Iterable<PersonMongo> persons = personMongoRepository.findAll();

        long end = System.currentTimeMillis();
        logger.info("Db query took {} ms", (end - start));

        return ResponseEntity.ok(persons);
    }

    @GetMapping("/mongodb/{id}")
    public ResponseEntity<PersonMongo> findByIdMongoDb(@PathVariable(name = "id") String id) {
        long start = System.currentTimeMillis();
        Optional<PersonMongo> person = personMongoRepository.findById(id);

        if (person.isPresent()) {
            long end = System.currentTimeMillis();
            logger.info("Db query took {} ms", (end - start));
            return ResponseEntity.ok(person.get());
        }

        return ResponseEntity.notFound().build();
    }
}
