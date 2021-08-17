package com.example.jpastudy.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jpastudy.domain.Member;
import com.example.jpastudy.domain.MemberRepository;
import com.example.jpastudy.domain.Team;
import javax.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
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
    void find_actualEntity() {

        Member member = new Member("member1");
        Team team = new Team("Team A");
        member.setTeam(team);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        EntityManager em = testEntityManager.getEntityManager();
        //설정된 fetch 전략에 따라 select 시 연관객체를 즉시로딩 혹은 지연로딩한다.
        Member findMember = em.find(Member.class, savedMember.getId()); //select 퀴리가 호출된다.

        assertThat(findMember.getId()).isNotNull();
        assertThat(findMember.getName()).isEqualTo("member1");
        assertThat(findMember.getClass()).isEqualTo(Member.class); //실제 엔티티 클래스임을 확인할 수 있다.
    }

    @DisplayName("em.getReference()는 해당 entity에 대한 proxy 객체를 가져온다.")
    @Test
    void getReference_proxyEntity() {

        Member member = new Member("member1");
        Team team = new Team("Team A");
        member.setTeam(team);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        EntityManager em = testEntityManager.getEntityManager();
        Member findMember = em.getReference(Member.class, savedMember.getId()); //아무 쿼리가 호출되지 않는다.

        assertThat(findMember.getId()).isNotNull(); //getId()는 여전히 쿼리는 호출되지 않는다.
        assertThat(findMember.getClass()).isNotEqualTo(Member.class); //실제 Entity 클래스가 아니다.
        assertThat(findMember).isInstanceOf(Member.class); //프록시 객체는 실제 Entity 를 상속한다.
        System.out.println(findMember.getClass());
    }

    @DisplayName("proxy 객체는 getName() 등을 호출 할 경우 초기화 된다.")
    @Test
    void getReference_proxyEntity_getName() {

        Member member = new Member("member1");
        Team team = new Team("Team A");
        member.setTeam(team);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        EntityManager em = testEntityManager.getEntityManager();
        Member findMember = em.getReference(Member.class, savedMember.getId()); //아무 쿼리가 호출되지 않는다.

        assertThat(findMember.getId()).isNotNull(); //getId()는 여전히 쿼리는 호출되지 않는다.
        assertThat(findMember.getName()).isEqualTo("member1"); //select 쿼리가 실행된다.

        assertThat(findMember.getClass()).isNotEqualTo(Member.class); //초기화 되어도 여전히 실제 Entity 클래스가 아니다.
        assertThat(findMember).isInstanceOf(Member.class); //프록시 객체는 실제 Entity 를 상속한다.
    }

    @DisplayName("proxy 객체의 타입은 실제 entity 객체 타입과 다르다. getClass()로 비교할 경우 false가 반환된다.")
    @Test
    void proxyType_getClass() {

        Member member = new Member("member1");
        Team teamA = new Team("Team A");
        member.setTeam(teamA);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        //프록시 객체를 가져오기 위해 Member의 Team 프로퍼티를 FetchType.LAZY로 설정
        EntityManager em = testEntityManager.getEntityManager();
        Member findMember = em.find(Member.class, savedMember.getId()); //Team은 지연로딩으로 프록시객체이다.
        Team findTeam = findMember.getTeam();

        assertThat(teamA.getName()).isEqualTo(findTeam.getName()); //기존 TeamA와 프록시 팀인 findTeam의 필드값은 동일하다.
        assertThat(teamA.getClass()).isNotEqualTo(findTeam.getClass()); //프록시 팀읜 findTeam의 class는 Team이 아니다.

        assertThat(findTeam).isInstanceOf(Team.class); //isInstanceOf로 비교하면 상속 받은 Team.class 이다.
    }

    @DisplayName("이미 영속성 컨텍스트에 있는 entity 조회시 getReference로 조회해도 실제 entity가 반환된다.")
    @Test
    void getReference_AlreadyInPersistenceContext() {

        Member member = new Member("member1");
        Team teamA = new Team("Team A");
        member.setTeam(teamA);

        Member savedMember = memberRepository.save(member); //영속성 컨텍스트에 존재햔다.

        EntityManager em = testEntityManager.getEntityManager();
        Member findMember = em.getReference(Member.class, savedMember.getId()); // 프록시 객체를 가져오듯 메서드를 호출한다.

        assertThat(findMember.getClass()).isEqualTo(Member.class); //프록시가 아닌 실제 entity 클래스를 가져온다.
    }

    @DisplayName("준영속 프록시 객체에 대한 초기화 시 예외가 발생한다.")
    @Test
    void getReference_InitializationExcpetion() {

        Member member = new Member("member1");
        Team teamA = new Team("Team A");
        member.setTeam(teamA);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        EntityManager em = testEntityManager.getEntityManager();
        Member findMember = em.find(Member.class, savedMember.getId()); //Team 을 lazy 로딩 설정하여 프록시 객체로 가져온다.

        em.clear(); //먼저 영속성 컨텍스트를 clear 해주어야한다.
        em.close(); //프록시 객체를 가져온 영속성 컨텍스트를 종료한다.

        assertThatThrownBy(() -> findMember.getTeam().getName()) //영속성 컨텍스트가 종료된 이후에 초기화를 시도하면
            .isInstanceOf(LazyInitializationException.class); //하이버네이트 예외가 발생한다.
    }

    @DisplayName("프록시 객체를 확인할 수 있다. ")
    @Test
    void PersistenceUnitUtil_Proxy() {

        Member member = new Member("member1");
        Team teamA = new Team("Team A");
        member.setTeam(teamA);

        Member savedMember = memberRepository.save(member);

        testEntityManager.flush();
        testEntityManager.clear();

        EntityManager em = testEntityManager.getEntityManager();
        Member findMember = em.find(Member.class, savedMember.getId()); //Team 을 lazy 로딩 설정하여 프록시 객체로 가져온다.

        boolean memberIsLoaded = em.getEntityManagerFactory()
            .getPersistenceUnitUtil().isLoaded(findMember);

        boolean teamIsLoaded = em.getEntityManagerFactory()
            .getPersistenceUnitUtil().isLoaded(findMember.getTeam()); //EntityManagerFactory에서 PersistenceUnitUtil을 가져온다.

        assertThat(memberIsLoaded).isTrue(); //findMember 는 실제 entity 이므로 결과값이 true 이다.
        assertThat(teamIsLoaded).isFalse(); //team 은 프록시 객체이므로 결과값이 false 이다.
    }
}
