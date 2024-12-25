package com.wikicoding.psqlredisbenchmark.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("persons")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CachedPerson implements Serializable {
    @Id
    private int id;
    private String name;
}
