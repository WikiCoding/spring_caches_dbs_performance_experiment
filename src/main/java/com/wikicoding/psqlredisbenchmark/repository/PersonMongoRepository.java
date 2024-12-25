package com.wikicoding.psqlredisbenchmark.repository;

import com.wikicoding.psqlredisbenchmark.entities.PersonMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonMongoRepository extends MongoRepository<PersonMongo, String> {
}
