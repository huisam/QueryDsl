package com.huisam.querydsl.repository;

import com.huisam.querydsl.dto.MemberSearchCondition;
import com.huisam.querydsl.entity.Member;
import com.huisam.querydsl.repository.support.QueryDsl4RepositorySupport;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.huisam.querydsl.entity.QMember.member;
import static com.huisam.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberTestRepository extends QueryDsl4RepositorySupport {

    public MemberTestRepository(Class<?> domainClass) {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query ->
                query.selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                                userNameEq(condition.getUserName()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
    }

    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, contentQuery ->
                contentQuery.selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                                userNameEq(condition.getUserName()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ), countQuery -> countQuery
                .select(member.id)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
        );
    }

    private BooleanExpression userNameEq(String userName) {
        return hasText(userName) ? member.username.eq(userName) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}
