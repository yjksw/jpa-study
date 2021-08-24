package com.example.jpastudy.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(
        mappedBy = "team",
        cascade = CascadeType.PERSIST,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<Member> members = new ArrayList<>();

    @OneToMany(
        mappedBy = "team",
        cascade = CascadeType.PERSIST,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<Locker> lockers;

    public Team() {
    }

    public Team(String name) {
        this(null, name, new ArrayList<>(), new ArrayList<>());
    }

    public Team(Long id, String name, List<Member> members,
        List<Locker> lockers) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.lockers = lockers;
    }

    public void addLocker(Locker locker) {
        locker.setTeam(this);
        lockers.add(locker);
    }

    public void setMembers(List<Member> members) {
        members.stream()
            .forEach(member -> member.setTeam(this));

        this.members = members;
    }

    public String getName() {
        return name;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<Locker> getLockers() {
        return lockers;
    }
}
