package com.example.jpastudy.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("select distinct t from Team t join fetch t.members")
    List<Team> findAllJoinFetchMembersWithDistinct();

    @Query("select distinct t from Team t join fetch t.members")
    List<Team> findAllJoinFetchMembersWithoutDistinct();

    @Query("select distinct t from Team t join fetch t.lockers")
    List<Team> findAllJoinFetchLockers();

//    @Query("select distinct t from Team t join fetch t.members join fetch t.lockers") //MultipleBagFetchExcetion 발생 쿼리
//    List<Team> findAllJoinFetchAll();
}
