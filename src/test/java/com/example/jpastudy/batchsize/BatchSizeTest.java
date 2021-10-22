package com.example.jpastudy.batchsize;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpastudy.domain.Member;
import com.example.jpastudy.domain.MemberRepository;
import com.example.jpastudy.domain.Team;
import com.example.jpastudy.domain.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DisplayName("default_batch_fetch_size 관련 궁금증 해결 테스트")
@DataJpaTest
public class BatchSizeTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @DisplayName("Member 리스트 조회 시 Team을 lazy loading 할 때 in 쿼리 Team이 한꺼번에 조회된다.")
    @Test
    void team_inquery_working() {

        // given
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        testEntityManager.flush();
        testEntityManager.clear();

        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        member1.setTeam(teamA);
        member2.setTeam(teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        testEntityManager.flush();

        // when
        List<Member> members = new ArrayList<>();
        members.add(memberRepository.findById(1L).get()); // Member 를 조회하는 쿼리가 생성된다.
        members.add(memberRepository.findById(2L).get());

        List<String> teamNames = members.stream()
            .map(member -> member.getTeam().getName())
            .collect(toList()); // Team 을 조회하는 쿼리가 in 쿼리로 수행된다.

        // then
        assertThat(teamNames).hasSize(2);
    }

    @DisplayName("Team을 initialize 할 때 in 쿼리가 수행되지 않는다.")
    @Test
    void team_inquery_notWorking() {

        EntityManager em = testEntityManager.getEntityManager();

        // given
        Team savedTeamA = teamRepository.save(new Team("TeamA"));
        Team savedTeamB = teamRepository.save(new Team("TeamB"));

        testEntityManager.flush();
        testEntityManager.clear();

        // when
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        Team teamA = em.getReference(Team.class, savedTeamA.getId()); // Team은 프록시 객체다.
        Team teamB = em.getReference(Team.class, savedTeamB.getId());
        member1.setTeam(teamA);
        member2.setTeam(teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        testEntityManager.flush();

        System.out.println("===============================");
        List<Member> members = new ArrayList<>();
        members.add(memberRepository.findById(1L).get()); // 영속성 컨텍스트에 있는 Member를 로딩한다.
        members.add(memberRepository.findById(2L).get());


        System.out.println("===============================");

        List<String> teamNames = members.stream()
            .map(member -> member.getTeam().getName()) // 각 멤버의 개수만큼 team을 select하는 쿼리를 실행한다.
            .collect(toList());

        // then
        assertThat(teamNames).hasSize(2);
    }
}
