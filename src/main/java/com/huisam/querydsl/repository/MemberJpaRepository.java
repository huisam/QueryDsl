package com.huisam.querydsl.repository;

import com.huisam.querydsl.dto.MemberSearchCondition;
import com.huisam.querydsl.dto.MemberTeamDto;
import com.huisam.querydsl.dto.QMemberTeamDto;
import com.huisam.querydsl.entity.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.huisam.querydsl.entity.QMember.member;
import static com.huisam.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        final Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll_QueryDsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByUserName(String userName) {
        return em.createQuery("select m from Member m where m.username = :userName", Member.class)
                .setParameter("userName", userName)
                .getResultList();
    }

    public List<Member> findByUserName_QueryDsl(String userName) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(userName))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition searchCondition) {

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(searchCondition.getUserName())) {
            builder.and(member.username.eq(searchCondition.getUserName()));
        }
        if (hasText(searchCondition.getTeamName())) {
            builder.and(team.name.eq(searchCondition.getTeamName()));
        }
        if (searchCondition.getAgeGoe() != null) {
            builder.and(member.age.goe(searchCondition.getAgeGoe()));
        }
        if (searchCondition.getAgeLoe() != null) {
            builder.and(member.age.loe(searchCondition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"), member.username, member.age, team.id.as("teamId"), team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

}
