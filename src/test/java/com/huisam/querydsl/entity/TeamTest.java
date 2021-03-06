package com.huisam.querydsl.entity;

import com.huisam.querydsl.dto.MemberDto;
import com.huisam.querydsl.dto.QMemberDto;
import com.huisam.querydsl.dto.UserDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.List;

import static com.huisam.querydsl.entity.QMember.member;
import static com.huisam.querydsl.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.select;
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

    @Test
    @DisplayName("회원과 팀을 조인하면서, 팀 이름이 A인 팀만 join, 회원은 모두 조회")
    void join_on_filtering() {
        final List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("fetch join")
    void fetchJoinNo() {
        em.flush();
        em.clear();

        final Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 미적용").isFalse();
    }

    @Test
    @DisplayName("fetch join Use")
    void fetchJoinUse() {
        em.flush();
        em.clear();

        final Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 적용").isTrue();
    }

    @Test
    @DisplayName("서브 쿼리")
    void subQuery() {
        QMember memberSub = new QMember("memberSub");

        final List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);

    }

    @Test
    @DisplayName("나이가 평균 이상인 서브 쿼리")
    void subQuery2() {
        QMember memberSub = new QMember("memberSub");

        final List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    @Test
    @DisplayName("서브 쿼리 In")
    void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        final List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    @DisplayName("select 서브 쿼리")
    void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        final List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple : " + tuple);
        }

    }

    @Test
    @DisplayName("case문 테스트")
    void caseTest() {
        final List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println(s);
        }

        final List<String> result2 = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : result2) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("상수 문자 처리")
    void testConstant() {
        final List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @DisplayName("concat 문자 처리")
    void test_concat() {
        final List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("프로젝션 테스트")
    void simpleProjection() {
        final List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("tuple 테스트")
    void tupleProjection() {
        final List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            final String userName = tuple.get(member.username);
            final Integer age = tuple.get(member.age);
            System.out.println("userName = " + userName);
            System.out.println("age = " + age);
        }
    }

    @Test
    @DisplayName("dto 조회하기")
    void testDTO_JPQL() {
        final List<MemberDto> result = em.createQuery("select new com.huisam.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("dto queryDSL조회하기")
    void testDTO_Query_DSL() {
        final List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("dto queryDSL조회하기")
    void testDTO_Query_DSL_By_field() {
        final List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("dto queryDSL조회하기")
    void testDTO_Query_DSL_By_constructor() {
        final List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("dto queryDSL조회하기")
    void testUserDTO_Query_DSL_By_other_DTO() {
        final List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    @Test
    @DisplayName("dto queryDSL조회하기")
    void testUserDTO_Query_DSL_By_sub_query() {
        final QMember memberSub = new QMember("memberSub");
        final List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class, member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age"))
                )
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }


    @Test
    @DisplayName("query Injection")
    void test_query_injection() {
        /* given */
        final List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("동적 쿼리 Boolean builder 테스트")
    void test_dynamicQuery_BooleanBuilder() {
        /* given */
        String userNameParam = "member1";
        Integer ageParam = null;

        /* when */
        List<Member> result = searchMember(userNameParam, ageParam);

        /* then */
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember(String userNameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (userNameCond != null) {
            builder.and(member.username.eq(userNameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


    @Test
    @DisplayName("동적 쿼리 Where Param 테스트")
    void test_Where_param() {
        /* given */
        String userNameParam = "member1";
        Integer ageParam = null;

        /* when */
        List<Member> result = searchMember2(userNameParam, ageParam);

        /* then */
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String userNameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(userNameCond, ageCond))
                .fetch();
    }

    private Predicate allEq(String userNameCond, Integer ageCond) {
        return userNameEq(userNameCond).and(ageEq(ageCond));
    }

    private BooleanExpression userNameEq(String userNameCond) {
        return userNameCond != null ? member.username.eq(userNameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    @Test
    @DisplayName("벌크 업데이트")
    void bulk_Update() {
        final long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        // 영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        final List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        // 영속성 컨텍스트 때문에 예전 update 전 자료들이 저장되어 있음
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    @DisplayName("bulk Add 테스트")
    void bulkAdd() {
        final long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

    }

    @Test
    @DisplayName("bulk delete 테스트")
    void test_bulkDelete() {
        final long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

    }

    @Test
    @DisplayName("sql function 호출하기")
    void test_sql_Function() {
        final List<String> result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("sql function 호출하기2")
    void test_sql_Function2() {
        final List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(
                        Expressions.stringTemplate("function('lower', {0})", member.username))
                )
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


}