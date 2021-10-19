package com.example.jpastudy.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(value = "select m from Member m left join fetch Team t where t.name = :name")
    Member selectMemberWithTeamNameLeftJoin(@Param("name")String name);

    @Query(value = "select m from Member m join fetch Team t where t.name = :name")
    Member selectMemberWithTeamName(@Param("name")String name);
}
