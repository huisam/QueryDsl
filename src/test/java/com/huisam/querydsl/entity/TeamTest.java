package com.huisam.querydsl.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@SpringBootTest
@Transactional
class TeamTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("엔티티 테스트")
    void testEntity() {
        /* given */
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
}