package com.example.jpastudy.proxy;

import com.example.jpastudy.domain.Member;
import com.example.jpastudy.domain.MemberRepository;
import com.example.jpastudy.domain.Team;
import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DisplayName("JPA 프록시 테스트")
@DataJpaTest
public class ProxyTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("em.find()는 실제 entity를 조회하여 가져온다.")
    @Test
    void emFind() {

        Member member = new Member("member1");
        Team team = new Team("Team A");
        member.setTeam(team);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        EntityManager em = testEntityManager.getEntityManager();
        Member emFindMember = em.find(Member.class, savedMember.getId());
    }
}
