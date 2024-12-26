package com.wikicoding.psqlredisbenchmark.controllers;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.wikicoding.psqlredisbenchmark.entities.CachedPerson;
import com.wikicoding.psqlredisbenchmark.entities.Person;
import com.wikicoding.psqlredisbenchmark.entities.PersonMongo;
import com.wikicoding.psqlredisbenchmark.repository.PersonMongoRepository;
import com.wikicoding.psqlredisbenchmark.repository.PersonRepository;
import com.wikicoding.psqlredisbenchmark.repository.RedisCache;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/")
public class PersonsController {
    private final PersonRepository personRepository;
    private final PersonMongoRepository personMongoRepository;
    private final RedisCache redisCache;
    private final HazelcastInstance hazelcastInstance;
    private final Logger logger = LoggerFactory.getLogger(PersonsController.class);

    public PersonsController(PersonRepository personRepository,
                             PersonMongoRepository personMongoRepository,
                             RedisCache redisCache,
                             @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        this.personRepository = personRepository;
        this.personMongoRepository = personMongoRepository;
        this.redisCache = redisCache;
        this.hazelcastInstance = hazelcastInstance;
    }

    private ConcurrentMap<String, Person> retrieveMap() {
        return hazelcastInstance.getMap("persons");
    }

    @GetMapping
    public ResponseEntity<List<Person>> findAll() {
        long startHazel = System.currentTimeMillis();
        IMap<String, Person> map = hazelcastInstance.getMap("persons");
        List<Person> hazelcastPeople = map.values().stream().toList();
        long endHazel = System.currentTimeMillis();

        long startTime = System.currentTimeMillis();
        Iterable<CachedPerson> cachedPeople = redisCache.findAll();
        long endTime = System.currentTimeMillis();

        logger.info("Hazelcast Cache query found {} elements", hazelcastPeople.size());
        logger.info("Hazelcast Cache query took {} ms", (endHazel - startHazel));

        List<Person> people = new ArrayList<>();

        if (cachedPeople.iterator().hasNext()) {
            for (CachedPerson cached : cachedPeople) {
                Person person = new Person(cached.getId(), cached.getName());
                people.add(person);
            }
            logger.info("Redis Cache query found {} elements", people.size());
            logger.info("Redis Cache query took {} ms", (endTime - startTime));
        }

        if(!cachedPeople.iterator().hasNext()) {
            long start = System.currentTimeMillis();
            people = personRepository.findAll();
            long end = System.currentTimeMillis();
            logger.info("Db query took {} ms and found {} elements", (end - start), people.size());

            long saveTime = System.currentTimeMillis();
            people.forEach(person -> redisCache.save(new CachedPerson(person.getId(), person.getName())));
            long endSave = System.currentTimeMillis();
            logger.info("Redis save in cache took {} ms", (endSave - saveTime));

            long saveTimeHazelcast = System.currentTimeMillis();
            people.forEach(person -> retrieveMap().put(String.valueOf(person.getId()), person));
            long endSaveHazelcast = System.currentTimeMillis();
            logger.info("Hazelcast save in cache took {} ms", (endSaveHazelcast - saveTimeHazelcast));
        }

        logger.info("-------------------------------------------------------------------------------------");

        return ResponseEntity.ok(people);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable(name = "id") int id) {
        long startTime = System.currentTimeMillis();
        Optional<CachedPerson> cachedPerson = redisCache.findById(id);
        long endTime = System.currentTimeMillis();
        logger.info("Redis Cache query took {} ms", (endTime - startTime));

        long startHazel = System.currentTimeMillis();
        Person hazelcastPerson = retrieveMap().get(String.valueOf(id));
        long endHazel = System.currentTimeMillis();

        logger.info("Hazelcast Cache query found {}", hazelcastPerson.getName());
        logger.info("Hazelcast Cache query took {} ms", (endHazel - startHazel));

        if (cachedPerson.isPresent()) {
            CachedPerson cached = cachedPerson.get();
            logger.info("Person with name {} found in cache", cached.getName());

            Person person = new Person(cached.getId(), cached.getName());

            return ResponseEntity.ok(person);
        }

        logger.info("Person with id {} not found in cache", id);
        long start = System.currentTimeMillis();
        Optional<Person> person = personRepository.findById(id);

        if (person.isPresent()) {
            long end = System.currentTimeMillis();
            logger.info("Db query took {} ms", (end - start));

            Person person1 = person.get();
            retrieveMap().put(String.valueOf(person1.getId()), person1);

            long startHazelcast = System.currentTimeMillis();
            Person person2 = retrieveMap().get("1");
            long endHazelcast = System.currentTimeMillis();

            logger.info("Hazelcast get query elapsed time {} ms", (endHazelcast - startHazelcast));

            return ResponseEntity.ok(person.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/mongodb")
    public ResponseEntity<Iterable<PersonMongo>> findAllMongoDb() {
        long start = System.currentTimeMillis();
        List<PersonMongo> persons = personMongoRepository.findAll();

        long end = System.currentTimeMillis();
        logger.info("Db query took {} ms and found {} elements", (end - start), persons.size());

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
