package com.wikicoding.psqlredisbenchmark.repository;

import com.wikicoding.psqlredisbenchmark.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
}
