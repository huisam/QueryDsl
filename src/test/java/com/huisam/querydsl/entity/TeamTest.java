package com.huisam.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.huisam.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TeamTest {

    @Autowired
    EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void testEntity() {
        /* given */
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamA");
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

        // 초기화
        em.flush();
        em.clear();

        /* when */
        final List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        /* then */
        members.forEach(member -> {
            System.out.println("member = " + member);
            System.out.println("-> member.team" + member.getTeam());
        });
    }

    @Test
    @DisplayName("jpql 테스트")
    void startJPQL() {
        /* when */
        final Member findByJPQL = em.createQuery("select m from Member m" +
                " where m.username =:username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        /* then */
        assertThat(findByJPQL.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("JPA Query Factory 테스트")
    void jpa_query_factory_test() {
        /* when */
        final Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        /* then */
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("and / or Query")
    void search() {
        /* given & when */
        final Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10))
                )
                .fetchOne();
        /* then */
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("and / or Query")
    void search_param() {
        /* given & when */
        final Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"), member.age.eq(10)
                )
                .fetchOne();
        /* then */
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("result fetch 테스트")
    void result_fetch() {
        // 리스트 조회
        final List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // 단 한건 조회, 결과가 둘 이상이면 NonUniqueException
        final Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        final Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        // 페이징 쿼리
        final QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        queryResults.getTotal();
        final List<Member> results = queryResults.getResults();

        // count로 쿼리 작성해서 조회
        final long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    @Test
    @DisplayName("1.회원 나이 내림차순 2.회원 이름 올림차순 3. 회원 없으면 null")
    void sort() {
        /* given */
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        /* when */
        final List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member result5 = fetch.get(0);
        Member result6 = fetch.get(1);
        Member memberNull = fetch.get(2);

        /* then */
        assertThat(result5.getUsername()).isEqualTo("member5");
        assertThat(result6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }
}