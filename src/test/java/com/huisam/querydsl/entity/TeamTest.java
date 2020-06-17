package com.huisam.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import static com.huisam.querydsl.entity.QTeam.team;
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
        assertThat(findMember).isNotNull();
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
        assertThat(findMember).isNotNull();
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
        assertThat(findMember).isNotNull();
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

    @Test
    @DisplayName("페이징 테스트")
    void paging1() {
        /* given */
        final List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        /* when */

        /* then */
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("페이징 테스트2")
    void paging2() {
        /* given */
        final QueryResults<Member> queryResult = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        /* when */

        /* then */
        assertThat(queryResult.getTotal()).isEqualTo(4);
        assertThat(queryResult.getLimit()).isEqualTo(2);
        assertThat(queryResult.getOffset()).isEqualTo(1);
        assertThat(queryResult.getResults().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("집합기능 테스트")
    void aggregation() {
        /* given & when */
        final List<Tuple> tuples = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        /* then */
        final Tuple tuple = tuples.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
    }

    @Test
    @DisplayName("팀의 이름과 각 팀의 평균 연령을 구해라")
    void group() {
        /* given */
        final List<Tuple> tuples = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        /* when */
        final Tuple resultA = tuples.get(0);
        final Tuple resultB = tuples.get(1);

        /* then */
        assertThat(resultA.get(team.name)).isEqualTo("teamA");
        assertThat(resultA.get(member.age.avg())).isEqualTo(15);

        assertThat(resultB.get(team.name)).isEqualTo("teamB");
        assertThat(resultB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    @DisplayName("팀A에 소속된 모든 회원")
    void join_test() {
        final List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

}