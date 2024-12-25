package com.wikicoding.psqlredisbenchmark.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("persons")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PersonMongo {
    @Id
    private String id;
    private String name;

    public PersonMongo(String name) {
        this.name = name;
    }
}
