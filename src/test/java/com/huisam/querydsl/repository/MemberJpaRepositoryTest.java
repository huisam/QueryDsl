package com.huisam.querydsl.repository;

import com.huisam.querydsl.dto.MemberSearchCondition;
import com.huisam.querydsl.dto.MemberTeamDto;
import com.huisam.querydsl.entity.Member;
import com.huisam.querydsl.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    @DisplayName("기본 테스트")
    void basic_test() {
        final Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        final Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        final List<Member> result = memberJpaRepository.findAll();
        assertThat(result).containsExactly(member);

        final List<Member> result2 = memberJpaRepository.findByUserName("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    @DisplayName("기본 QueryDsl 테스트")
    void basic_queryDsl_test() {
        final Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        final Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        final List<Member> result = memberJpaRepository.findAll_QueryDsl();
        assertThat(result).containsExactly(member);

        final List<Member> result2 = memberJpaRepository.findByUserName_QueryDsl("member1");
        assertThat(result2).containsExactly(member);

    }

    @Test
    @DisplayName("search 테스트")
    void search_test() {
        /* given */
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);


        final MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        final List<MemberTeamDto> result = memberJpaRepository.search(condition);

        assertThat(result).extracting("username")
                .containsExactly("member4");
    }
}