package com.example.jpastudy.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Team() {
    }

    public Team(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
