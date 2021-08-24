package com.example.jpastudy.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Locker {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public Locker() {
    }

    public Locker(String name) {
        this.name = name;
    }

    public Locker(String name, Team team) {
        this(null, name, team);
    }

    public Locker(Long id, String name, Team team) {
        this.id = id;
        this.name = name;
        this.team = team;
    }

    public void setTeam(Team team) {
        this.team = team;    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Team getTeam() {
        return team;
    }
}
