package com.huisam.querydsl.repository;

import com.huisam.querydsl.entity.Member;
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
}