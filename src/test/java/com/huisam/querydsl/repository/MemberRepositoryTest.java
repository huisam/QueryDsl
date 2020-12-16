package com.huisam.querydsl.repository;

import com.huisam.querydsl.dto.MemberSearchCondition;
import com.huisam.querydsl.dto.MemberTeamDto;
import com.huisam.querydsl.entity.Member;
import com.huisam.querydsl.entity.QMember;
import com.huisam.querydsl.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("기본 테스트")
    void basic_test() {
        final Member member = new Member("member1", 10);
        memberRepository.save(member);

        final Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        final List<Member> result = memberRepository.findAll();
        assertThat(result).containsExactly(member);

        final List<Member> result2 = memberRepository.findByUsername("member1");
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

        final List<MemberTeamDto> result = memberRepository.search(condition);

        assertThat(result).extracting("username")
                .containsExactly("member4");
    }

    @Test
    @DisplayName("search pagination 테스트")
    void search_page_test() {
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
        final PageRequest pageRequest = PageRequest.of(0, 3);

        final Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

        assertThat(result).extracting("username")
                .containsExactly("member1", "member2", "member3");
        assertThat(result.getSize()).isEqualTo(3);
    }

    @Test
    @DisplayName("queryDsl Predicate 테스트")
    void test_queryDsl_predicate() {
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

        final QMember qMember = QMember.member;
        final Iterable<Member> result = memberRepository.findAll(qMember.age.between(10, 40).and(qMember.username.eq("member1")));
        for (Member member : result) {
            System.out.println("member1 = " + member);
        }
    }
}