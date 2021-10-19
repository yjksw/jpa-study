package com.example.jpastudy;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpastudy.domain.Locker;
import com.example.jpastudy.domain.Member;
import com.example.jpastudy.domain.MemberRepository;
import com.example.jpastudy.domain.Team;
import com.example.jpastudy.domain.TeamRepository;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
public class FetchJoinTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        Team teamA = new Team("teamA");
        teamA.addLocker(new Locker("L1"));
        teamA.addLocker(new Locker("L2"));
        Team teamB = new Team("teamB");
        teamB.addLocker(new Locker("L3"));
        teamB.addLocker(new Locker("L4"));

        Member memberA1 = new Member("memberA1");
        Member memberA2 = new Member("memberA2");
        Member memberA3 = new Member("memberA3");

        Member memberB1 = new Member("memberB1");
        Member memberB2 = new Member("memberB2");
        Member memberB3 = new Member("memberB3");

        List<Member> teamAMembers = List.of(memberA1, memberA2, memberA3);
        List<Member> teamBMembers = List.of(memberB1, memberB2, memberB3);

        teamA.setMembers(teamAMembers);
        teamB.setMembers(teamBMembers);

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @DisplayName("collection fetch join 데이터에 대해 데이터 뻥튀기가 발생한다.")
    @Test
    void collectionFetchJoin_DuplicatedDataSelected() {

        List<Team> teams = teamRepository.findAll(); //members 와 lockers 가 지연로딩이므로 select 쿼리 1개
        assertThat(teams.size()).isEqualTo(2);

        testEntityManager.flush();
        testEntityManager.clear();

        List<Team> teamsWithMembersNoDistinct = teamRepository.findAllJoinFetchMembersWithoutDistinct();
        assertThat(teamsWithMembersNoDistinct.size())
            .isEqualTo(6); // members 에 대한 fetch join 을 할 경우(distinct 없이) 데이터 뻥튀기가 발생한다.

        List<Team> teamsWithMembersDistinct = teamRepository.findAllJoinFetchMembersWithDistinct();
        assertThat(teamsWithMembersDistinct.size())
            .isEqualTo(2); // members 에 대한 fetch join 을 할 경우(distinct 있음) 데이터 중복이 일어나지 않는다.
    }

    @DisplayName("다수의 OneToMany 연관관계 엔티티를 EAGER로 지정할 경우 예외가 발생한다.")
    @Test
    void findTeam_MultipleBagFetchException() { // 이 테스트 메소드는 성공하지 않는다. bean creating 할 때 이미 오류가 난다.
        //테스트 상황 - Team 엔티티의 Members, Lockers 가 모두 FetchType.EAGER 로 설정되어 있다.
        // assertThatThrownBy(() -> teamRepository.findAll())
        //    .isInstanceOf(MultipleBagFetchException.class);
    }

    @DisplayName("Team의 Members, Lockers 관련 서비스 로직이 있을 때 N+1 문제가 발생한다.")
    @Test
    void findAll_TooManyQuery() {

        List<Team> teams = teamRepository.findAll(); //모든 teams 를 조회하는 select 쿼리 1개가 발생한다.

        List<String> memberNames = teams.stream() //각 팀 A개에 대한 각각의 Member select 쿼리 N개가 발생한다.
            .map(Team::getMembers)
            .flatMap(Collection::stream)
            .map(Member::getName)
            .collect(toList());

        List<String> lockerNums = teams.stream() //각 팀 A개에 대한 각각의 Locker select 쿼리 N개가 발생한다.
            .map(Team::getLockers)
            .flatMap(Collection::stream)
            .map(Locker::getName)
            .collect(toList());

        assertThat(teams.size()).isEqualTo(2);
        assertThat(memberNames.size()).isEqualTo(6);
        assertThat(lockerNums.size()).isEqualTo(4);
    }

    @DisplayName("Team의 Members, Lockers 관련 서비스 로직이 있을 때 N+1 문제가 발생한다.")
    @Test
    void findAllJoinFetchLockers() {

        List<Team> teams = teamRepository.findAllJoinFetchLockers(); //모든 teams 를 조회할 때 Lockers 는 fetch join 으로 즉시로딩한다.

        List<String> memberNames = teams.stream() //각 팀 A개에 대한 각각의 Member select 쿼리 N개가 발생한다.
            .map(Team::getMembers)
            .flatMap(Collection::stream)
            .map(Member::getName)
            .collect(toList());

        List<String> lockerNums = teams.stream() //teams 를 조회할 때 즉시로딩 했으므로 추가쿼리가 발생하지 않는다.
            .map(Team::getLockers)
            .flatMap(Collection::stream)
            .map(Locker::getName)
            .collect(toList());

        assertThat(teams.size()).isEqualTo(2);
        assertThat(memberNames.size()).isEqualTo(6);
        assertThat(lockerNums.size()).isEqualTo(4);
    }

    @DisplayName("두개의 collection에 대한 fetch join을 실행하면 예외가 발생한다.")
    @Test
    void findAllJoinFetchAll() {

        /* 이 테스트 메소드는 성공하지 않는다. bean creating 할 때 이미 오류가 난다.
        * 테스트 상황 - Team 엔티티의 Members, Lockers 를 한꺼번에 fetch join 하는 JPQL 을 실행한다.
        *
        * assertThatThrownBy(() -> teamRepository.findAllJoinFetchAll())
        *    .isInstanceOf(MultipleBagFetchException.class);
        */

    }

    @DisplayName("Batch size를 적용해 Pagination을 하면 N+1문제나 경고가 뜨지 않는다.")
    @Test
    void findWithPagination() { //application.properties 에 batch size 옵션 적용한 경우
        Pageable pageable = PageRequest.of(0, 2);
        List<Team> teams = teamRepository
            .findAll(pageable)
            .toList();

        List<String> memberNames = teams.stream() //최대 batch size 만큼 IN 쿼리로 한번에 처리된다
            .map(Team::getMembers)
            .flatMap(Collection::stream)
            .map(Member::getName)
            .collect(toList());

        List<String> lockerNums = teams.stream() //최대 batch size 만큼 IN 쿼리로 한번에 처리된다
            .map(Team::getLockers)
            .flatMap(Collection::stream)
            .map(Locker::getName)
            .collect(toList());

        assertThat(teams.size()).isEqualTo(2);
        assertThat(memberNames.size()).isEqualTo(6);
        assertThat(lockerNums.size()).isEqualTo(4);
    }

    @DisplayName("fetch join 대상에 조건문을 걸었을 때 데이터가 불일치하다.")
    @Test
    void findTeamWithSpecificNameMember() {
        // given
        Team teamA = teamRepository.findById(1L).orElseThrow();
        int teamAMemberSize = teamA.getMembers().size();
        testEntityManager.clear();

        // when
        Team teamAWithMemberName = teamRepository.findTeamWithSomeMemberByName("memberA1");

        // then
        /* 본래 teamA에 3명의 멤버가 들어가있지만 fetch join 대상에 where문이 들어가면서 데이터 불일치가 일어났다.
        * collection 에는 관련 데이터가 모두 들어가있기를 기대하는데 그렇게 되지를 않는다.
        * 따라서 fetch join
         */
        assertThat(teamA.getName()).isEqualTo(teamAWithMemberName.getName());
        assertThat(teamAMemberSize).isNotEqualTo(teamAWithMemberName.getMembers().size());
    }
}
