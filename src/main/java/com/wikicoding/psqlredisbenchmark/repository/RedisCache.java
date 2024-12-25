package com.wikicoding.psqlredisbenchmark.repository;

import com.wikicoding.psqlredisbenchmark.entities.CachedPerson;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisCache extends CrudRepository<CachedPerson, Integer> {
}
